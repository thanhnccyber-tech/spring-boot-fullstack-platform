package vn.utetra.controller.admin;

import vn.utetra.security.ErrorMessages;
import org.springframework.security.access.AccessDeniedException;
import vn.utetra.security.SafeRedirect;
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

    /** Đơn mới nhất cho khung chuông thông báo (Admin: mọi chi nhánh; Manager/Staff: chỉ chi nhánh của mình). */
    @GetMapping("/recent")
    @ResponseBody
    public java.util.List<Map<String, Object>> recent(Authentication auth) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        Long branchIdFilter = me.isAdmin() ? null
                : (me.getBranch() == null ? -1L : me.getBranch().getId());
        java.util.List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (Order o : orderService.searchForAdmin(null, null, branchIdFilter, null, null, 0, 10).getContent()) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("code", o.getOrderCode());
            m.put("customer", o.getUser() == null || o.getUser().getFullName() == null ? "Khách" : o.getUser().getFullName());
            m.put("total", o.getTotalAmount());
            m.put("status", o.getStatus());
            m.put("statusLabel", o.getStatusLabel());
            m.put("payment", o.getPaymentMethod());
            m.put("paid", "PAID".equals(o.getPaymentStatus()));
            m.put("createdAt", o.getCreatedAt() == null ? null : o.getCreatedAt().toString());
            out.add(m);
        }
        return out;
    }

    /** Admin xem mọi chi nhánh; Manager/Staff chỉ đơn thuộc chi nhánh của mình (chống truy cập chéo chi nhánh). */
    private Order requireAccess(Long orderId, Authentication auth) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order order = orderService.get(orderId);
        if (!me.isAdmin()) {
            Long myBranch = me.getBranch() == null ? null : me.getBranch().getId();
            Long orderBranch = order.getBranch() == null ? null : order.getBranch().getId();
            if (myBranch == null || !myBranch.equals(orderBranch)) {
                throw new AccessDeniedException("Đơn hàng không thuộc chi nhánh của bạn");
            }
        }
        return order;
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        User me = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order order = requireAccess(id, auth);

        model.addAttribute("order", order);
        Long branchId = order.getBranch() != null ? order.getBranch().getId() : null;
        model.addAttribute("shippers", userRepo.findActiveShippers(branchId));
        model.addAttribute("isAdmin", me.isAdmin());
        return "admin/order-detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long id,
                               @RequestParam String status,
                               Authentication auth,
                               RedirectAttributes ra) {
        requireAccess(id, auth);
        try {
            orderService.updateStatus(id, status);
            ra.addFlashAttribute("success", "Cập nhật trạng thái thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/admin/orders/detail/" + id;
    }

    @PostMapping("/quick-status")
    public String quickStatus(@RequestParam Long id,
                              @RequestParam String status,
                              @RequestParam(required = false) String returnUrl,
                              Authentication auth,
                              RedirectAttributes ra) {
        requireAccess(id, auth);
        try {
            orderService.updateStatus(id, status);
            ra.addFlashAttribute("success", "Đã chuyển trạng thái");
        } catch (Exception e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/orders");
    }

    @PostMapping("/assign-shipper")
    public String assignShipper(@RequestParam Long id,
                                @RequestParam(required = false) Long shipperId,
                                Authentication auth,
                                RedirectAttributes ra) {
        requireAccess(id, auth);
        try {
            orderService.assignShipper(id, shipperId);
            ra.addFlashAttribute("success",
                    shipperId != null ? "Đã phân công shipper" : "Đã bỏ phân công shipper");
        } catch (Exception e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/admin/orders/detail/" + id;
    }
}