package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Tin tức & khuyến mãi hiển thị ở trang chủ và /khuyen-mai. */
@Entity
@Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Mã giảm giá đi kèm bài viết (nếu có) để khách bấm sao chép */
    @Column(name = "voucher_code", length = 30)
    private String voucherCode;

    @Column(nullable = false)
    private Boolean active = true;

    /** Lượt xem */
    @Column(nullable = false)
    private Long views = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
