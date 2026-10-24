package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    /** Khách đã có đơn HOÀN THÀNH chứa sản phẩm này chưa (dùng cho "đã mua hàng" khi đánh giá). */
    @Query("SELECT COUNT(d) FROM OrderDetail d " +
           "WHERE d.order.user.id = :userId AND d.product.id = :productId " +
           "AND d.order.status = 'COMPLETED'")
    long countCompletedPurchases(@Param("userId") Long userId, @Param("productId") Long productId);

    default boolean hasPurchased(Long userId, Long productId) {
        return countCompletedPurchases(userId, productId) > 0;
    }
}
