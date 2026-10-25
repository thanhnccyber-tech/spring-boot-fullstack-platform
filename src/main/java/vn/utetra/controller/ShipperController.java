package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /** Chi tiết đơn: danh sách món, thông tin khách, các nút nhận / giao xong / giao thất bại. */
    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("order", shipperService.detail(id, auth.getName()));
        return "shipper/order-detail";
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

    /** Giao thành công: đơn hoàn thành, khách được báo realtime, COD được ghi nhận đã thu tiền. */
    @PostMapping("/orders/{id}/complete")
    public String complete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            shipperService.complete(id, auth.getName());
            ra.addFlashAttribute("success", "Đã ghi nhận giao thành công. Cảm ơn bạn!");
            return "redirect:/shipper/dashboard";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
            return "redirect:/shipper/orders/" + id;
        }
    }

    /** Giao thất bại: bắt buộc nhập lý do (khách không nghe máy, sai địa chỉ...). */
    @PostMapping("/orders/{id}/fail")
    public String fail(@PathVariable Long id, @RequestParam(required = false) String reason,
                       Authentication auth, RedirectAttributes ra) {
        try {
            shipperService.fail(id, auth.getName(), reason);
            ra.addFlashAttribute("success", "Đã ghi nhận giao thất bại. Cửa hàng sẽ được thông báo.");
            return "redirect:/shipper/dashboard";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
            return "redirect:/shipper/orders/" + id;
        }
    }
}