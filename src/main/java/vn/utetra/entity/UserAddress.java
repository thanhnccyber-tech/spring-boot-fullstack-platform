package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Sổ địa chỉ của khách hàng. */
@Entity
@Table(name = "user_addresses", indexes = @Index(name = "idx_addr_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Tên gợi nhớ: Nhà, Công ty... */
    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(name = "is_default", nullable = false)
    private Boolean defaultAddress = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
