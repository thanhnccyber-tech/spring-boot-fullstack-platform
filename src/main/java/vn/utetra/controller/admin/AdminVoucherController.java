package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Notification;
import vn.utetra.entity.RoleName;
import vn.utetra.entity.User;
import vn.utetra.entity.Voucher;
import vn.utetra.repository.NotificationRepository;
import vn.utetra.repository.UserRepository;
import vn.utetra.security.ErrorMessages;
import vn.utetra.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * Quản lý mã giảm giá.
 * - ADMIN : CRUD đầy đủ.
 * - MANAGER: chỉ xem danh sách (mutate bị chặn @PreAuthorize(ADMIN)).
 */
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @Autowired private VoucherService voucherService;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void notifyCustomersAboutVoucher(Voucher voucher) {
        if (voucher == null || !Boolean.TRUE.equals(voucher.getActive())) return;
        String discount = voucher.isPercent()
                ? "giảm " + voucher.getDiscountValue().stripTrailingZeros().toPlainString() + "%"
                : "giảm " + String.format("%,d", voucher.getDiscountValue().longValue()).replace(',', '.') + "đ";
        String content = "UTeTra vừa phát hành mã " + voucher.getCode() + " (" + discount + "). ";
        if (voucher.getDescription() != null && !voucher.getDescription().isBlank()) content += voucher.getDescription().trim() + " ";
        if (voucher.getMinOrderAmount() != null && voucher.getMinOrderAmount().signum() > 0)
            content += "Đơn tối thiểu " + String.format("%,d", voucher.getMinOrderAmount().longValue()).replace(',', '.') + "đ. ";
        if (voucher.getExpiresAt() != null) content += "Hạn sử dụng đến " + voucher.getExpiresAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ". ";
        content += "Nhập mã tại bước thanh toán để sử dụng ưu đãi.";
        for (User user : userRepository.findAll()) {
            if (!Boolean.TRUE.equals(user.getEnabled()) || !user.hasRole(RoleName.USER)
                    || user.isAdmin() || user.isManager() || user.isStaff() || user.isShipper()) continue;
            Notification n = new Notification();
            n.setUserId(user.getId());
            n.setTitle("Voucher mới: " + voucher.getCode());
            n.setContent(content);
            n.setType("VOUCHER");
            n.setIsRead(false);
            notificationRepository.save(n);
        }
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Authentication auth, Model model) {
        Voucher form = new Voucher();
        if (edit != null && isAdmin(auth)) {
            try { form = voucherService.get(edit); } catch (NoSuchElementException ignored) { }
        }
        model.addAttribute("vouchers", voucherService.all());
        model.addAttribute("form", form);
        model.addAttribute("isAdmin", isAdmin(auth));
        return "admin/vouchers";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam String code,
                       @RequestParam String discountType,
                       @RequestParam BigDecimal discountValue,
                       @RequestParam(required = false) BigDecimal minOrderAmount,
                       @RequestParam(required = false) BigDecimal maxDiscountAmount,
                       @RequestParam(required = false) String expiresAt,
                       @RequestParam(required = false) Integer usageLimit,
                       @RequestParam(required = false) String description,
                       @RequestParam(defaultValue = "false") boolean active,
                       RedirectAttributes ra) {
        try {
            LocalDateTime exp = (expiresAt == null || expiresAt.isBlank()) ? null : LocalDateTime.parse(expiresAt);
            Voucher v = id == null ? new Voucher() : voucherService.get(id);
            boolean wasActive = Boolean.TRUE.equals(v.getActive());
            voucherService.save(v, code, discountType, discountValue, minOrderAmount,
                    maxDiscountAmount, exp, usageLimit, active, description);
            if (active && (id == null || !wasActive)) notifyCustomersAboutVoucher(v);
            ra.addFlashAttribute("success", id == null ? "Đã tạo mã giảm giá." : "Đã cập nhật mã giảm giá.");
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("error", "Ngày hết hạn không hợp lệ.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggle(@RequestParam Long id, RedirectAttributes ra) {
        Voucher before = voucherService.get(id);
        boolean wasActive = Boolean.TRUE.equals(before.getActive());
        voucherService.toggle(id);
        Voucher after = voucherService.get(id);
        if (!wasActive && Boolean.TRUE.equals(after.getActive())) notifyCustomersAboutVoucher(after);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái mã giảm giá.");
        return "redirect:/admin/vouchers";
    }
}