package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Voucher;
import vn.utetra.repository.VoucherRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherService {

    @Autowired private VoucherRepository repo;

    public List<Voucher> all() { return repo.findAllByOrderByIdDesc(); }

    /** Kiểm tra mã và trả về voucher hợp lệ; ném IllegalArgumentException (thông báo tiếng Việt) nếu không dùng được. */
    public Voucher validate(String code, BigDecimal subtotal) {
        String c = code == null ? "" : code.trim();
        if (c.isEmpty() || c.length() > 30)
            throw new IllegalArgumentException("Vui lòng nhập mã giảm giá hợp lệ.");
        Voucher v = repo.findByCodeIgnoreCase(c)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại."));
        if (!Boolean.TRUE.equals(v.getActive()))
            throw new IllegalArgumentException("Mã giảm giá đã bị tạm ngưng.");
        if (v.getExpiresAt() != null && v.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn.");
        if (v.getUsageLimit() != null && v.getUsedCount() != null && v.getUsedCount() >= v.getUsageLimit())
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
        BigDecimal min = v.getMinOrderAmount() == null ? BigDecimal.ZERO : v.getMinOrderAmount();
        if (subtotal == null || subtotal.compareTo(min) < 0)
            throw new IllegalArgumentException("Đơn hàng cần tối thiểu "
                    + String.format("%,d", min.longValue()).replace(',', '.') + "đ để dùng mã này.");
        return v;
    }

    /** Số tiền giảm trên TIỀN HÀNG. Voucher SHIPPING trả 0. */
    public BigDecimal discountOf(Voucher v, BigDecimal subtotal) {
        if (v.isShippingVoucher()) return BigDecimal.ZERO;
        BigDecimal d;
        if (v.isPercent()) {
            d = subtotal.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            if (v.getMaxDiscountAmount() != null && d.compareTo(v.getMaxDiscountAmount()) > 0)
                d = v.getMaxDiscountAmount();
        } else {
            d = v.getDiscountValue();
        }
        if (d.compareTo(subtotal) > 0) d = subtotal;
        return d.max(BigDecimal.ZERO);
    }

    /** Số tiền giảm trên PHÍ VẬN CHUYỂN. Voucher ORDER trả 0. */
    public BigDecimal shippingDiscountOf(Voucher v, BigDecimal shippingFee) {
        if (!v.isShippingVoucher() || shippingFee == null || shippingFee.signum() <= 0)
            return BigDecimal.ZERO;
        BigDecimal d;
        if (v.isPercent()) {
            d = shippingFee.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            if (v.getMaxDiscountAmount() != null && d.compareTo(v.getMaxDiscountAmount()) > 0)
                d = v.getMaxDiscountAmount();
        } else {
            d = v.getDiscountValue();
        }
        if (d.compareTo(shippingFee) > 0) d = shippingFee;
        return d.max(BigDecimal.ZERO);
    }

    /** Ghi nhận 1 lượt dùng (gọi trong giao dịch đặt hàng). */
    @Transactional
    public void consume(Voucher v) {
        if (repo.increaseUsage(v.getId()) == 0)
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
    }

    /** Trả lại 1 lượt dùng khi đơn dùng mã này bị hủy. */
    @Transactional
    public void release(String code) {
        if (code != null && !code.isBlank()) repo.decreaseUsage(code.trim());
    }

    @Transactional
    public void save(Voucher v, String code, String type, BigDecimal value, BigDecimal min,
                     BigDecimal max, LocalDateTime expires, Integer limit, boolean active, String desc,
                     String applyTo) {
        String c = code == null ? "" : code.trim().toUpperCase();
        if (!c.matches("[A-Z0-9_-]{3,30}"))
            throw new IllegalArgumentException("Mã chỉ gồm chữ HOA, số, gạch (3-30 ký tự).");
        if (!Voucher.PERCENT.equals(type) && !Voucher.AMOUNT.equals(type))
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ.");
        if (value == null || value.signum() <= 0)
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0.");
        if (Voucher.PERCENT.equals(type) && value.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Giảm theo % không được quá 100.");
        if (min != null && min.signum() < 0) throw new IllegalArgumentException("Đơn tối thiểu không hợp lệ.");
        if (limit != null && limit < 1) throw new IllegalArgumentException("Số lượt dùng phải từ 1 trở lên.");
        if (desc != null && desc.length() > 255) throw new IllegalArgumentException("Mô tả tối đa 255 ký tự.");
        repo.findByCodeIgnoreCase(c).ifPresent(e -> {
            if (v.getId() == null || !e.getId().equals(v.getId()))
                throw new IllegalArgumentException("Mã giảm giá đã tồn tại.");
        });

        String at = Voucher.APPLY_SHIPPING.equals(applyTo) ? Voucher.APPLY_SHIPPING : Voucher.APPLY_ORDER;

        v.setCode(c);
        v.setDiscountType(type);
        v.setApplyTo(at);
        v.setDiscountValue(value);
        v.setMinOrderAmount(min == null ? BigDecimal.ZERO : min);
        v.setMaxDiscountAmount(Voucher.PERCENT.equals(type) ? max : null);
        v.setExpiresAt(expires);
        v.setUsageLimit(limit);
        v.setActive(active);
        v.setDescription(desc == null || desc.isBlank() ? null : desc.trim());
        repo.save(v);
    }

    @Transactional
    public void toggle(Long id) {
        Voucher v = repo.findById(id).orElseThrow();
        v.setActive(!Boolean.TRUE.equals(v.getActive()));
        repo.save(v);
    }

    public Voucher get(Long id) { return repo.findById(id).orElseThrow(); }
}