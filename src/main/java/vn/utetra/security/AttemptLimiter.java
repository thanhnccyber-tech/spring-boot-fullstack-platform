package vn.utetra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn số lần thử sai (chống brute-force mật khẩu).
 * Sai quá {@code security.max-attempts} lần trong cửa sổ thời gian thì khoá
 * {@code security.lock-minutes} phút. Lưu trong bộ nhớ (mất khi khởi động lại).
 */
@Component
public class AttemptLimiter {

    private static final class State {
        int failures;
        Instant windowStart = Instant.now();
        Instant lockedUntil;
    }

    private final Map<String, State> states = new ConcurrentHashMap<>();

    @Value("${security.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.lock-minutes:15}")
    private int lockMinutes;

    /** Số giây còn bị khoá, 0 nếu không bị khoá. */
    public long lockedSeconds(String key) {
        State s = states.get(key);
        if (s == null || s.lockedUntil == null) return 0;
        long left = Duration.between(Instant.now(), s.lockedUntil).getSeconds();
        if (left <= 0) {
            states.remove(key);
            return 0;
        }
        return left;
    }

    public void recordFailure(String key) {
        states.compute(key, (k, s) -> {
            Instant now = Instant.now();
            if (s == null || now.isAfter(s.windowStart.plus(Duration.ofMinutes(lockMinutes)))) {
                s = new State();
            }
            s.failures++;
            if (s.failures >= maxAttempts) {
                s.lockedUntil = now.plus(Duration.ofMinutes(lockMinutes));
            }
            return s;
        });
    }

    public void reset(String key) {
        states.remove(key);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanup() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(lockMinutes * 2L));
        states.entrySet().removeIf(e -> e.getValue().windowStart.isBefore(cutoff)
                && (e.getValue().lockedUntil == null || e.getValue().lockedUntil.isBefore(Instant.now())));
    }
}
