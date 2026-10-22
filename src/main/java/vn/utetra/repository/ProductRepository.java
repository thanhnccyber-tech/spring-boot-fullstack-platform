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

    /**
     * Lấy tất cả sản phẩm và FETCH JOIN luôn Category.
     * Dùng cho màn hình admin/products để tránh LazyInitializationException.
     * Sắp xếp theo ID giảm dần để sản phẩm mới nhất lên đầu.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.id DESC")
    List<Product> findAllWithCategory();

    /**
     * Lấy 1 sản phẩm và FETCH JOIN luôn Category và Toppings.
     * Dùng cho màn hình admin/product-form (edit) để tránh LazyInitializationException.
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.toppings " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);
}