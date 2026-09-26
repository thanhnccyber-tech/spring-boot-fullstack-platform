package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.User;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping
    public String profile(Authentication auth, Model model) {
        User u = userService.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("user", u);
        return "user/profile";
    }

    @PostMapping("/update")
    public String update(Authentication auth,
                         @RequestParam String fullName,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) MultipartFile avatar,
                         RedirectAttributes ra) {
        String url = null;
        if (avatar != null && !avatar.isEmpty()) {
            url = cloudinaryService.upload(avatar, "avatars");
        }
        userService.updateProfile(auth.getName(), fullName, phone, address, url);
        ra.addFlashAttribute("success", "Cập nhật thành công!");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication auth,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 RedirectAttributes ra) {
        try {
            userService.changePassword(auth.getName(), oldPassword, newPassword);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}