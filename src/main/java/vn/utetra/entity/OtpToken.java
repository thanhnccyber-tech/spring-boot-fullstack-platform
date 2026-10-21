package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_email", columnList = "email"),
        @Index(name = "idx_otp_code", columnList = "code")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OtpToken {

    public static final String TYPE_REGISTER = "REGISTER";
    public static final String TYPE_RESET    = "RESET";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean consumed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}