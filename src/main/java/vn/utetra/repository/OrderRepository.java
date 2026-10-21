package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Fetch sẵn user + branch để JSP dùng được khi open-in-view=false
     */
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "WHERE o.user.id = :userId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "WHERE o.branch.id = :branchId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByBranchIdWithUser(@Param("branchId") Long branchId);

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
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

    /**
     * Fetch đầy đủ Order + user + branch + details + product + toppings
     * cho màn hình chi tiết đơn hàng.
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.branch " +
           "LEFT JOIN FETCH o.details d " +
           "LEFT JOIN FETCH d.product " +
           "LEFT JOIN FETCH d.toppings " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}