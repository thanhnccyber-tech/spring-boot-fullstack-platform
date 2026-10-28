package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mã giảm giá (nhập ở bước thanh toán). */
@Entity
@Table(name = "vouchers", indexes = @Index(name = "idx_voucher_code", columnList = "code", unique = true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Voucher {

    public static final String PERCENT = "PERCENT";
    public static final String AMOUNT  = "AMOUNT";

    /** Áp dụng cho tiền hàng (mặc định, tương thích voucher cũ). */
    public static final String APPLY_ORDER    = "ORDER";
    /** Áp dụng cho phí vận chuyển. */
    public static final String APPLY_SHIPPING = "SHIPPING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(length = 255)
    private String description;

    /** PERCENT (giảm theo %) hoặc AMOUNT (giảm số tiền cố định) */
    @Column(name = "discount_type", nullable = false, length = 10)
    private String discountType = AMOUNT;

    /** ORDER: áp dụng tiền hàng — SHIPPING: áp dụng phí vận chuyển. */
    @Column(name = "apply_to", length = 20)
    private String applyTo = APPLY_ORDER;

    @Column(name = "discount_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    /** Đơn tối thiểu (tiền hàng, chưa gồm phí ship) để được áp dụng */
    @Column(name = "min_order_amount", precision = 14, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /** Giảm tối đa (dùng cho loại PERCENT); null = không giới hạn */
    @Column(name = "max_discount_amount", precision = 14, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Tổng số lượt dùng tối đa; null = không giới hạn */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Cột apply_to vừa được thêm: dòng cũ NULL -> coi như ORDER. */
    @PostLoad
    void ensureDefaults() {
        if (applyTo == null) applyTo = APPLY_ORDER;
    }

    public boolean isPercent()         { return PERCENT.equals(discountType); }
    public boolean isShippingVoucher() { return APPLY_SHIPPING.equals(applyTo); }
    public boolean isOrderVoucher()    { return !isShippingVoucher(); }
}