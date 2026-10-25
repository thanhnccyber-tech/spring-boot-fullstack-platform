package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Voucher;
import vn.utetra.security.ErrorMessages;
import vn.utetra.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/** Quản lý mã giảm giá (chỉ ADMIN, xem SecurityConfig). */
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @Autowired private VoucherService voucherService;

    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        Voucher form = new Voucher();
        if (edit != null) {
            try { form = voucherService.get(edit); } catch (NoSuchElementException ignored) { }
        }
        model.addAttribute("vouchers", voucherService.all());
        model.addAttribute("form", form);
        return "admin/vouchers";
    }

    @PostMapping("/save")
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
            voucherService.save(v, code, discountType, discountValue, minOrderAmount,
                    maxDiscountAmount, exp, usageLimit, active, description);
            ra.addFlashAttribute("success", id == null ? "Đã tạo mã giảm giá." : "Đã cập nhật mã giảm giá.");
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("error", "Ngày hết hạn không hợp lệ.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle")
    public String toggle(@RequestParam Long id, RedirectAttributes ra) {
        voucherService.toggle(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái mã giảm giá.");
        return "redirect:/admin/vouchers";
    }
}
