package vn.utetra.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Danh sách token đã đăng xuất: dù ai đó đã sao chép cookie JWT trước khi đăng xuất,
 * token đó cũng không dùng lại được. Lưu trong bộ nhớ (mất khi tắt server; token hết hạn sau 24 giờ).
 */
@Component
public class TokenBlacklist {

    /** token -> thời điểm hết hạn của danh sách đen (ms) */
    private final Map<String, Long> revoked = new ConcurrentHashMap<>();

    public void revoke(String token, long ttlMillis) {
        if (token != null && !token.isBlank()) revoked.put(token, System.currentTimeMillis() + ttlMillis);
    }

    /** email (chữ thường) -> mốc (giây epoch): mọi token cấp trước mốc này đều vô hiệu. */
    private final Map<String, Long> revokedBefore = new ConcurrentHashMap<>();

    /** Đổi/đặt lại mật khẩu: vô hiệu mọi token đã cấp trước đó của tài khoản (kể cả token bị đánh cắp). */
    public void revokeAllFor(String email) {
        if (email != null) revokedBefore.put(email.toLowerCase(), System.currentTimeMillis() / 1000 + 1);
    }

    /** true nếu token (thời điểm cấp issuedAtMillis) được cấp trước lần đổi mật khẩu gần nhất. */
    public boolean isIssuedBeforeRevocation(String email, long issuedAtMillis) {
        Long cutoff = email == null ? null : revokedBefore.get(email.toLowerCase());
        return cutoff != null && issuedAtMillis / 1000 < cutoff;
    }

    public boolean isRevoked(String token) {
        Long until = revoked.get(token);
        if (until == null) return false;
        if (until < System.currentTimeMillis()) { revoked.remove(token); return false; }
        return true;
    }

    @Scheduled(fixedDelay = 3_600_000L)
    public void cleanup() {
        long now = System.currentTimeMillis();
        revoked.entrySet().removeIf(e -> e.getValue() < now);
    }
}
