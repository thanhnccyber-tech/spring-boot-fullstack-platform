package vn.utetra.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token dùng 1 lần để đặt lại mật khẩu, chỉ được cấp SAU KHI xác thực OTP thành công.
 * Nếu không có token này thì không ai đặt lại được mật khẩu của người khác
 * chỉ bằng cách gửi thẳng email + mật khẩu mới.
 */
@Service
public class ResetTokenService {

    private record Entry(String email, Instant expiresAt) {}

    private static final Duration TTL = Duration.ofMinutes(10);

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();

    public String issue(String email) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, new Entry(email.toLowerCase(), Instant.now().plus(TTL)));
        return token;
    }

    /** Kiểm tra token còn hiệu lực cho email này (không tiêu thụ). */
    public boolean isValid(String token, String email) {
        if (token == null || email == null) return false;
        Entry e = tokens.get(token);
        return e != null && e.expiresAt().isAfter(Instant.now())
                && e.email().equals(email.toLowerCase());
    }

    /** Kiểm tra và tiêu thụ token (dùng được đúng 1 lần). */
    public boolean consume(String token, String email) {
        if (!isValid(token, email)) return false;
        return tokens.remove(token) != null;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanup() {
        tokens.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(Instant.now()));
    }
}
