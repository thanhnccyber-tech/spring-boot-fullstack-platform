package vn.utetra.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Truy vấn dành riêng cho màn hình Shipper (tách khỏi OrderRepository để dễ quản lý).
 */
public interface ShipperOrderRepository extends JpaRepository<Order, Long> {

    /**
     * Khóa dòng đơn hàng khi shipper thao tác, tránh 2 shipper cùng bấm "Nhận đơn" một lúc.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    /**
     * Đơn chờ shipper nhận: giao tận nơi, đã xác nhận hoặc chờ giao lại, cùng chi nhánh,
     * chưa có shipper hoặc đã được phân công đúng cho shipper này.
     */
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "LEFT JOIN FETCH o.shipper " +
           "WHERE o.deliveryMethod = 'DELIVERY' " +
           "AND o.status IN ('PREPARING', 'READY') " +
           "AND o.branch.id = :branchId " +
           "AND (o.shipper IS NULL OR o.shipper.id = :shipperId) " +
           "ORDER BY o.createdAt ASC")
    List<Order> findAvailable(@Param("branchId") Long branchId,
                              @Param("shipperId") Long shipperId);

    /** Đơn của shipper theo trạng thái (đang giao, đã giao, giao thất bại...). Mới cập nhật nằm trên. */
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "WHERE o.shipper.id = :shipperId " +
           "AND o.status IN :statuses " +
           "ORDER BY o.updatedAt DESC")
    List<Order> findByShipperAndStatuses(@Param("shipperId") Long shipperId,
                                         @Param("statuses") Collection<String> statuses);

    /**
     * Thống kê đơn giao thành công trong khoảng thời gian:
     * [0] số đơn, [1] tổng giá trị đơn.
     */
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o " +
           "WHERE o.shipper.id = :shipperId " +
           "AND o.status = 'COMPLETED' " +
           "AND o.updatedAt BETWEEN :from AND :to")
    List<Object[]> deliveredStats(@Param("shipperId") Long shipperId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    /** Tiền mặt (COD) shipper đã thu hộ trong khoảng thời gian. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o " +
           "WHERE o.shipper.id = :shipperId " +
           "AND o.status = 'COMPLETED' " +
           "AND o.paymentMethod = 'COD' " +
           "AND o.updatedAt BETWEEN :from AND :to")
    BigDecimal codCollected(@Param("shipperId") Long shipperId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    /** Thời điểm hoàn thành của các đơn giao thành công (để gom theo ngày ở tầng service). */
    @Query("SELECT o.updatedAt FROM Order o " +
           "WHERE o.shipper.id = :shipperId " +
           "AND o.status = 'COMPLETED' " +
           "AND o.updatedAt BETWEEN :from AND :to")
    List<LocalDateTime> deliveredTimes(@Param("shipperId") Long shipperId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);
}