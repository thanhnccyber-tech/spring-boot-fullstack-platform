package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.utetra.entity.User;
import vn.utetra.repository.NotificationRepository;
import vn.utetra.repository.UserRepository;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired private NotificationRepository notiRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping
    @Transactional
    public String list(Authentication auth, Model model) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        var notifications = notiRepo.findByUserIdOrderByCreatedAtDesc(u.getId());
        notifications.forEach(n -> n.setIsRead(true));
        notiRepo.saveAll(notifications);
        model.addAttribute("notifications", notifications);
        return "user/notifications";
    }

    @PostMapping("/read-all")
    @ResponseBody
    @Transactional
    public String markAllRead(Authentication auth) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        var unread = notiRepo.findByUserIdAndIsReadFalse(u.getId());
        unread.forEach(n -> n.setIsRead(true));
        notiRepo.saveAll(unread);
        return "ok";
    }
}