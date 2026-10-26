package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Đánh giá & bình luận của khách cho một sản phẩm.
 * Mỗi khách chỉ có 1 đánh giá / sản phẩm (gửi lại sẽ cập nhật đánh giá cũ).
 */
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_user_product",
                columnNames = {"user_id", "product_id"}),
        indexes = @Index(name = "idx_review_product", columnList = "product_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Review {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Số sao 1..5 */
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "media_type", length = 100)
    private String mediaType;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(FMT);
    }

    public String getUpdatedAtFormatted() {
        return updatedAt == null ? "" : updatedAt.format(FMT);
    }

    /** true nếu đã được chỉnh sửa sau lần đăng đầu */
    public boolean isEdited() {
        return createdAt != null && updatedAt != null
                && updatedAt.isAfter(createdAt.plusSeconds(1));
    }
}
