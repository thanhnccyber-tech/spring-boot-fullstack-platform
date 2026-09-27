package vn.utetra.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.config.JwtAuthenticationFilter;
import vn.utetra.dto.LoginRequest;
import vn.utetra.dto.RegisterRequest;
import vn.utetra.entity.User;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.MailService;
import vn.utetra.service.UserService;
import vn.utetra.util.JwtUtil;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private MailService mailService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginRequest"))
            model.addAttribute("loginRequest", new LoginRequest());
        return "user/login";
    }

    @PostMapping("/auth/login")
    public String doLogin(@ModelAttribute LoginRequest req,
                          RedirectAttributes ra,
                          HttpServletResponse response,
                          HttpSession session) {

        Optional<User> opt = userService.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
            return "redirect:/login";
        }

        User u = opt.get();

        // Verify password (dùng bean PasswordEncoder đã inject)
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            ra.addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
            return "redirect:/login";
        }

        List<String> roles = u.getRoles().stream()
                .map(r -> r.getName()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), roles, u.getId());

        Cookie cookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 3600);
        response.addCookie(cookie);

        // Lưu session cho WebSocket + JSP check role
        session.setAttribute("userId",   u.getId());
        session.setAttribute("fullName", u.getFullName());
        session.setAttribute("email",    u.getEmail());
        session.setAttribute("roles",    roles);   // Lưu LIST roles

        // Redirect theo role (ưu tiên cao nhất)
        if (roles.contains("ROLE_ADMIN"))   return "redirect:/admin/dashboard";
        if (roles.contains("ROLE_MANAGER")) return "redirect:/admin/users";
        if (roles.contains("ROLE_STAFF"))   return "redirect:/admin/orders";
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "user/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute RegisterRequest req,
                           BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "user/register";
        try {
            User u = userService.register(req);
            try {
                mailService.send(u.getEmail(), "Xác nhận đăng ký UTeTra",
                    "Chào mừng " + u.getFullName() + " đến với UTeTra!");
            } catch (Exception ignored) {}
            ra.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, HttpSession session) {
        Cookie cookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        session.invalidate();

        return "redirect:/";
    }
}