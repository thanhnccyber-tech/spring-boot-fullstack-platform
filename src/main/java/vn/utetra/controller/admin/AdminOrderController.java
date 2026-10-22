package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Order;
import vn.utetra.entity.User;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       Authentication auth, Model model) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();

        List<Order> orders = me.isAdmin()
                ? orderService.allWithUser()
                : orderService.ordersForBranch(
                        me.getBranch() == null ? -1L : me.getBranch().getId());

        // Lọc theo trạng thái
        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(o -> status.equals(o.getStatus()))
                    .toList();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", status);
        return "admin/orders";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order order = orderService.get(id);

        model.addAttribute("order", order);
        // Danh sách shipper để phân công (chỉ shipper cùng chi nhánh với đơn)
        Long branchId = order.getBranch() != null ? order.getBranch().getId() : null;
        model.addAttribute("shippers", userRepo.findActiveShippers(branchId));
        model.addAttribute("isAdmin", me.isAdmin());
        return "admin/order-detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        orderService.updateStatus(id, status);
        ra.addFlashAttribute("success", "Cập nhật trạng thái thành công");
        return "redirect:/admin/orders/detail/" + id;
    }

    /**
     * Chuyển trạng thái nhanh từ bảng danh sách.
     */
    @PostMapping("/quick-status")
    public String quickStatus(@RequestParam Long id,
                              @RequestParam String status,
                              @RequestParam(required = false) String returnUrl,
                              RedirectAttributes ra) {
        orderService.updateStatus(id, status);
        ra.addFlashAttribute("success", "Đã chuyển trạng thái");
        return "redirect:" + (returnUrl != null && !returnUrl.isBlank()
                ? returnUrl : "/admin/orders");
    }

    /**
     * Admin/Manager phân công shipper cho đơn hàng.
     */
    @PostMapping("/assign-shipper")
    public String assignShipper(@RequestParam Long id,
                                @RequestParam(required = false) Long shipperId,
                                RedirectAttributes ra) {
        try {
            orderService.assignShipper(id, shipperId);
            ra.addFlashAttribute("success",
                    shipperId != null ? "Đã phân công shipper" : "Đã bỏ phân công shipper");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/detail/" + id;
    }
}