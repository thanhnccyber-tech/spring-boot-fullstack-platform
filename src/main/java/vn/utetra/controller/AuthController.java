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
import vn.utetra.dto.OtpVerifyRequest;
import vn.utetra.dto.RegisterRequest;
import vn.utetra.entity.OtpToken;
import vn.utetra.entity.RoleName;
import vn.utetra.entity.User;
import vn.utetra.service.MailService;
import vn.utetra.service.OtpService;
import vn.utetra.service.UserService;
import vn.utetra.util.JwtUtil;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private MailService mailService;
    @Autowired private OtpService otpService;
    @Autowired private PasswordEncoder passwordEncoder;

    /* ==================== LOGIN ==================== */

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

        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            ra.addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
            return "redirect:/login";
        }
        if (!Boolean.TRUE.equals(u.getEnabled())) {
            ra.addFlashAttribute("error", "Tài khoản chưa được kích hoạt. Vui lòng xác thực OTP.");
            return "redirect:/otp/register?email=" + u.getEmail();
        }

        List<String> roles = u.getRoles().stream().map(r -> r.getName()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), roles, u.getId(), u.getFullName());

        Cookie cookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 3600);
        cookie.setSecure(false); // bật =true khi chạy HTTPS
        response.addCookie(cookie);

        session.setAttribute("userId",   u.getId());
        session.setAttribute("fullName", u.getFullName());
        session.setAttribute("email",    u.getEmail());
        session.setAttribute("roles",    roles);

        // Điều hướng theo role (ưu tiên cao nhất)
        if (roles.contains(RoleName.ADMIN))   return "redirect:/admin/dashboard";
        if (roles.contains(RoleName.MANAGER)) return "redirect:/admin/orders";
        if (roles.contains(RoleName.STAFF))   return "redirect:/admin/orders";
        if (roles.contains(RoleName.SHIPPER)) return "redirect:/shipper/dashboard";
        return "redirect:/";
    }

    /* ==================== REGISTER ==================== */

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest"))
            model.addAttribute("registerRequest", new RegisterRequest());
        return "user/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute RegisterRequest req,
                           BindingResult result,
                           RedirectAttributes ra,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("registerRequest", req);
            return "user/register";
        }
        try {
            User u = userService.registerPending(req);
            otpService.issue(u.getEmail(), OtpToken.TYPE_REGISTER);
            ra.addFlashAttribute("success", "Đăng ký thành công! Vui lòng nhập OTP đã gửi qua email.");
            return "redirect:/otp/register?email=" + u.getEmail();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    /* ==================== OTP REGISTER ==================== */

    @GetMapping("/otp/register")
    public String otpPage(@RequestParam String email, Model model) {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setEmail(email);
        model.addAttribute("otpRequest", req);
        model.addAttribute("email", email);
        return "user/otp";
    }

    @PostMapping("/otp/register/verify")
    public String otpVerify(@ModelAttribute OtpVerifyRequest req, RedirectAttributes ra) {
        boolean ok = otpService.verify(req.getEmail(), req.getCode(), OtpToken.TYPE_REGISTER);
        if (!ok) {
            ra.addFlashAttribute("error", "Mã OTP không đúng hoặc đã hết hạn");
            return "redirect:/otp/register?email=" + req.getEmail();
        }
        userService.activate(req.getEmail());
        try {
            mailService.send(req.getEmail(), "Chào mừng đến với UTeTra",
                    "Tài khoản của bạn đã được kích hoạt. Hãy đăng nhập và trải nghiệm nhé!");
        } catch (Exception ignored) {}

        ra.addFlashAttribute("success", "Kích hoạt tài khoản thành công! Hãy đăng nhập.");
        return "redirect:/login";
    }

    @GetMapping("/otp/register/resend")
    public String resendOtp(@RequestParam String email, RedirectAttributes ra) {
        try {
            otpService.issue(email, OtpToken.TYPE_REGISTER);
            ra.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể gửi lại OTP: " + e.getMessage());
        }
        return "redirect:/otp/register?email=" + email;
    }

    /* ==================== LOGOUT ==================== */

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