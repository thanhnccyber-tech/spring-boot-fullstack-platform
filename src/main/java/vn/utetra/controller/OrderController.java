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

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private BranchService branchService;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("branches", branchService.active());
        return "user/checkout";
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