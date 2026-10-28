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
    public static final String STATUS_READY     = "READY";
    public static final String STATUS_DELIVERING = "DELIVERING";
    public static final String STATUS_COMPLETED  = "COMPLETED";
    public static final String STATUS_CANCELLED  = "CANCELLED";
    public static final String STATUS_FAILED     = "FAILED";
    public static final String STATUS_COMPLAINED = "COMPLAINED";
    public static final String STATUS_REFUNDED   = "REFUNDED";

    public static final String METHOD_DELIVERY = "DELIVERY";
    public static final String METHOD_PICKUP   = "PICKUP";

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

    @Column(name = "delivery_method", length = 10)
    private String deliveryMethod = METHOD_DELIVERY;

    @Column(length = 20)
    private String phone;

    @Column(name = "delivery_time", length = 40)
    private String deliveryTime;

    @Column(name = "vat_info", length = 500)
    private String vatInfo;

    @Column(name = "shipping_fee", precision = 14, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", precision = 14, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "voucher_code", length = 30)
    private String voucherCode;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "points_used")
    private Integer pointsUsed;

    @Column(name = "points_discount", precision = 14, scale = 2)
    private BigDecimal pointsDiscount;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_ref", length = 60)
    private String paymentRef;

    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    /* ===== Khiếu nại ===== */
    @Column(name = "complaint_reason", length = 1000)
    private String complaintReason;

    @Column(name = "complaint_at")
    private LocalDateTime complaintAt;

    @Column(name = "complaint_resolved_at")
    private LocalDateTime complaintResolvedAt;

    /** Ảnh/video đính kèm khiếu nại (Cloudinary URL + MIME type) */
    @Column(name = "complaint_media_url", length = 500)
    private String complaintMediaUrl;

    @Column(name = "complaint_media_type", length = 100)
    private String complaintMediaType;

    /* ===== Hoàn tiền ===== */
    @Column(name = "refund_amount", precision = 14, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_at")
    private LocalDateTime refundAt;

    @Column(name = "refund_note", length = 500)
    private String refundNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details;

    public boolean isPickup() { return METHOD_PICKUP.equals(deliveryMethod); }

    public BigDecimal getSubtotalAmount() {
        BigDecimal t = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        BigDecimal fee = shippingFee == null ? BigDecimal.ZERO : shippingFee;
        BigDecimal dis = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal pts = pointsDiscount == null ? BigDecimal.ZERO : pointsDiscount;
        return t.subtract(fee).add(dis).add(pts);
    }

    public String getStatusLabel() {
        if (status == null) return "";
        return switch (status) {
            case STATUS_PENDING    -> "Chờ xác nhận";
            case STATUS_PREPARING  -> "Đang pha chế";
            case STATUS_READY      -> "Chờ giao lại";
            case STATUS_DELIVERING -> isPickup() ? "Sẵn sàng tại cửa hàng" : "Đang giao hàng";
            case STATUS_COMPLETED  -> "Hoàn thành";
            case STATUS_CANCELLED  -> "Đã hủy";
            case STATUS_FAILED     -> "Giao thất bại";
            case STATUS_COMPLAINED -> "Đang khiếu nại";
            case STATUS_REFUNDED   -> "Đã hoàn tiền";
            default -> status;
        };
    }

    public int getProgressStep() {
        if (status == null) return 0;
        return switch (status) {
            case STATUS_PENDING    -> 1;
            case STATUS_PREPARING, STATUS_READY -> 2;
            case STATUS_DELIVERING -> 3;
            case STATUS_COMPLETED  -> 4;
            default -> 0;
        };
    }

    public boolean isCancellable() {
        return STATUS_PENDING.equals(status) && !"PAID".equals(paymentStatus);
    }

    public boolean isAwaitingOnlinePayment() {
        return ("VNPAY".equals(paymentMethod) || "MOMO".equals(paymentMethod))
                && !"PAID".equals(paymentStatus)
                && STATUS_PENDING.equals(status);
    }

    /** Khách chỉ được khiếu nại đơn đã hoàn thành và chưa từng khiếu nại trước đó. */
    public boolean isComplainable() {
        return STATUS_COMPLETED.equals(status) && complaintAt == null;
    }

    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(FMT_DATETIME);
    }
    public String getCreatedAtShort() {
        return createdAt == null ? "" : createdAt.format(FMT_SHORT);
    }
    public String getUpdatedAtFormatted() {
        return updatedAt == null ? "" : updatedAt.format(FMT_DATETIME);
    }
    public String getComplaintAtFormatted() {
        return complaintAt == null ? "" : complaintAt.format(FMT_DATETIME);
    }
    public String getRefundAtFormatted() {
        return refundAt == null ? "" : refundAt.format(FMT_DATETIME);
    }
}