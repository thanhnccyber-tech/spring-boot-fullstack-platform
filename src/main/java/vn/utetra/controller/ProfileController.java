package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.utetra.entity.RoleName;
import vn.utetra.entity.User;
import vn.utetra.security.ErrorMessages;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.FileValidator;
import vn.utetra.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FileValidator fileValidator;

    @GetMapping
    public String profile(Authentication auth, Model model) {
        User u = userService.findByEmail(auth.getName()).orElseThrow();
        // Shipper cũng là khách hàng: /profile luôn là hồ sơ khách hàng (điểm, hạng, địa chỉ nhận hàng).
        // Hồ sơ riêng của shipper nằm ở /shipper/profile và có link trong menu tài khoản.
        model.addAttribute("user", u);
        return "user/profile";
    }

    @PostMapping("/update")
    public String update(Authentication auth,
                         @RequestParam String fullName,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) MultipartFile avatar,
                         Model model) {
        try {
            if (avatar != null && !avatar.isEmpty()) {
                fileValidator.validateImage(avatar);
            }
            String url = null;
            if (avatar != null && !avatar.isEmpty()) {
                url = cloudinaryService.upload(avatar, "avatars");
            }
            userService.updateProfile(auth.getName(), fullName, phone, address, url);
            User u = userService.findByEmail(auth.getName()).orElseThrow();
            model.addAttribute("user", u);
            model.addAttribute("success", "Cập nhật thành công!");
            return "user/profile";
        } catch (Exception e) {
            User u = userService.findByEmail(auth.getName()).orElseThrow();
            model.addAttribute("user", u);
            model.addAttribute("formFullName", fullName);
            model.addAttribute("formPhone", phone);
            model.addAttribute("formAddress", address);
            model.addAttribute("error", "Cập nhật thất bại: " + ErrorMessages.safe(e));
            return "user/profile";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication auth,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Model model) {
        try {
            userService.changePassword(auth.getName(), oldPassword, newPassword);
            User u = userService.findByEmail(auth.getName()).orElseThrow();
            model.addAttribute("user", u);
            model.addAttribute("success", "Đổi mật khẩu thành công!");
            return "user/profile";
        } catch (Exception e) {
            User u = userService.findByEmail(auth.getName()).orElseThrow();
            model.addAttribute("user", u);
            model.addAttribute("error", ErrorMessages.safe(e));
            return "user/profile";
        }
    }
}