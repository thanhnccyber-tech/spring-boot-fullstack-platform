package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByFeaturedTrueAndActiveTrue();
    List<Product> findByIsNewTrueAndActiveTrue();
    Optional<Product> findFirstByNameIgnoreCaseAndActiveTrue(String name);
    boolean existsByNameIgnoreCase(String name);

    /* =====================================================
     * SEARCH CHO KHÁCH — luôn chỉ lấy sản phẩm active = true
     * (dùng cho sắp xếp theo giá / tên / mới nhất)
     * ===================================================== */
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "AND (:catId IS NULL OR p.category.id = :catId) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> search(@Param("kw") String kw,
                         @Param("catId") Long catId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);

    /* =====================================================
     * SẮP XẾP ĐẶC BIỆT (native SQL — dùng subquery GROUP BY để
     * tránh nhân bản dòng và đảm bảo pagination đúng)
     * ===================================================== */

    /** Bán chạy: tổng số lượng đã bán trong các đơn HOÀN THÀNH, giảm dần. */
    @Query(value =
            "SELECT p.* FROM products p " +
            "LEFT JOIN (" +
            "  SELECT od.product_id, COALESCE(SUM(od.quantity), 0) AS sold " +
            "  FROM order_details od " +
            "  JOIN orders o ON od.order_id = o.id " +
            "  WHERE o.status = 'COMPLETED' " +
            "  GROUP BY od.product_id" +
            ") sold ON sold.product_id = p.id " +
            "WHERE p.active = true " +
            "  AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "  AND (:catId IS NULL OR p.category_id = :catId) " +
            "  AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
            "  AND (:maxPrice IS NULL OR p.base_price <= :maxPrice) " +
            "ORDER BY COALESCE(sold.sold, 0) DESC, p.created_at DESC, p.id DESC",
            countQuery = "SELECT COUNT(*) FROM products p " +
                         "WHERE p.active = true " +
                         "  AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                         "  AND (:catId IS NULL OR p.category_id = :catId) " +
                         "  AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
                         "  AND (:maxPrice IS NULL OR p.base_price <= :maxPrice)",
            nativeQuery = true)
    Page<Product> searchBestSeller(@Param("kw") String kw,
                                   @Param("catId") Long catId,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    /** Đánh giá cao: điểm trung bình giảm dần, số lượt đánh giá làm tie-break. */
    @Query(value =
            "SELECT p.* FROM products p " +
            "LEFT JOIN (" +
            "  SELECT r.product_id, AVG(r.rating) AS avg_rating, COUNT(r.id) AS review_count " +
            "  FROM reviews r " +
            "  GROUP BY r.product_id" +
            ") rv ON rv.product_id = p.id " +
            "WHERE p.active = true " +
            "  AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "  AND (:catId IS NULL OR p.category_id = :catId) " +
            "  AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
            "  AND (:maxPrice IS NULL OR p.base_price <= :maxPrice) " +
            "ORDER BY COALESCE(rv.avg_rating, 0) DESC, " +
            "         COALESCE(rv.review_count, 0) DESC, " +
            "         p.created_at DESC, p.id DESC",
            countQuery = "SELECT COUNT(*) FROM products p " +
                         "WHERE p.active = true " +
                         "  AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                         "  AND (:catId IS NULL OR p.category_id = :catId) " +
                         "  AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
                         "  AND (:maxPrice IS NULL OR p.base_price <= :maxPrice)",
            nativeQuery = true)
    Page<Product> searchTopRated(@Param("kw") String kw,
                                 @Param("catId") Long catId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);

    /** Yêu thích: số lượt yêu thích giảm dần. */
    @Query(value =
            "SELECT p.* FROM products p " +
            "LEFT JOIN (" +
            "  SELECT f.product_id, COUNT(f.id) AS fav_count " +
            "  FROM favorites f " +
            "  GROUP BY f.product_id" +
            ") fv ON fv.product_id = p.id " +
            "WHERE p.active = true " +
            "  AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "  AND (:catId IS NULL OR p.category_id = :catId) " +
            "  AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
            "  AND (:maxPrice IS NULL OR p.base_price <= :maxPrice) " +
            "ORDER BY COALESCE(fv.fav_count, 0) DESC, p.created_at DESC, p.id DESC",
            countQuery = "SELECT COUNT(*) FROM products p " +
                         "WHERE p.active = true " +
                         "  AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                         "  AND (:catId IS NULL OR p.category_id = :catId) " +
                         "  AND (:minPrice IS NULL OR p.base_price >= :minPrice) " +
                         "  AND (:maxPrice IS NULL OR p.base_price <= :maxPrice)",
            nativeQuery = true)
    Page<Product> searchMostFavorite(@Param("kw") String kw,
                                     @Param("catId") Long catId,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     Pageable pageable);

    /* =====================================================
     * SEARCH CHO ADMIN — lọc được cả active, fetch luôn category
     * ===================================================== */
    @Query(value = "SELECT p FROM Product p " +
                   "LEFT JOIN FETCH p.category c " +
                   "WHERE (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                   "AND (:catId IS NULL OR c.id = :catId) " +
                   "AND (:active IS NULL OR p.active = :active) " +
                   "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                   "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)",
           countQuery = "SELECT COUNT(p) FROM Product p " +
                        "LEFT JOIN p.category c " +
                        "WHERE (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                        "AND (:catId IS NULL OR c.id = :catId) " +
                        "AND (:active IS NULL OR p.active = :active) " +
                        "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> searchForAdmin(@Param("kw") String kw,
                                 @Param("catId") Long catId,
                                 @Param("active") Boolean active,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.id DESC")
    List<Product> findAllWithCategory();

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.toppings " +
           "LEFT JOIN FETCH p.sizes " +
           "LEFT JOIN FETCH p.sugars " +
           "LEFT JOIN FETCH p.ices " +
           "LEFT JOIN FETCH p.defaultSize " +
           "LEFT JOIN FETCH p.defaultSugar " +
           "LEFT JOIN FETCH p.defaultIce " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.sizes " +
           "LEFT JOIN FETCH p.sugars " +
           "LEFT JOIN FETCH p.ices " +
           "LEFT JOIN FETCH p.defaultSize " +
           "LEFT JOIN FETCH p.defaultSugar " +
           "LEFT JOIN FETCH p.defaultIce " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithDefaults(@Param("id") Long id);
}