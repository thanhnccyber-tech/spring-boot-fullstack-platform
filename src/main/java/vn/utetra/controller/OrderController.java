package vn.utetra.controller;

import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.dto.CartItem;
import vn.utetra.dto.OrderRequest;
import vn.utetra.entity.Order;
import vn.utetra.service.BranchService;
import vn.utetra.service.OrderService;

import org.springframework.http.ResponseEntity;
import vn.utetra.entity.User;
import vn.utetra.entity.Voucher;
import vn.utetra.service.UserService;
import vn.utetra.service.VoucherService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private BranchService branchService;
    @Autowired private UserService userService;
    @Autowired private VoucherService voucherService;

    @SuppressWarnings("unchecked")
    private static List<CartItem> cartOf(HttpSession session) {
        Object o = session.getAttribute("CART");
        return o == null ? List.of() : (List<CartItem>) o;
    }

    private static BigDecimal subtotalOf(List<CartItem> cart) {
        return cart.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Authentication auth, Model model, RedirectAttributes ra) {
        List<CartItem> cart = cartOf(session);
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống, hãy chọn món trước nhé.");
            return "redirect:/products";
        }
        BigDecimal subtotal = subtotalOf(cart);
        User me = userService.findByEmail(auth.getName()).orElseThrow();

        // Ưu tiên lựa chọn khách đã chọn ở popup "Chọn phương thức nhận hàng" trên header
        String method = (String) session.getAttribute("RECV_METHOD");
        if (!Order.METHOD_PICKUP.equals(method)) method = Order.METHOD_DELIVERY;
        Long branchId = (Long) session.getAttribute("RECV_BRANCH");
        String addr = (String) session.getAttribute("RECV_ADDR");
        if (addr == null || addr.isBlank()) addr = me.getAddress();

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipFeeDelivery", orderService.shippingFeeFor(Order.METHOD_DELIVERY, subtotal));
        model.addAttribute("shipFee", orderService.shippingFeeFor(method, subtotal));
        model.addAttribute("freeShipFrom", orderService.getFreeShipFrom());
        model.addAttribute("branches", branchService.active());
        model.addAttribute("recvMethod", method);
        model.addAttribute("recvBranchId", branchId);
        model.addAttribute("recvAddress", addr);
        model.addAttribute("userPhone", me.getPhone());
        return "user/checkout";
    }

    /** Kiểm tra mã giảm giá (AJAX) trước khi đặt hàng. Số tiền thật vẫn được server tính lại lúc đặt. */
    @GetMapping("/voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkVoucher(@RequestParam String code,
                                                            @RequestParam(defaultValue = "DELIVERY") String method,
                                                            HttpSession session) {
        Map<String, Object> res = new LinkedHashMap<>();
        BigDecimal subtotal = subtotalOf(cartOf(session));
        try {
            Voucher v = voucherService.validate(code, subtotal);
            BigDecimal discount = voucherService.discountOf(v, subtotal);
            BigDecimal fee = orderService.shippingFeeFor(method, subtotal);
            res.put("valid", true);
            res.put("code", v.getCode());
            res.put("discount", discount);
            res.put("total", subtotal.subtract(discount).add(fee));
            res.put("message", "Đã áp dụng mã " + v.getCode() + ": giảm "
                    + String.format("%,d", discount.longValue()).replace(',', '.') + "đ");
        } catch (IllegalArgumentException e) {
            res.put("valid", false);
            res.put("discount", 0);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/place")
    public String place(@ModelAttribute OrderRequest req,
                        HttpSession session,
                        Authentication auth,
                        RedirectAttributes ra) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("CART");
        if (cart == null || cart.isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        req.setItems(cart);
        Order o;
        try {
            o = orderService.placeOrder(auth.getName(), req);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/order/checkout";
        }
        session.removeAttribute("CART");
        ra.addFlashAttribute("success", "Đặt hàng thành công! Mã: " + o.getOrderCode());
        return "redirect:/order/history";
    }

    @GetMapping("/history")
    public String history(Authentication auth, Model model) {
        model.addAttribute("orders", orderService.ordersOf(auth.getName()));
        return "user/order-history";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        Order order = orderService.get(id);
        // Chống IDOR: khách chỉ được xem đơn của chính mình
        if (order.getUser() == null || !order.getUser().getEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Không có quyền xem đơn hàng này");
        }
        model.addAttribute("order", order);
        return "user/order-detail";
    }
}