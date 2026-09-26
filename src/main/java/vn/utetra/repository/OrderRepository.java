package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Order;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    List<Order> findBetween(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();

    @Query(value = "SELECT p.name, SUM(od.quantity) as total " +
            "FROM order_details od JOIN products p ON od.product_id = p.id " +
            "GROUP BY p.id ORDER BY total DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topSellingProducts();
}