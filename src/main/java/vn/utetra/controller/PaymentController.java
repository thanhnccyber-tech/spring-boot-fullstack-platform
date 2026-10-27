package vn.utetra.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Order;
import vn.utetra.service.OrderService;
import vn.utetra.service.MomoService;
import vn.utetra.service.VnPayService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thanh toán online qua VNPay và MoMo.
 *  - /payment/vnpay/{id}      : khách (đã đăng nhập) được chuyển sang cổng thanh toán VNPay
 *  - /payment/vnpay-return    : VNPay đưa khách quay lại (công khai, bảo vệ bằng chữ ký)
 *  - /payment/vnpay-ipn       : VNPay báo kết quả server-to-server (công khai, bảo vệ bằng chữ ký)
 *  - /payment/momo/{id}       : khách được chuyển sang cổng thanh toán MoMo
 *  - /payment/momo-return     : MoMo đưa khách quay lại
 *  - /payment/momo-ipn        : MoMo báo kết quả server-to-server
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired private OrderService orderService;
    @Autowired private VnPayService vnPay;
    @Autowired private MomoService momo;

    @GetMapping("/vnpay/{id}")
    public String pay(@PathVariable Long id, Authentication auth, HttpServletRequest req, RedirectAttributes ra) {
        Order o = orderService.ownedOrder(id, auth.getName());
        if (!vnPay.enabled() || !"VNPAY".equals(o.getPaymentMethod()) || !o.isAwaitingOnlinePayment()) {
            ra.addFlashAttribute("error", "Đơn này không cần thanh toán online hoặc đã thanh toán.");
            return "redirect:/order/detail/" + id;
        }
        return "redirect:" + vnPay.buildPaymentUrl(o, VnPayService.clientIp(req));
    }

    @GetMapping("/momo/{id}")
    public String payMomo(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Order order = orderService.ownedOrder(id, auth.getName());
        if (!momo.enabled() || !"MOMO".equals(order.getPaymentMethod()) || !order.isAwaitingOnlinePayment()) {
            ra.addFlashAttribute("error", "Đơn này không cần thanh toán MoMo hoặc đã thanh toán.");
            return "redirect:/order/detail/" + id;
        }
        try {
            return "redirect:" + momo.createPaymentUrl(order);
        } catch (RestClientException | IllegalStateException e) {
            log.error("Không tạo được thanh toán MoMo cho đơn {}", id, e);
            ra.addFlashAttribute("error", "Không thể tạo thanh toán MoMo lúc này. Vui lòng thử lại sau.");
            return "redirect:/order/detail/" + id;
        }
    }

    private static Map<String, String> params(HttpServletRequest req) {
        Map<String, String> m = new LinkedHashMap<>();
        req.getParameterMap().forEach((k, v) -> { if (v != null && v.length > 0) m.put(k, v[0]); });
        return m;
    }

    private OrderService.PaymentResult process(Map<String, String> p) {
        long amount;
        try { amount = Long.parseLong(p.getOrDefault("vnp_Amount", "0")); } catch (NumberFormatException e) { amount = -1; }
        boolean ok = "00".equals(p.get("vnp_ResponseCode")) && "00".equals(p.get("vnp_TransactionStatus"));
        return orderService.recordVnPay(p.get("vnp_TxnRef"), amount, ok, p.get("vnp_TransactionNo"));
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest req, RedirectAttributes ra) {
        Map<String, String> p = params(req);
        if (!vnPay.verify(p)) {
            ra.addFlashAttribute("error", "Không xác thực được kết quả thanh toán.");
            return "redirect:/order/history";
        }
        OrderService.PaymentResult r = process(p);
        if (r.order() == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng của giao dịch này.");
            return "redirect:/order/history";
        }
        if (r.success()) ra.addFlashAttribute("success", "Thanh toán thành công đơn " + r.order().getOrderCode() + ". Cảm ơn bạn!");
        else if ("24".equals(p.get("vnp_ResponseCode"))) ra.addFlashAttribute("error", "Bạn đã hủy thanh toán VNPay. Đơn vẫn được giữ, bạn có thể thanh toán lại hoặc hủy đơn trong 'Chi tiết đơn'.");
        else ra.addFlashAttribute("error", "Thanh toán chưa hoàn tất. Đơn vẫn được giữ, bạn có thể thanh toán lại trong 'Chi tiết đơn'.");
        return "redirect:/order/detail/" + r.order().getId();
    }

    @GetMapping("/vnpay-ipn")
    @ResponseBody
    public ResponseEntity<Map<String, String>> vnpayIpn(HttpServletRequest req) {
        Map<String, String> res = new LinkedHashMap<>();
        Map<String, String> p = params(req);
        if (!vnPay.verify(p)) {
            res.put("RspCode", "97"); res.put("Message", "Invalid signature");
            return ResponseEntity.ok(res);
        }
        OrderService.PaymentResult r = process(p);
        res.put("RspCode", r.rspCode());
        res.put("Message", switch (r.rspCode()) {
            case "00" -> "Confirm Success";
            case "01" -> "Order not found";
            case "02" -> "Order already confirmed";
            default -> "Invalid amount";
        });
        return ResponseEntity.ok(res);
    }

    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        if (!momo.verifyCallback(params)) {
            ra.addFlashAttribute("error", "Không xác thực được kết quả thanh toán MoMo.");
            return "redirect:/order/history";
        }
        OrderService.PaymentResult result = processMomo(params);
        if (result.order() == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng của giao dịch MoMo.");
            return "redirect:/order/history";
        }
        if (result.success()) {
            ra.addFlashAttribute("success", "Thanh toán MoMo thành công đơn "
                    + result.order().getOrderCode() + ". Cảm ơn bạn!");
        } else {
            ra.addFlashAttribute("error", "Thanh toán MoMo chưa hoàn tất. Bạn có thể thanh toán lại trong chi tiết đơn.");
        }
        return "redirect:/order/detail/" + result.order().getId();
    }

    @PostMapping("/momo-ipn")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> momoIpn(@RequestBody Map<String, Object> params) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (!momo.verifyCallback(params)) {
            response.put("message", "Invalid signature");
            response.put("resultCode", 97);
            return ResponseEntity.ok(response);
        }
        OrderService.PaymentResult result = processMomo(params);
        response.put("message", switch (result.rspCode()) {
            case "00", "02" -> "Confirm Success";
            case "01" -> "Order not found";
            case "04" -> "Invalid amount";
            default -> "Confirm failed";
        });
        response.put("resultCode", switch (result.rspCode()) {
            case "00", "02" -> 0;
            case "01" -> 1;
            case "04" -> 4;
            default -> 99;
        });
        return ResponseEntity.ok(response);
    }

    private OrderService.PaymentResult processMomo(Map<String, ?> params) {
        long amount;
        try {
            amount = Long.parseLong(String.valueOf(params.get("amount")));
        } catch (NumberFormatException | NullPointerException e) {
            amount = -1;
        }
        boolean success = "0".equals(String.valueOf(params.get("resultCode")));
        return orderService.recordMomo(
                String.valueOf(params.get("orderId")), amount, success, value(params.get("transId")));
    }

    private static String value(Object value) {
        return value == null ? null : value.toString();
    }
}