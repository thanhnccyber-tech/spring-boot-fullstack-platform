package vn.utetra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.OtpToken;
import vn.utetra.repository.OtpTokenRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;

    /** Cooldown giữa 2 lần gửi cùng email + loại */
    private static final int COOLDOWN_SECONDS = 60;

    /** Số OTP tối đa được phát trong 1 giờ cho cùng email + loại */
    private static final int MAX_PER_HOUR = 5;

    /** Số lần nhập sai tối đa trước khi OTP bị huỷ (buộc phải xin mã mới) */
    private static final int MAX_WRONG_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Integer> wrongAttempts = new ConcurrentHashMap<>();

    private static String attemptKey(String email, String type) {
        return type + ":" + (email == null ? "" : email.toLowerCase());
    }

    @Autowired private OtpTokenRepository repo;
    @Autowired private MailService mailService;

    /**
     * Phát OTP có kiểm soát spam:
     *  - Cooldown 60 giây.
     *  - Tối đa 5 lần / giờ / (email + loại).
     *
     * Ném RuntimeException với thông báo thân thiện nếu vượt giới hạn.
     */
    @Transactional
    public void issue(String email, String type) {
        LocalDateTime now = LocalDateTime.now();

        // 1) Kiểm tra cooldown
        Optional<OtpToken> last = repo.findTopByEmailAndTypeOrderByCreatedAtDesc(email, type);
        if (last.isPresent()) {
            LocalDateTime lastAt = last.get().getCreatedAt();
            if (lastAt != null) {
                long elapsed = Duration.between(lastAt, now).getSeconds();
                if (elapsed < COOLDOWN_SECONDS) {
                    long wait = COOLDOWN_SECONDS - elapsed;
                    throw new RuntimeException(
                            "Vui lòng đợi " + wait + " giây trước khi gửi lại mã OTP.");
                }
            }
        }

        // 2) Kiểm tra rate limit trong 1 giờ
        long issuedLastHour = repo.countIssuedSince(email, type, now.minusHours(1));
        if (issuedLastHour >= MAX_PER_HOUR) {
            throw new RuntimeException(
                    "Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau 1 giờ.");
        }

        // 3) Xóa OTP cũ, tạo OTP mới
        repo.deleteByEmailAndType(email, type);
        wrongAttempts.remove(attemptKey(email, type));

        String code = generateCode();
        OtpToken otp = new OtpToken();
        otp.setEmail(email);
        otp.setCode(code);
        otp.setType(type);
        otp.setExpiresAt(now.plusMinutes(EXPIRE_MINUTES));
        otp.setCreatedAt(now);
        repo.save(otp);

        // 4) Gửi mail (nếu lỗi thì vẫn giữ OTP để user resend sau)
        try {
            mailService.sendOtp(email, code);
        } catch (Exception e) {
            log.error("Gửi OTP thất bại cho {}: {}", email, e.getMessage(), e);
            throw new RuntimeException(
                    "Không thể gửi email OTP. Vui lòng thử lại sau.", e);
        }
    }

    @Transactional
    public boolean verify(String email, String code, String type) {
        String key = attemptKey(email, type);
        var opt = repo.findTopByEmailAndCodeAndTypeAndConsumedFalseOrderByCreatedAtDesc(
                email, code, type);
        if (opt.isEmpty() || opt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            // Chống dò mã 6 số: sai quá MAX_WRONG_ATTEMPTS lần thì huỷ OTP hiện tại
            int n = wrongAttempts.merge(key, 1, Integer::sum);
            if (n >= MAX_WRONG_ATTEMPTS) {
                repo.deleteByEmailAndType(email, type);
                wrongAttempts.remove(key);
            }
            return false;
        }
        OtpToken otp = opt.get();
        otp.setConsumed(true);
        repo.save(otp);
        wrongAttempts.remove(key);
        return true;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    @Transactional
    public void cleanup() {
        repo.findAll().stream()
                .filter(o -> o.getExpiresAt().isBefore(LocalDateTime.now().minusHours(1)))
                .forEach(repo::delete);
    }
}