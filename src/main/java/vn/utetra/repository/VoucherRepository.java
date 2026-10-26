package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCodeIgnoreCase(String code);

    List<Voucher> findAllByOrderByIdDesc();

    /** Tăng lượt dùng nguyên tử; trả 0 nếu đã hết lượt (chống 2 người dùng cùng lúc vượt giới hạn). */
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 " +
           "WHERE v.id = :id AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    int increaseUsage(@Param("id") Long id);

    /** Trả lại 1 lượt dùng khi đơn bị hủy. */
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount - 1 " +
           "WHERE UPPER(v.code) = UPPER(:code) AND v.usedCount > 0")
    int decreaseUsage(@Param("code") String code);

    /** Mã còn hiệu lực nhưng sắp hết lượt (còn <= :left lượt) hoặc sắp hết hạn trước :soon. */
    @Query("SELECT v FROM Voucher v WHERE v.active = true " +
           "AND (v.expiresAt IS NULL OR v.expiresAt > :now) " +
           "AND ((v.usageLimit IS NOT NULL AND v.usageLimit - v.usedCount <= :left) " +
           "     OR (v.expiresAt IS NOT NULL AND v.expiresAt < :soon)) " +
           "ORDER BY v.id DESC")
    List<Voucher> findExpiringOrNearlyUsedUp(@Param("now") java.time.LocalDateTime now,
                                             @Param("soon") java.time.LocalDateTime soon,
                                             @Param("left") int left);
}
