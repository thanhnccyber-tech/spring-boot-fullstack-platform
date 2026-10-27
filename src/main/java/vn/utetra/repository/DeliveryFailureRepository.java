package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.DeliveryFailure;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryFailureRepository extends JpaRepository<DeliveryFailure, Long> {

    @Query("SELECT f FROM DeliveryFailure f " +
            "JOIN FETCH f.order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.branch " +
            "JOIN FETCH f.shipper " +
            "WHERE f.shipper.id = :shipperId " +
            "ORDER BY f.failedAt DESC")
    List<DeliveryFailure> findHistoryByShipperId(@Param("shipperId") Long shipperId);

    @Query("SELECT f FROM DeliveryFailure f " +
            "JOIN FETCH f.shipper " +
            "WHERE f.order.id = :orderId " +
            "ORDER BY f.failedAt DESC")
    List<DeliveryFailure> findHistoryByOrderId(@Param("orderId") Long orderId);

    boolean existsByOrder_IdAndShipper_Id(Long orderId, Long shipperId);

    long countByShipperIdAndFailedAtBetween(Long shipperId, LocalDateTime from, LocalDateTime to);
}
