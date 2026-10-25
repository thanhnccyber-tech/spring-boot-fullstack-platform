package vn.utetra.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.utetra.entity.User;
import vn.utetra.service.BranchService;
import vn.utetra.service.OrderService;
import vn.utetra.service.PromotionService;
import vn.utetra.service.UserService;

import java.util.NoSuchElementException;

/** Các trang công khai kiểu website thương hiệu: Khuyến mãi, Hội viên, Cửa hàng. */
@Controller
public class PublicController {

    @Autowired private PromotionService promotionService;
    @Autowired private BranchService branchService;
    @Autowired private UserService userService;

    @GetMapping("/khuyen-mai")
    public String promotions(Model model) {
        model.addAttribute("promotions", promotionService.active());
        return "user/promotions";
    }

    @GetMapping("/khuyen-mai/{id}")
    public String promotionDetail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("promo", promotionService.view(id));
        } catch (NoSuchElementException e) {
            return "redirect:/khuyen-mai";
        }
        return "user/promotion-detail";
    }

    @GetMapping("/cua-hang")
    public String stores(Model model) {
        model.addAttribute("branches", branchService.active());
        return "user/stores";
    }

    /** Trang Hội viên công khai: câu hỏi thường gặp + điều khoản (ai cũng xem được).
     *  Thẻ hội viên, điểm và hạng nằm ở Tài khoản > Khách hàng thành viên (/account/member). */
    @GetMapping("/hoi-vien")
    public String membership(Model model) {
        model.addAttribute("pointUnit", OrderService.POINT_UNIT);
        return "user/membership";
    }
}
