package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("SELECT o FROM Order o " + "LEFT JOIN FETCH o.user " + "LEFT JOIN FETCH o.branch "
			+ "LEFT JOIN FETCH o.shipper " + "WHERE o.user.id = :userId " + "ORDER BY o.createdAt DESC")
	List<Order> findByUserIdWithUser(@Param("userId") Long userId);

	@Query("SELECT o FROM Order o " + "LEFT JOIN FETCH o.user " + "LEFT JOIN FETCH o.branch "
			+ "LEFT JOIN FETCH o.shipper " + "WHERE o.branch.id = :branchId " + "ORDER BY o.createdAt DESC")
	List<Order> findByBranchIdWithUser(@Param("branchId") Long branchId);

	@Query("SELECT o FROM Order o " + "LEFT JOIN FETCH o.user " + "LEFT JOIN FETCH o.branch "
			+ "LEFT JOIN FETCH o.shipper " + "ORDER BY o.createdAt DESC")
	List<Order> findAllWithUser();

	@Query(value = "SELECT o FROM Order o " + "LEFT JOIN FETCH o.user u " + "LEFT JOIN FETCH o.branch b "
			+ "LEFT JOIN FETCH o.shipper s " + "WHERE (:branchId IS NULL OR b.id = :branchId) "
			+ "AND (:status IS NULL OR o.status = :status) " + "AND (:kw IS NULL "
			+ "     OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :kw, '%')) "
			+ "     OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) "
			+ "     OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :kw, '%')) "
			+ "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))) "
			+ "AND (:from IS NULL OR o.createdAt >= :from) "
			+ "AND (:to IS NULL OR o.createdAt <= :to)", countQuery = "SELECT COUNT(o) FROM Order o "
					+ "LEFT JOIN o.user u " + "LEFT JOIN o.branch b " + "WHERE (:branchId IS NULL OR b.id = :branchId) "
					+ "AND (:status IS NULL OR o.status = :status) " + "AND (:kw IS NULL "
					+ "     OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :kw, '%')) "
					+ "     OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) "
					+ "     OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :kw, '%')) "
					+ "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))) "
					+ "AND (:from IS NULL OR o.createdAt >= :from) " + "AND (:to IS NULL OR o.createdAt <= :to)")
	Page<Order> searchForAdmin(@Param("kw") String kw, @Param("status") String status, @Param("branchId") Long branchId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
	List<Order> findBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Tổng số điểm đã cộng từ các đơn hàng trước đây của hội viên (dùng khởi tạo dữ
	 * liệu cũ).
	 */
	@Query("SELECT SUM(o.pointsEarned) FROM Order o WHERE o.user.id = :userId")
	Long sumPointsEarnedByUserId(@Param("userId") Long userId);

	@Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
	List<Object[]> countByStatus();

	@Query("SELECT o.status, COUNT(o) FROM Order o " + "WHERE (:branchId IS NULL OR o.branch.id = :branchId) "
			+ "GROUP BY o.status")
	List<Object[]> countByStatusForBranch(@Param("branchId") Long branchId);

	@Query(value = "SELECT p.name, SUM(od.quantity) AS total "
			+ "FROM order_details od JOIN products p ON od.product_id = p.id "
			+ "GROUP BY p.id ORDER BY total DESC LIMIT 10", nativeQuery = true)
	List<Object[]> topSellingProducts();

	@Query(value = "SELECT t.name, COUNT(odt.topping_id) AS total " + "FROM order_detail_toppings odt "
			+ "JOIN toppings t ON odt.topping_id = t.id "
			+ "GROUP BY t.id ORDER BY total DESC LIMIT 10", nativeQuery = true)
	List<Object[]> topSellingToppings();

	// Tim don hang theo ma don hang, dong thoi tai cac thong tin lien quan
	@Query("SELECT DISTINCT o FROM Order o " + "LEFT JOIN FETCH o.user " + "LEFT JOIN FETCH o.branch "
			+ "LEFT JOIN FETCH o.shipper " + "LEFT JOIN FETCH o.details d " + "LEFT JOIN FETCH d.product "
			+ "LEFT JOIN FETCH d.toppings " + "WHERE o.orderCode = :orderCode")
	Optional<Order> findByOrderCode(@Param("orderCode") String orderCode);

	// Tim don hang theo ID va tai cac thong tin chi tiet
	@Query("SELECT DISTINCT o FROM Order o " + "LEFT JOIN FETCH o.user " + "LEFT JOIN FETCH o.branch "
			+ "LEFT JOIN FETCH o.shipper " + "LEFT JOIN FETCH o.details d " + "LEFT JOIN FETCH d.product "
			+ "LEFT JOIN FETCH d.toppings " + "WHERE o.id = :id")
	Optional<Order> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT o FROM Order o " + "LEFT JOIN FETCH o.user " + "WHERE o.id = :id")
	Optional<Order> findByIdWithUser(@Param("id") Long id);

	@Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount), COUNT(o) " + "FROM Order o "
			+ "WHERE o.createdAt BETWEEN :from AND :to " + "AND o.status <> 'CANCELLED' "
			+ "GROUP BY FUNCTION('DATE', o.createdAt) " + "ORDER BY FUNCTION('DATE', o.createdAt)")
	List<Object[]> revenueByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), "
			+ "       SUM(o.totalAmount), COUNT(o) " + "FROM Order o " + "WHERE o.createdAt BETWEEN :from AND :to "
			+ "AND o.status <> 'CANCELLED' " + "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt) "
			+ "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
	List<Object[]> revenueByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/* ==================== THỐNG KÊ DASHBOARD ====================
	 * "Hợp lệ" = chưa hủy, chưa giao thất bại. "Đã thu" = đã thanh toán online hoặc đơn đã hoàn thành (COD).
	 * Tất cả đều lọc theo khoảng thời gian và (tuỳ chọn) chi nhánh, tính ngay ở database. */
	String LIVE = "o.status NOT IN ('CANCELLED','FAILED')";
	String COLLECTED = "(o.paymentStatus = 'PAID' OR o.status = 'COMPLETED')";
	String IN_RANGE = "o.createdAt BETWEEN :from AND :to AND (:branchId IS NULL OR o.branch.id = :branchId)";

	@Query("SELECT o.status, COUNT(o) FROM Order o WHERE " + IN_RANGE + " GROUP BY o.status")
	List<Object[]> statusCountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	/** [tổng tiền dự kiến (có thể null), số đơn hợp lệ] */
	@Query("SELECT SUM(o.totalAmount), COUNT(o) FROM Order o WHERE " + IN_RANGE + " AND " + LIVE)
	List<Object[]> liveTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	/** [tổng tiền đã thu (có thể null), số đơn đã thu] */
	@Query("SELECT SUM(o.totalAmount), COUNT(o) FROM Order o WHERE " + IN_RANGE + " AND " + LIVE + " AND " + COLLECTED)
	List<Object[]> collectedTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	@Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount) FROM Order o WHERE " + IN_RANGE + " AND " + LIVE
			+ " GROUP BY FUNCTION('DATE', o.createdAt)")
	List<Object[]> liveByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	@Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount) FROM Order o WHERE " + IN_RANGE + " AND " + LIVE
			+ " AND " + COLLECTED + " GROUP BY FUNCTION('DATE', o.createdAt)")
	List<Object[]> collectedByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	@Query("SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount) FROM Order o WHERE "
			+ IN_RANGE + " AND " + LIVE + " AND " + COLLECTED
			+ " GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
	List<Object[]> collectedByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	@Query("SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), SUM(o.totalAmount) FROM Order o WHERE "
			+ IN_RANGE + " AND " + LIVE
			+ " GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
	List<Object[]> liveByMonth(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	/** [giờ trong ngày, số đơn, tổng tiền] */
	@Query("SELECT FUNCTION('HOUR', o.createdAt), COUNT(o), SUM(o.totalAmount) FROM Order o WHERE " + IN_RANGE
			+ " AND " + LIVE + " GROUP BY FUNCTION('HOUR', o.createdAt)")
	List<Object[]> liveByHour(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	/** [phương thức thanh toán, số đơn, tổng tiền] */
	@Query("SELECT o.paymentMethod, COUNT(o), SUM(o.totalAmount) FROM Order o WHERE " + IN_RANGE + " AND " + LIVE
			+ " GROUP BY o.paymentMethod")
	List<Object[]> liveByPaymentMethod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	@Query(value = "SELECT p.name, SUM(od.quantity) AS total FROM order_details od "
			+ "JOIN orders o ON od.order_id = o.id JOIN products p ON od.product_id = p.id "
			+ "WHERE o.created_at BETWEEN :from AND :to AND (:branchId IS NULL OR o.branch_id = :branchId) "
			+ "AND o.status NOT IN ('CANCELLED','FAILED') "
			+ "GROUP BY p.id, p.name ORDER BY total DESC LIMIT 10", nativeQuery = true)
	List<Object[]> topProductsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	@Query(value = "SELECT t.name, COUNT(odt.topping_id) AS total FROM order_detail_toppings odt "
			+ "JOIN order_details od ON odt.order_detail_id = od.id JOIN orders o ON od.order_id = o.id "
			+ "JOIN toppings t ON odt.topping_id = t.id "
			+ "WHERE o.created_at BETWEEN :from AND :to AND (:branchId IS NULL OR o.branch_id = :branchId) "
			+ "AND o.status NOT IN ('CANCELLED','FAILED') "
			+ "GROUP BY t.id, t.name ORDER BY total DESC LIMIT 10", nativeQuery = true)
	List<Object[]> topToppingsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("branchId") Long branchId);

	/* ----- Cảnh báo (không phụ thuộc khoảng thời gian đang xem) ----- */

	@Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING' AND (:branchId IS NULL OR o.branch.id = :branchId)")
	long countPending(@Param("branchId") Long branchId);

	/** Đơn VNPay chưa thanh toán sau một khoảng thời gian. */
	@Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING' AND o.paymentMethod = 'VNPAY' "
			+ "AND o.paymentStatus <> 'PAID' AND o.createdAt < :before "
			+ "AND (:branchId IS NULL OR o.branch.id = :branchId)")
	long countStaleOnlinePayments(@Param("before") LocalDateTime before, @Param("branchId") Long branchId);

	/** Đơn chờ xác nhận lâu (không tính đơn VNPay đang chờ khách trả tiền). */
	@Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING' AND o.createdAt < :before "
			+ "AND (o.paymentMethod IS NULL OR o.paymentMethod <> 'VNPAY' OR o.paymentStatus = 'PAID') "
			+ "AND (:branchId IS NULL OR o.branch.id = :branchId)")
	long countStaleUnconfirmed(@Param("before") LocalDateTime before, @Param("branchId") Long branchId);
}