package vn.utetra.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.utetra.dto.LoginRequest;
import vn.utetra.entity.User;
import vn.utetra.service.UserService;
import vn.utetra.util.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> opt = userService.findByEmail(req.getEmail());
        if (opt.isEmpty() || !passwordEncoder.matches(req.getPassword(), opt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        User u = opt.get();
        List<String> roles = u.getRoles().stream().map(r -> r.getName()).toList();
        String token = jwtUtil.generateToken(u.getEmail(), roles, u.getId());
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("email", u.getEmail());
        res.put("roles", roles);
        return ResponseEntity.ok(res);
    }
}