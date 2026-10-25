package vn.utetra.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Order;
import vn.utetra.service.OrderService;
import vn.utetra.service.VnPayService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thanh toán online qua VNPay.
 *  - /payment/vnpay/{id}      : khách (đã đăng nhập) được chuyển sang cổng thanh toán
 *  - /payment/vnpay-return    : VNPay đưa khách quay lại (công khai, bảo vệ bằng chữ ký)
 *  - /payment/vnpay-ipn       : VNPay báo kết quả server-to-server (công khai, bảo vệ bằng chữ ký)
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired private OrderService orderService;
    @Autowired private VnPayService vnPay;

    @GetMapping("/vnpay/{id}")
    public String pay(@PathVariable Long id, Authentication auth, HttpServletRequest req, RedirectAttributes ra) {
        Order o = orderService.ownedOrder(id, auth.getName());
        if (!vnPay.enabled() || !"VNPAY".equals(o.getPaymentMethod()) || !o.isAwaitingOnlinePayment()) {
            ra.addFlashAttribute("error", "Đơn này không cần thanh toán online hoặc đã thanh toán.");
            return "redirect:/order/detail/" + id;
        }
        return "redirect:" + vnPay.buildPaymentUrl(o, req.getRemoteAddr());
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
}
