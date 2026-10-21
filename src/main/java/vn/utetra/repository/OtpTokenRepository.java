package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.OtpToken;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByEmailAndCodeAndTypeAndConsumedFalseOrderByCreatedAtDesc(
            String email, String code, String type);
    void deleteByEmailAndType(String email, String type);
}