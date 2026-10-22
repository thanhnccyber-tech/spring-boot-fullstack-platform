package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "LEFT JOIN FETCH o.shipper " +
           "WHERE o.user.id = :userId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "LEFT JOIN FETCH o.shipper " +
           "WHERE o.branch.id = :branchId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByBranchIdWithUser(@Param("branchId") Long branchId);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "LEFT JOIN FETCH o.shipper " +
           "ORDER BY o.createdAt DESC")
    List<Order> findAllWithUser();

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    List<Order> findBetween(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();

    @Query(value = "SELECT p.name, SUM(od.quantity) AS total " +
            "FROM order_details od JOIN products p ON od.product_id = p.id " +
            "GROUP BY p.id ORDER BY total DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topSellingProducts();

    @Query(value = "SELECT t.name, COUNT(odt.topping_id) AS total " +
            "FROM order_detail_toppings odt " +
            "JOIN toppings t ON odt.topping_id = t.id " +
            "GROUP BY t.id ORDER BY total DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topSellingToppings();

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "LEFT JOIN FETCH o.shipper " +
           "LEFT JOIN FETCH o.details d " +
           "LEFT JOIN FETCH d.product " +
           "LEFT JOIN FETCH d.toppings " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    /**
     * Doanh thu theo từng ngày (bỏ qua CANCELLED).
     * Trả về: [ngày (LocalDate), doanh thu (BigDecimal), số đơn (Long)]
     */
    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.createdAt BETWEEN :from AND :to " +
           "AND o.status <> 'CANCELLED' " +
           "GROUP BY FUNCTION('DATE', o.createdAt) " +
           "ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> revenueByDay(@Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    /**
     * Doanh thu theo từng tháng.
     * Trả về: [năm, tháng, doanh thu, số đơn]
     */
    @Query("SELECT FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt), " +
           "       SUM(o.totalAmount), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.createdAt BETWEEN :from AND :to " +
           "AND o.status <> 'CANCELLED' " +
           "GROUP BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt) " +
           "ORDER BY FUNCTION('YEAR', o.createdAt), FUNCTION('MONTH', o.createdAt)")
    List<Object[]> revenueByMonth(@Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}