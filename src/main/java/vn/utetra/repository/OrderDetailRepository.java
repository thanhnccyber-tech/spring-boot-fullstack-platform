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

    /** Sản phẩm khách đã đặt (không tính đơn huỷ): [productId, tổng số lượng, lần đặt gần nhất]. */
    @Query("SELECT d.product.id, SUM(d.quantity), MAX(d.order.createdAt) FROM OrderDetail d " +
           "WHERE d.order.user.id = :userId AND d.order.status <> 'CANCELLED' " +
           "GROUP BY d.product.id ORDER BY MAX(d.order.createdAt) DESC")
    java.util.List<Object[]> orderedProducts(@Param("userId") Long userId);

    default boolean hasPurchased(Long userId, Long productId) {
        return countCompletedPurchases(userId, productId) > 0;
    }
}
