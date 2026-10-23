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

    /* =====================================================
     * SEARCH CHO KHÁCH — luôn chỉ lấy sản phẩm active = true
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