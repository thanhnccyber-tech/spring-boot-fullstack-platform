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
import vn.utetra.entity.Branch;
import vn.utetra.entity.Order;
import vn.utetra.service.BranchService;
import vn.utetra.service.OrderService;
import vn.utetra.service.PersistentCartService;

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
    @Autowired private PersistentCartService persistentCartService;
    @Autowired private BranchService branchService;
    @Autowired private UserService userService;
    @Autowired private VoucherService voucherService;
    @Autowired private vn.utetra.repository.UserAddressRepository addressRepo;

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
        User me = userService.findByEmail(auth.getName()).orElseThrow();
        List<CartItem> cart = persistentCartService.load(me.getId());
        session.setAttribute("CART", cart);
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống, hãy chọn món trước nhé.");
            return "redirect:/products";
        }
        BigDecimal subtotal = subtotalOf(cart);

        // Ưu tiên lựa chọn khách đã chọn ở popup "Chọn phương thức nhận hàng" trên header
        String method = (String) session.getAttribute("RECV_METHOD");
        if (!Order.METHOD_PICKUP.equals(method)) method = Order.METHOD_DELIVERY;
        Long branchId = (Long) session.getAttribute("RECV_BRANCH");
        String addr = (String) session.getAttribute("RECV_ADDR");
        if (addr == null || addr.isBlank()) addr = me.getAddress();

        Double lat = (Double) session.getAttribute("RECV_LAT");
        Double lng = (Double) session.getAttribute("RECV_LNG");
        List<Branch> branches = branchService.active();
        // Phí ship giao tận nơi theo từng cửa hàng (tính theo khoảng cách nếu biết vị trí khách)
        Map<Long, Long> branchFee = new LinkedHashMap<>();
        for (Branch b : branches) {
            branchFee.put(b.getId(), orderService.shippingFeeFor(Order.METHOD_DELIVERY, subtotal,
                    orderService.distanceKm(b, lat, lng)).longValue());
        }
        Branch sel = branches.stream().filter(b -> b.getId().equals(branchId)).findFirst()
                .orElse(branches.isEmpty() ? null : branches.get(0));
        BigDecimal feeDelivery = sel == null ? orderService.shippingFeeFor(Order.METHOD_DELIVERY, subtotal)
                : BigDecimal.valueOf(branchFee.get(sel.getId()));

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipFeeDelivery", feeDelivery);
        model.addAttribute("branchFee", branchFee);
        model.addAttribute("recvLat", lat);
        model.addAttribute("recvLng", lng);
        model.addAttribute("userPoints", me.getPoints());
        model.addAttribute("pointValue", orderService.getPointValue());
        model.addAttribute("vnpayEnabled", orderService.isOnlinePaymentEnabled());
        model.addAttribute("shipFee", orderService.shippingFeeFor(method, subtotal));
        model.addAttribute("freeShipFrom", orderService.getFreeShipFrom());
        model.addAttribute("branches", branches);
        model.addAttribute("recvMethod", method);
        model.addAttribute("recvBranchId", branchId);
        model.addAttribute("recvAddress", addr);
        model.addAttribute("userPhone", me.getPhone());
        model.addAttribute("savedAddresses", addressRepo.findByUserIdOrderByDefaultAddressDescIdAsc(me.getId()));
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
        List<CartItem> cart = persistentCartService.load(
                userService.findByEmail(auth.getName()).orElseThrow().getId());
        session.setAttribute("CART", cart);
        if (cart == null || cart.isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        req.setItems(cart);
        if (req.getLat() == null) req.setLat((Double) session.getAttribute("RECV_LAT"));
        if (req.getLng() == null) req.setLng((Double) session.getAttribute("RECV_LNG"));
        Order o;
        try {
            o = orderService.placeOrder(auth.getName(), req);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/order/checkout";
        }
        session.removeAttribute("CART");
        persistentCartService.clear(o.getUser().getId());
        if ("VNPAY".equals(o.getPaymentMethod())) {
            // Chuyển sang cổng VNPay để trả tiền; chưa trả vẫn vào lại được từ trang chi tiết đơn
            return "redirect:/payment/vnpay/" + o.getId();
        }
        ra.addFlashAttribute("success", "Đặt hàng thành công! Mã: " + o.getOrderCode()
                + ". Bạn có thể theo dõi tiến trình ngay tại đây.");
        return "redirect:/order/detail/" + o.getId();
    }

    /** Khách tự hủy đơn khi cửa hàng chưa xác nhận. */
    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            orderService.cancelByCustomer(id, auth.getName());
            ra.addFlashAttribute("success", "Đã hủy đơn hàng. Mã giảm giá và điểm (nếu có) đã được hoàn lại.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail/" + id;
    }

    /** "Đặt lại": đưa các món của đơn cũ vào giỏ (giá hiện tại), rồi sang giỏ hàng để kiểm tra. */
    @PostMapping("/reorder/{id}")
    @SuppressWarnings("unchecked")
    public String reorder(@PathVariable Long id, HttpSession session, Authentication auth, RedirectAttributes ra) {
        OrderService.ReorderResult r = orderService.reorderItems(id, auth.getName());
        User user = userService.findByEmail(auth.getName()).orElseThrow();
        List<CartItem> cart = persistentCartService.load(user.getId());
        int added = 0;
        for (CartItem ci : r.items()) {
            if (cart.size() >= vn.utetra.service.CartService.MAX_CART_ITEMS) break;
            cart.add(ci);
            added++;
        }
        if (added == 0) {
            ra.addFlashAttribute("error", "Các món trong đơn này hiện không còn bán.");
            return "redirect:/order/history";
        }
        persistentCartService.save(user.getId(), cart);
        session.setAttribute("CART", cart);
        ra.addFlashAttribute("success", "Đã thêm " + added + " món vào giỏ"
                + (r.skipped() > 0 ? " (" + r.skipped() + " món không còn bán đã được bỏ qua)" : "") + ".");
        return "redirect:/cart";
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