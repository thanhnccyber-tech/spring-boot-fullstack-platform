package vn.utetra.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Autowired private Environment env;

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String DEFAULT_SECRET_PREFIX = "utetraSecretKey2024";

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret phải dài tối thiểu 32 byte (256 bit).");
        }
        if (secret.startsWith(DEFAULT_SECRET_PREFIX) && env.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException("Không được dùng JWT secret mặc định trong source khi chạy profile prod. "
                    + "Hãy đặt JWT_SECRET ngẫu nhiên (vd: openssl rand -base64 48).");
        }
        if (secret.startsWith(DEFAULT_SECRET_PREFIX)) {
            log.warn("Đang dùng JWT secret MẶC ĐỊNH trong source code. "
                   + "Hãy đặt JWT_SECRET ngẫu nhiên riêng trong file .env (không commit lên Git).");
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, List<String> roles, Long userId, String fullName) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .claim("userId", userId)
                .claim("fullName", fullName)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) { return parse(token).getSubject(); }

    public Long getUserId(String token) {
        Object v = parse(token).get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) parse(token).get("roles");
    }

    public String getFullName(String token) {
        Object v = parse(token).get("fullName");
        return v == null ? "" : v.toString();
    }
}