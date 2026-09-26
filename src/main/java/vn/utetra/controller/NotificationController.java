package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.utetra.entity.User;
import vn.utetra.repository.NotificationRepository;
import vn.utetra.repository.UserRepository;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired private NotificationRepository notiRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public String list(Authentication auth, Model model) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("notifications",
                notiRepo.findByUserIdOrderByCreatedAtDesc(u.getId()));
        return "user/notifications";
    }
}