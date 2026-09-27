package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {

    private static final DateTimeFormatter FMT_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_SHORT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "payment_method")
    private String paymentMethod; // CASH, BANK, COD

    @Column(name = "payment_status")
    private String paymentStatus = "PENDING"; // PENDING, PAID

    @Column(nullable = false)
    private String status = "PENDING";
    // PENDING -> PREPARING -> DELIVERING -> COMPLETED -> CANCELLED

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details;

    /* ==================== Derived getters cho JSP ==================== */

    /** Format đầy đủ: 27/09/2026 18:56 */
    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(FMT_DATETIME);
    }

    /** Format ngắn: 27/09 18:56 */
    public String getCreatedAtShort() {
        return createdAt == null ? "" : createdAt.format(FMT_SHORT);
    }

    public String getUpdatedAtFormatted() {
        return updatedAt == null ? "" : updatedAt.format(FMT_DATETIME);
    }
}