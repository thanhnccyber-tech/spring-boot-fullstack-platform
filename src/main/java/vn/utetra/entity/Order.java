package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user",   columnList = "user_id"),
        @Index(name = "idx_order_branch", columnList = "branch_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created", columnList = "created_at"),
        @Index(name = "idx_order_shipper", columnList = "shipper_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {

    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_PREPARING  = "PREPARING";
    public static final String STATUS_DELIVERING = "DELIVERING";
    public static final String STATUS_COMPLETED  = "COMPLETED";
    public static final String STATUS_CANCELLED  = "CANCELLED";
    public static final String STATUS_FAILED     = "FAILED";

    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_SHORT    = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", unique = true, length = 30)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /** Shipper được Admin/Manager phân công giao đơn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "PENDING";

    @Column(nullable = false, length = 20)
    private String status = STATUS_PENDING;

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    /** Lý do giao thất bại (nếu có) */
    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details;

    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(FMT_DATETIME);
    }
    public String getCreatedAtShort() {
        return createdAt == null ? "" : createdAt.format(FMT_SHORT);
    }
    public String getUpdatedAtFormatted() {
        return updatedAt == null ? "" : updatedAt.format(FMT_DATETIME);
    }
}