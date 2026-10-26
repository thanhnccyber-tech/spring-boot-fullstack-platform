package vn.utetra.controller;

import org.springframework.security.access.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
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

import org.springframework.http.ResponseEntity;
import vn.utetra.entity.User;
import vn.utetra.entity.Voucher;
import vn.utetra.service.UserService;
import vn.utetra.service.VoucherService;
import vn.utetra.service.VnPayService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired private OrderService orderService;
    @Autowired private BranchService branchService;
    @Autowired private UserService userService;
    @Autowired private VoucherService voucherService;
    @Autowired private VnPayService vnPayService;
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
                        HttpServletRequest servletRequest,
                        Authentication auth,
                        RedirectAttributes ra) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("CART");
        if (cart == null || cart.isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        if (auth == null || auth.getName() == null) {
            ra.addFlashAttribute("error", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            return "redirect:/login?next=%2Forder%2Fcheckout";
        }

        req.setItems(cart);
        if (req.getLat() == null) req.setLat((Double) session.getAttribute("RECV_LAT"));
        if (req.getLng() == null) req.setLng((Double) session.getAttribute("RECV_LNG"));
        Order o;
        log.info("Bắt đầu tạo đơn hàng: paymentMethod={}, itemCount={}",
                req.getPaymentMethod(), cart.size());
        try {
            o = orderService.placeOrder(auth.getName(), req);
        } catch (IllegalArgumentException e) {
            log.warn("Không thể tạo đơn hàng: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/order/checkout";
        } catch (RuntimeException e) {
            log.error("Lỗi khi tạo đơn hàng (paymentMethod={})", req.getPaymentMethod(), e);
            ra.addFlashAttribute("error", "Hệ thống chưa xác nhận được đơn hàng. Vui lòng kiểm tra mục Đơn hàng trước khi thử lại.");
            return "redirect:/order/history";
        }

        log.info("Đã tạo đơn hàng id={}, code={}, paymentMethod={}",
                o.getId(), o.getOrderCode(), o.getPaymentMethod());
        session.removeAttribute("CART");
        if ("VNPAY".equals(o.getPaymentMethod())) {
            // Chuyển thẳng sang cổng VNPay trong response của POST /order/place,
            // tránh thêm một request nội bộ /payment/vnpay/{id} ngay sau khi đặt hàng.
            try {
                String ip = VnPayService.clientIp(servletRequest);
                String paymentUrl = vnPayService.buildPaymentUrl(o, ip);
                log.info("Chuyển đơn hàng id={} sang cổng VNPay", o.getId());
                return "redirect:" + paymentUrl;
            } catch (RuntimeException e) {
                log.error("Đơn hàng id={} đã tạo nhưng không tạo được URL VNPay", o.getId(), e);
                ra.addFlashAttribute("error", "Đơn hàng đã được tạo nhưng chưa mở được VNPay. Bạn có thể thử thanh toán lại trong chi tiết đơn hàng.");
                return "redirect:/order/detail/" + o.getId();
            }
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
        List<CartItem> cart = (List<CartItem>) session.getAttribute("CART");
        if (cart == null) { cart = new java.util.ArrayList<>(); session.setAttribute("CART", cart); }
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