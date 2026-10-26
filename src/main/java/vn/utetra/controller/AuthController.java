package vn.utetra.controller;

import vn.utetra.security.ErrorMessages;
import vn.utetra.security.SafeRedirect;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
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
import vn.utetra.security.AttemptLimiter;
import vn.utetra.security.PasswordPolicy;
import vn.utetra.security.ResetTokenService;
import vn.utetra.service.UserService;
import vn.utetra.util.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private MailService mailService;
    @Autowired private OtpService otpService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AttemptLimiter attemptLimiter;
    @Autowired private ResetTokenService resetTokenService;

    /** true khi chạy HTTPS (production): cookie JWT chỉ gửi qua kết nối mã hoá */
    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    private static final String DUMMY_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOa7eNn0n0Yb1pQ0o5R6xO8Yk3vJ9eQ2u";

    private void writeJwtCookie(HttpServletResponse response, String value, int maxAgeSeconds, boolean secureRequest) {
        Cookie cookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE, value);
        cookie.setHttpOnly(true);               // JS không đọc được cookie (chống đánh cắp qua XSS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setSecure(cookieSecure || secureRequest);
        cookie.setAttribute("SameSite", "Lax"); // trình duyệt không gửi cookie trong POST từ trang web khác
        response.addCookie(cookie);
    }

    /** Mã hoá giá trị đưa vào query string của redirect */
    private static String enc(String v) {
        return UriUtils.encodeQueryParam(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    /* ==================== LOGIN ==================== */

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String next, Model model) {
        model.addAttribute("next", SafeRedirect.localPath(next, ""));
        if (!model.containsAttribute("loginRequest"))
            model.addAttribute("loginRequest", new LoginRequest());
        return "user/login";
    }

    @PostMapping("/auth/login")
    public String doLogin(@ModelAttribute LoginRequest req,
                          @RequestParam(required = false) String next,
                          RedirectAttributes ra,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          HttpSession session) {

        String email = req.getEmail() == null ? "" : req.getEmail().trim();
        String ipKey = "login:ip:" + request.getRemoteAddr();
        String emailKey = "login:email:" + email.toLowerCase();

        // Chống dò mật khẩu: khoá tạm khi sai nhiều lần
        long locked = Math.max(attemptLimiter.lockedSeconds(ipKey), attemptLimiter.lockedSeconds(emailKey));
        if (locked > 0) {
            model.addAttribute("loginRequest", req);
            model.addAttribute("next", SafeRedirect.localPath(next, ""));
            model.addAttribute("error", "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau "
                    + ((locked + 59) / 60) + " phút.");
            return "user/login";
        }

        Optional<User> opt = userService.findByEmail(email);
        // Luôn băm 1 lần dù email không tồn tại để thời gian phản hồi không lộ email nào có tài khoản
        boolean passwordOk = passwordEncoder.matches(
                req.getPassword() == null ? "" : req.getPassword(),
                opt.map(User::getPassword).orElse(DUMMY_HASH));

        if (opt.isEmpty() || !passwordOk) {
            attemptLimiter.recordFailure(ipKey);
            attemptLimiter.recordFailure(emailKey);
            model.addAttribute("loginRequest", req);
            model.addAttribute("next", SafeRedirect.localPath(next, ""));
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "user/login";
        }
        User u = opt.get();

        if (!Boolean.TRUE.equals(u.getEnabled())) {
            return "redirect:/otp/register?email=" + enc(u.getEmail());
        }

        attemptLimiter.reset(ipKey);
        attemptLimiter.reset(emailKey);

        List<String> roles = u.getRoles().stream().map(r -> r.getName()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), roles, u.getId(), u.getFullName());

        writeJwtCookie(response, token, 24 * 3600, request.isSecure());

        // Chống session fixation: đổi session id sau khi đăng nhập
        request.changeSessionId();
        session.setAttribute("userId",   u.getId());
        session.setAttribute("fullName", u.getFullName());
        session.setAttribute("email",    u.getEmail());
        session.setAttribute("roles",    roles);

        if (roles.contains(RoleName.ADMIN))   return "redirect:/admin/dashboard";
        if (roles.contains(RoleName.MANAGER)) return "redirect:/admin/orders";
        if (roles.contains(RoleName.STAFF))   return "redirect:/admin/orders";
        if (roles.contains(RoleName.SHIPPER)) return "redirect:/shipper/dashboard";

        userService.ensureMemberNotification(u);
        boolean newMember = userService.markWelcomedFirstTime(u);
        if (newMember) {
            ra.addFlashAttribute("welcomeNew", u.getFullName() == null || u.getFullName().isBlank() ? "bạn" : u.getFullName());
        }
        // Khách hàng: thông báo hội viên + quay lại trang đang làm dở (giỏ hàng, thanh toán...)
        ra.addFlashAttribute("success", "Đăng nhập thành công! Bạn là hội viên UTeTra"
                + (u.getFullName() == null || u.getFullName().isBlank() ? "" : " (" + u.getFullName() + ")")
                + ", hiện có " + u.getPoints() + " điểm. Mỗi 10.000đ mua hàng = 1 điểm.");
        return "redirect:" + SafeRedirect.localPath(next, "/");
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
                model.addAttribute("error", ErrorMessages.safe(mailEx));
                return "user/otp";
            }
            return "redirect:/otp/register?email=" + enc(u.getEmail());
        } catch (Exception e) {
            model.addAttribute("registerRequest", req);
            model.addAttribute("error", ErrorMessages.safe(e));
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
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/otp/register?email=" + enc(email);
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

        // Không tiết lộ email có tồn tại hay không (chống dò danh sách tài khoản):
        // email lạ vẫn được chuyển sang trang nhập OTP như bình thường, chỉ là không gửi mail.
        if (opt.isPresent()) {
            try {
                otpService.issue(email, OtpToken.TYPE_RESET);
            } catch (Exception e) {
                model.addAttribute("forgotRequest", req);
                model.addAttribute("error", ErrorMessages.safe(e));
                return "user/forgot-password";
            }
        }
        return "redirect:/otp/reset?email=" + enc(email);
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
        // Verify OK → cấp token dùng 1 lần, chuyển sang form đặt mật khẩu mới
        String resetToken = resetTokenService.issue(req.getEmail());
        return "redirect:/reset-password?email=" + enc(req.getEmail()) + "&token=" + enc(resetToken);
    }

    @GetMapping("/otp/reset/resend")
    public String resendOtpReset(@RequestParam String email, RedirectAttributes ra) {
        try {
            otpService.issue(email, OtpToken.TYPE_RESET);
            ra.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", ErrorMessages.safe(e));
        }
        return "redirect:/otp/reset?email=" + enc(email);
    }

    /* ==================== RESET PASSWORD FORM ==================== */

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam String email,
                            @RequestParam(required = false) String token,
                            Model model) {
        // Phải có token hợp lệ (chỉ cấp sau khi nhập đúng OTP) mới được vào trang này
        if (!resetTokenService.isValid(token, email)) {
            return "redirect:/forgot-password";
        }
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail(email);
        req.setToken(token);
        model.addAttribute("resetRequest", req);
        return "user/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@ModelAttribute("resetRequest") ResetPasswordRequest req,
                              Model model) {
        try {
            if (!resetTokenService.isValid(req.getToken(), req.getEmail())) {
                throw new RuntimeException(
                        "Phiên đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu mã OTP mới.");
            }
            PasswordPolicy.require(req.getNewPassword());
            if (!req.getNewPassword().equals(req.getConfirmPassword())) {
                throw new RuntimeException("Xác nhận mật khẩu không khớp");
            }
            if (!resetTokenService.consume(req.getToken(), req.getEmail())) {
                throw new RuntimeException(
                        "Phiên đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu mã OTP mới.");
            }
            userService.resetPassword(req.getEmail(), req.getNewPassword());
            return "redirect:/login?reset=true";
        } catch (Exception e) {
            model.addAttribute("resetRequest", req);
            model.addAttribute("error", ErrorMessages.safe(e));
            return "user/reset-password";
        }
    }

    /* ==================== LOGOUT ==================== */

    @Autowired private vn.utetra.security.TokenBlacklist tokenBlacklist;

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        // Thu hồi token đang dùng (kể cả bản sao) rồi xoá cookie + phiên
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (JwtAuthenticationFilter.JWT_COOKIE.equals(c.getName())) {
                    tokenBlacklist.revoke(c.getValue(), 25L * 3600 * 1000);
                }
            }
        }
        writeJwtCookie(response, "", 0, request.isSecure());
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        session.invalidate();
        // Không cho trình duyệt giữ lại trang cũ (nút Back không hiện lại dữ liệu sau khi đăng xuất)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Clear-Site-Data", "\"cache\"");
        return "redirect:/";
    }
}