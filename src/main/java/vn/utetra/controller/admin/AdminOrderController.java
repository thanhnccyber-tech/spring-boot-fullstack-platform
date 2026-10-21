package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.User;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public String list(Authentication auth, Model model) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        var orders = me.isAdmin()
                ? orderService.allWithUser()
                : orderService.ordersForBranch(
                        me.getBranch() == null ? -1L : me.getBranch().getId());
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.get(id));
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
}