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

    /** DELIVERY (giao tận nơi) hoặc PICKUP (đến lấy tại cửa hàng) */
    @Column(name = "delivery_method", length = 10)
    private String deliveryMethod = METHOD_DELIVERY;

    @Column(length = 20)
    private String phone;

    /** "ASAP" (càng sớm càng tốt) hoặc thời điểm khách hẹn (dd/MM/yyyy HH:mm) */
    @Column(name = "delivery_time", length = 40)
    private String deliveryTime;

    /** Thông tin xuất hoá đơn VAT (nếu khách yêu cầu) */
    @Column(name = "vat_info", length = 500)
    private String vatInfo;

    @Column(name = "shipping_fee", precision = 14, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", precision = 14, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "voucher_code", length = 30)
    private String voucherCode;

    /** Điểm hội viên đã cộng cho đơn này (cộng 1 lần khi đơn hoàn thành) */
    @Column(name = "points_earned")
    private Integer pointsEarned;

    /** Điểm hội viên khách đã dùng để trừ tiền cho đơn này, và số tiền được trừ */
    @Column(name = "points_used")
    private Integer pointsUsed;

    @Column(name = "points_discount", precision = 14, scale = 2)
    private BigDecimal pointsDiscount;

    /** Khoảng cách (km) từ cửa hàng tới khách lúc đặt, dùng tính phí ship (có thể null) */
    @Column(name = "distance_km")
    private Double distanceKm;

    /** Thanh toán online: thời điểm nhận tiền + mã giao dịch của cổng thanh toán */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_ref", length = 60)
    private String paymentRef;

    /** Lý do giao thất bại (nếu có) */
    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details;

    public boolean isPickup() { return METHOD_PICKUP.equals(deliveryMethod); }

    /** Tiền hàng (chưa tính phí ship, chưa trừ giảm giá) */
    public BigDecimal getSubtotalAmount() {
        BigDecimal t = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        BigDecimal fee = shippingFee == null ? BigDecimal.ZERO : shippingFee;
        BigDecimal dis = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal pts = pointsDiscount == null ? BigDecimal.ZERO : pointsDiscount;
        return t.subtract(fee).add(dis).add(pts);
    }

    /** Nhãn tiếng Việt của trạng thái, hiển thị cho khách. */
    public String getStatusLabel() {
        if (status == null) return "";
        return switch (status) {
            case STATUS_PENDING    -> "Chờ xác nhận";
            case STATUS_PREPARING  -> "Đang pha chế";
            case STATUS_DELIVERING -> isPickup() ? "Sẵn sàng tại cửa hàng" : "Đang giao hàng";
            case STATUS_COMPLETED  -> "Hoàn thành";
            case STATUS_CANCELLED  -> "Đã hủy";
            case STATUS_FAILED     -> "Giao thất bại";
            default -> status;
        };
    }

    /** Bước hiện tại trên thanh tiến trình: 1..4; 0 nếu đơn đã hủy / thất bại. */
    public int getProgressStep() {
        if (status == null) return 0;
        return switch (status) {
            case STATUS_PENDING    -> 1;
            case STATUS_PREPARING  -> 2;
            case STATUS_DELIVERING -> 3;
            case STATUS_COMPLETED  -> 4;
            default -> 0;
        };
    }

    /** Khách chỉ tự hủy được khi cửa hàng chưa xác nhận và chưa thanh toán online. */
    public boolean isCancellable() {
        return STATUS_PENDING.equals(status) && !"PAID".equals(paymentStatus);
    }

    /** Đơn chọn cổng online mà chưa thanh toán xong → cho phép thanh toán lại. */
    public boolean isAwaitingOnlinePayment() {
        return ("VNPAY".equals(paymentMethod) || "MOMO".equals(paymentMethod))
                && !"PAID".equals(paymentStatus)
                && STATUS_PENDING.equals(status);
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
}