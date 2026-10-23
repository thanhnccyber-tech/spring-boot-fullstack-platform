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
import vn.utetra.dto.ForgotPasswordRequest;
import vn.utetra.dto.LoginRequest;
import vn.utetra.dto.OtpVerifyRequest;
import vn.utetra.dto.RegisterRequest;
import vn.utetra.dto.ResetPasswordRequest;
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
                          Model model,
                          HttpServletResponse response,
                          HttpSession session) {

        Optional<User> opt = userService.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            model.addAttribute("loginRequest", req);
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "user/login";
        }
        User u = opt.get();

        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            model.addAttribute("loginRequest", req);
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "user/login";
        }
        if (!Boolean.TRUE.equals(u.getEnabled())) {
            return "redirect:/otp/register?email=" + u.getEmail();
        }

        List<String> roles = u.getRoles().stream().map(r -> r.getName()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), roles, u.getId(), u.getFullName());

        Cookie cookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 3600);
        cookie.setSecure(false);
        response.addCookie(cookie);

        session.setAttribute("userId",   u.getId());
        session.setAttribute("fullName", u.getFullName());
        session.setAttribute("email",    u.getEmail());
        session.setAttribute("roles",    roles);

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
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest req,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "user/register";
        }
        try {
            User u = userService.registerPending(req);
            try {
                otpService.issue(u.getEmail(), OtpToken.TYPE_REGISTER);
            } catch (Exception mailEx) {
                OtpVerifyRequest oreq = new OtpVerifyRequest();
                oreq.setEmail(u.getEmail());
                model.addAttribute("otpRequest", oreq);
                model.addAttribute("email", u.getEmail());
                model.addAttribute("error", mailEx.getMessage());
                return "user/otp";
            }
            return "redirect:/otp/register?email=" + u.getEmail();
        } catch (Exception e) {
            model.addAttribute("registerRequest", req);
            model.addAttribute("error", e.getMessage());
            return "user/register";
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
    public String otpVerify(@ModelAttribute OtpVerifyRequest req, Model model) {
        boolean ok = otpService.verify(req.getEmail(), req.getCode(), OtpToken.TYPE_REGISTER);
        if (!ok) {
            model.addAttribute("otpRequest", req);
            model.addAttribute("email", req.getEmail());
            model.addAttribute("error", "Mã OTP không đúng hoặc đã hết hạn");
            return "user/otp";
        }
        userService.activate(req.getEmail());
        try {
            mailService.send(req.getEmail(), "Chào mừng đến với UTeTra",
                    "Tài khoản của bạn đã được kích hoạt. Hãy đăng nhập và trải nghiệm nhé!");
        } catch (Exception ignored) {}

        return "redirect:/login?activated=true";
    }

    @GetMapping("/otp/register/resend")
    public String resendOtp(@RequestParam String email, RedirectAttributes ra) {
        try {
            otpService.issue(email, OtpToken.TYPE_REGISTER);
            ra.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/otp/register?email=" + email;
    }

    /* ==================== FORGOT PASSWORD ==================== */

    @GetMapping("/forgot-password")
    public String forgotPage(Model model) {
        if (!model.containsAttribute("forgotRequest"))
            model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@ModelAttribute("forgotRequest") ForgotPasswordRequest req,
                               Model model) {
        String email = req.getEmail() == null ? "" : req.getEmail().trim();
        Optional<User> opt = userService.findByEmail(email);

        // Không tiết lộ email có tồn tại hay không để tránh enumeration
        // Nhưng vì đây là đồ án, có thể thông báo rõ để UX tốt hơn.
        if (opt.isEmpty()) {
            model.addAttribute("forgotRequest", req);
            model.addAttribute("error",
                    "Không tìm thấy tài khoản với email này.");
            return "user/forgot-password";
        }

        try {
            otpService.issue(email, OtpToken.TYPE_RESET);
            return "redirect:/otp/reset?email=" + email;
        } catch (Exception e) {
            model.addAttribute("forgotRequest", req);
            model.addAttribute("error", e.getMessage());
            return "user/forgot-password";
        }
    }

    /* ==================== OTP RESET PASSWORD ==================== */

    @GetMapping("/otp/reset")
    public String otpResetPage(@RequestParam String email, Model model) {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setEmail(email);
        model.addAttribute("otpRequest", req);
        model.addAttribute("email", email);
        return "user/otp-reset";
    }

    @PostMapping("/otp/reset/verify")
    public String otpResetVerify(@ModelAttribute OtpVerifyRequest req, Model model) {
        boolean ok = otpService.verify(req.getEmail(), req.getCode(), OtpToken.TYPE_RESET);
        if (!ok) {
            model.addAttribute("otpRequest", req);
            model.addAttribute("email", req.getEmail());
            model.addAttribute("error", "Mã OTP không đúng hoặc đã hết hạn");
            return "user/otp-reset";
        }
        // Verify OK → chuyển sang form đặt mật khẩu mới
        return "redirect:/reset-password?email=" + req.getEmail();
    }

    @GetMapping("/otp/reset/resend")
    public String resendOtpReset(@RequestParam String email, RedirectAttributes ra) {
        try {
            otpService.issue(email, OtpToken.TYPE_RESET);
            ra.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/otp/reset?email=" + email;
    }

    /* ==================== RESET PASSWORD FORM ==================== */

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam String email, Model model) {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail(email);
        model.addAttribute("resetRequest", req);
        return "user/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@ModelAttribute("resetRequest") ResetPasswordRequest req,
                              Model model) {
        try {
            if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
                throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự");
            }
            if (!req.getNewPassword().equals(req.getConfirmPassword())) {
                throw new RuntimeException("Xác nhận mật khẩu không khớp");
            }
            userService.resetPassword(req.getEmail(), req.getNewPassword());
            return "redirect:/login?reset=true";
        } catch (Exception e) {
            model.addAttribute("resetRequest", req);
            model.addAttribute("error", e.getMessage());
            return "user/reset-password";
        }
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