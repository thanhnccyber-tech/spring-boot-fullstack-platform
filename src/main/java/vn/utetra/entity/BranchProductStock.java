package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Trạng thái tồn kho của sản phẩm tại MỘT chi nhánh.
 * - Không có bản ghi → dùng {@link Product#getOutOfStock()} toàn cục.
 * - Nếu Product.outOfStock = true (Admin đánh dấu) → luôn hết hàng ở mọi chi nhánh.
 */
@Entity
@Table(name = "branch_product_stock",
        uniqueConstraints = @UniqueConstraint(name = "uk_bps_branch_product",
                columnNames = {"branch_id", "product_id"}),
        indexes = @Index(name = "idx_bps_branch", columnList = "branch_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BranchProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "out_of_stock", nullable = false)
    private Boolean outOfStock = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}