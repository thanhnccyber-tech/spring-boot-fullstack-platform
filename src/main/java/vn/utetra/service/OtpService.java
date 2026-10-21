package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.OtpToken;
import vn.utetra.repository.OtpTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;
    private final SecureRandom random = new SecureRandom();

    @Autowired private OtpTokenRepository repo;
    @Autowired private MailService mailService;

    @Transactional
    public void issue(String email, String type) {
        // Xóa OTP cũ cùng loại để tránh nhầm lẫn
        repo.deleteByEmailAndType(email, type);

        String code = generateCode();
        OtpToken otp = new OtpToken();
        otp.setEmail(email);
        otp.setCode(code);
        otp.setType(type);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
        repo.save(otp);

        mailService.sendOtp(email, code);
    }

    @Transactional
    public boolean verify(String email, String code, String type) {
        var opt = repo.findTopByEmailAndCodeAndTypeAndConsumedFalseOrderByCreatedAtDesc(
                email, code, type);
        if (opt.isEmpty()) return false;
        OtpToken otp = opt.get();
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        otp.setConsumed(true);
        repo.save(otp);
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