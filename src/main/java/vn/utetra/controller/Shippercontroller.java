package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.security.ErrorMessages;
import vn.utetra.service.ShipperService;

/**
 * Màn hình dành cho Shipper (layout riêng: shipper-layout.jsp).
 * Quyền truy cập /shipper/** được khai báo trong SecurityConfig; ShipperService kiểm tra thêm
 * rằng người dùng đúng là shipper và chỉ thao tác được đơn của chi nhánh / của chính mình.
 */
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired private ShipperService shipperService;

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/shipper/dashboard";
    }

    /** Trang chính: thống kê hôm nay, đơn đang giao và đơn chờ nhận tại chi nhánh. */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        model.addAttribute("hasBranch", shipperService.hasBranch(email));
        model.addAttribute("available", shipperService.available(email));
        model.addAttribute("delivering", shipperService.delivering(email));
        model.addAttribute("today", shipperService.statsToday(email));
        return "shipper/dashboard";
    }

    /** Nhận đơn: đơn chuyển sang "Đang giao" và khách được báo realtime. */
    @PostMapping("/orders/{id}/accept")
    public String accept(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            shipperService.accept(id, auth.getName());
            ra.addFlashAttribute("success", "Đã nhận đơn. Chúc bạn giao hàng thuận lợi!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/shipper/dashboard";
    }
}