package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Sản phẩm yêu thích của khách. */
@Entity
@Table(name = "favorites",
       uniqueConstraints = @UniqueConstraint(name = "uk_fav_user_product", columnNames = {"user_id", "product_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
