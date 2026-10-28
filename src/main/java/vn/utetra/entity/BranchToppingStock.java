package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Trạng thái tồn kho của topping tại MỘT chi nhánh (fallback về Topping.outOfStock). */
@Entity
@Table(name = "branch_topping_stock",
        uniqueConstraints = @UniqueConstraint(name = "uk_bts_branch_topping",
                columnNames = {"branch_id", "topping_id"}),
        indexes = @Index(name = "idx_bts_branch", columnList = "branch_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BranchToppingStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "topping_id", nullable = false)
    private Long toppingId;

    @Column(name = "out_of_stock", nullable = false)
    private Boolean outOfStock = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}