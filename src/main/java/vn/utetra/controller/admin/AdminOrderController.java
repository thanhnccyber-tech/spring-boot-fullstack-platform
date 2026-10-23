package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Order;
import vn.utetra.entity.User;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.OrderService;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private static final int PAGE_SIZE = 15;

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                       @RequestParam(required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                       @RequestParam(defaultValue = "0") int page,
                       Authentication auth,
                       Model model) {

        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        Long branchIdFilter = me.isAdmin() ? null
                : (me.getBranch() == null ? -1L : me.getBranch().getId());

        Page<Order> pageData = orderService.searchForAdmin(
                kw, status, branchIdFilter, fromDate, toDate, page, PAGE_SIZE);

        Map<String, Long> statusCount = orderService.countByStatus(branchIdFilter);

        model.addAttribute("pageData", pageData);
        model.addAttribute("statusCount", statusCount);
        model.addAttribute("kw", kw);
        model.addAttribute("statusFilter", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("isAdmin", me.isAdmin());
        return "admin/orders";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order order = orderService.get(id);

        model.addAttribute("order", order);
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

    @PostMapping("/quick-status")
    public String quickStatus(@RequestParam Long id,
                              @RequestParam String status,
                              @RequestParam(required = false) String returnUrl,
                              RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, status);
            ra.addFlashAttribute("success", "Đã chuyển trạng thái");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + (returnUrl != null && !returnUrl.isBlank()
                ? returnUrl : "/admin/orders");
    }

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