package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.RoleName;
import vn.utetra.entity.User;
import vn.utetra.service.ReviewService;
import vn.utetra.service.UserService;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;

    /** Gửi / cập nhật đánh giá. Cần đăng nhập (xem SecurityConfig: /review/**). */
    @PostMapping("/{productId}")
    public String submit(@PathVariable Long productId,
                         @RequestParam(required = false) Integer rating,
                         @RequestParam(required = false) String comment,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            User u = userService.findByEmail(auth.getName()).orElseThrow();
            reviewService.submit(u, productId, rating, comment);
            ra.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("reviewError", e.getMessage());
        }
        return "redirect:/product/" + productId + "#reviews";
    }

    /** Xoá đánh giá (chủ đánh giá hoặc ADMIN). */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) Long productId,
                         Authentication auth,
                         RedirectAttributes ra) {
        boolean admin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleName.ADMIN::equals);
        try {
            Long pid = reviewService.delete(id, auth.getName(), admin);
            ra.addFlashAttribute("reviewSuccess", "Đã xoá đánh giá.");
            return "redirect:/product/" + pid + "#reviews";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("reviewError", e.getMessage());
            return productId != null ? "redirect:/product/" + productId + "#reviews" : "redirect:/products";
        }
    }
}
