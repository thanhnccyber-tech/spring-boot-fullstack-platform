package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Promotion;
import vn.utetra.security.ErrorMessages;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.FileValidator;
import vn.utetra.service.PromotionService;

import java.util.NoSuchElementException;

/**
 * Quản lý tin tức & khuyến mãi.
 * - ADMIN : CRUD đầy đủ.
 * - MANAGER: chỉ xem danh sách (mutate bị chặn @PreAuthorize(ADMIN)).
 */
@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {

    @Autowired private PromotionService promotionService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FileValidator fileValidator;

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Authentication auth, Model model) {
        Promotion form = new Promotion();
        if (edit != null && isAdmin(auth)) {
            try { form = promotionService.get(edit); } catch (NoSuchElementException ignored) { }
        }
        model.addAttribute("promotions", promotionService.all());
        model.addAttribute("form", form);
        model.addAttribute("isAdmin", isAdmin(auth));
        return "admin/promotions";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam String title,
                       @RequestParam(required = false) String summary,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String imageUrl,
                       @RequestParam(required = false) String voucherCode,
                       @RequestParam(required = false) MultipartFile imageFile,
                       @RequestParam(defaultValue = "false") boolean active,
                       RedirectAttributes ra) {
        try {
            String img = imageUrl;
            if (imageFile != null && !imageFile.isEmpty()) {
                fileValidator.validateImage(imageFile);
                img = cloudinaryService.upload(imageFile, "promotions");
            }
            Promotion p = id == null ? new Promotion() : promotionService.get(id);
            promotionService.save(p, title, summary, content, img, voucherCode, active);
            ra.addFlashAttribute("success", id == null ? "Đã đăng bài mới." : "Đã cập nhật bài viết.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggle(@RequestParam Long id, RedirectAttributes ra) {
        promotionService.toggle(id);
        ra.addFlashAttribute("success", "Đã cập nhật hiển thị bài viết.");
        return "redirect:/admin/promotions";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        promotionService.delete(id);
        ra.addFlashAttribute("success", "Đã xoá bài viết.");
        return "redirect:/admin/promotions";
    }
}