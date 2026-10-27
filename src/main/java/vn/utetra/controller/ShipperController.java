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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Order;
import vn.utetra.repository.UserRepository;
import vn.utetra.security.ErrorMessages;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.FileValidator;
import vn.utetra.service.ShipperService;
import vn.utetra.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình dành cho Shipper (layout riêng: shipper-layout.jsp).
 * Quyền truy cập /shipper/** được khai báo trong SecurityConfig; ShipperService kiểm tra thêm
 * rằng người dùng đúng là shipper và chỉ thao tác được đơn của chi nhánh / của chính mình.
 */
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    /** Lịch sử chỉ hiển thị số đơn gần nhất này để trang không quá nặng. */
    private static final int HISTORY_LIMIT = 100;
    private static final int CHART_DAYS = 7;

    @Autowired private ShipperService shipperService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;   // dùng findByEmailWithBranch cho trang profile
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FileValidator fileValidator;

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

    /** Lịch sử giao hàng: đơn đã giao xong và đơn giao thất bại, mới nhất trước. */
    @GetMapping("/history")
    public String history(Authentication auth, Model model) {
        List<Order> all = shipperService.history(auth.getName());
        model.addAttribute("orders", all.size() > HISTORY_LIMIT ? all.subList(0, HISTORY_LIMIT) : all);
        model.addAttribute("total", all.size());
        List<vn.utetra.entity.DeliveryFailure> failures = shipperService.failureHistory(auth.getName());
        model.addAttribute("failures",
                failures.size() > HISTORY_LIMIT ? failures.subList(0, HISTORY_LIMIT) : failures);
        model.addAttribute("failureTotal", failures.size());
        model.addAttribute("limit", HISTORY_LIMIT);
        return "shipper/history";
    }

    /** Thống kê đơn đã giao: hôm nay / tuần này / tháng này và biểu đồ 7 ngày gần nhất. */
    @GetMapping("/stats")
    public String stats(Authentication auth, Model model) {
        String email = auth.getName();

        Map<String, ShipperService.Stats> periods = new LinkedHashMap<>();
        periods.put("Hôm nay", shipperService.statsToday(email));
        periods.put("Tuần này", shipperService.statsThisWeek(email));
        periods.put("Tháng này", shipperService.statsThisMonth(email));
        model.addAttribute("periods", periods);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> e : shipperService.deliveredPerDay(email, CHART_DAYS).entrySet()) {
            labels.add(e.getKey().format(fmt));
            values.add(e.getValue());
        }
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);
        return "shipper/stats";
    }

    /** Chi tiết đơn: danh sách món, thông tin khách, các nút nhận / giao xong / giao thất bại. */
    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("order", shipperService.detail(id, auth.getName()));
        model.addAttribute("deliveryFailures", shipperService.failureHistoryForOrder(id, auth.getName()));
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
            ra.addFlashAttribute("success", "Đã ghi nhận lần giao thất bại. Đơn đang chờ được phân công giao lại.");
            return "redirect:/shipper/dashboard";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
            return "redirect:/shipper/orders/" + id;
        }
    }

    /* ==================== HỒ SƠ CÁ NHÂN (riêng cho shipper) ==================== */

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        // findByEmailWithBranch dùng LEFT JOIN FETCH u.branch để load sẵn branch ngay trong
        // transaction của repository — tránh LazyInitializationException khi JSP đọc me.branch.name
        // do spring.jpa.open-in-view=false.
        model.addAttribute("me", userRepository.findByEmailWithBranch(auth.getName()).orElseThrow());
        return "shipper/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) MultipartFile avatar,
                                RedirectAttributes ra) {
        try {
            if (avatar != null && !avatar.isEmpty()) fileValidator.validateImage(avatar);
            String url = (avatar != null && !avatar.isEmpty())
                    ? cloudinaryService.upload(avatar, "avatars") : null;
            userService.updateProfile(auth.getName(), fullName, phone, address, url);
            ra.addFlashAttribute("success", "Đã cập nhật thông tin cá nhân.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/shipper/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication auth,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
            }
            userService.changePassword(auth.getName(), oldPassword, newPassword);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/shipper/profile";
    }
}