package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.OtpToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndCodeAndTypeAndConsumedFalseOrderByCreatedAtDesc(
            String email, String code, String type);

    void deleteByEmailAndType(String email, String type);

    /** OTP mới nhất của email + loại (dùng để check cooldown) */
    Optional<OtpToken> findTopByEmailAndTypeOrderByCreatedAtDesc(String email, String type);

    /** Đếm số OTP đã phát trong khoảng thời gian (rate limit) */
    @Query("SELECT COUNT(o) FROM OtpToken o " +
           "WHERE o.email = :email AND o.type = :type " +
           "AND o.createdAt >= :since")
    long countIssuedSince(@Param("email") String email,
                          @Param("type") String type,
                          @Param("since") LocalDateTime since);
}