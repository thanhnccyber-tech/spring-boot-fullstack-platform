package vn.utetra.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import vn.utetra.dto.LoginRequest;
import vn.utetra.security.AttemptLimiter;
import vn.utetra.entity.User;
import vn.utetra.service.UserService;
import vn.utetra.util.JwtUtil;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AttemptLimiter attemptLimiter;

    /** Hash hợp lệ của một mật khẩu ngẫu nhiên: luôn tốn 1 lần băm để thời gian phản hồi không lộ email có tồn tại. */
    private static final String DUMMY_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOa7eNn0n0Yb1pQ0o5R6xO8Yk3vJ9eQ2u";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        String email = req.getEmail() == null ? "" : req.getEmail().trim();
        String ipKey = "login:ip:" + request.getRemoteAddr();
        String emailKey = "login:email:" + email.toLowerCase();

        // Dùng chung bộ đếm với form đăng nhập: không thể né giới hạn sai mật khẩu bằng cách gọi API.
        long locked = Math.max(attemptLimiter.lockedSeconds(ipKey), attemptLimiter.lockedSeconds(emailKey));
        if (locked > 0) {
            return ResponseEntity.status(429).header("Retry-After", String.valueOf(locked))
                    .body(Map.of("error", "Too many failed attempts"));
        }

        Optional<User> opt = userService.findByEmail(email);
        boolean ok = passwordEncoder.matches(
                req.getPassword() == null ? "" : req.getPassword(),
                opt.map(User::getPassword).orElse(DUMMY_HASH));
        if (opt.isEmpty() || !ok) {
            attemptLimiter.recordFailure(ipKey);
            attemptLimiter.recordFailure(emailKey);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        attemptLimiter.reset(ipKey);
        attemptLimiter.reset(emailKey);
        User u = opt.get();
        if (!Boolean.TRUE.equals(u.getEnabled())) {
            return ResponseEntity.status(403).body(Map.of("error", "Account not activated"));
        }
        List<String> roles = u.getRoles().stream().map(r -> r.getName()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), roles, u.getId(), u.getFullName());

        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("email", u.getEmail());
        res.put("roles", roles);
        res.put("userId", u.getId());
        return ResponseEntity.ok(res);
    }
}