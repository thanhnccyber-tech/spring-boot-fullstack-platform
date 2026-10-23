package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderBySortOrderAscIdAsc();

    List<Category> findAllByOrderBySortOrderAscIdAsc();

    @Query("SELECT c FROM Category c " +
           "WHERE (:kw IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "AND (:active IS NULL OR c.active = :active)")
    Page<Category> searchForAdmin(@Param("kw") String kw,
                                  @Param("active") Boolean active,
                                  Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT p.category.id, COUNT(p) FROM Product p " +
           "WHERE p.category.id IN :ids GROUP BY p.category.id")
    List<Object[]> countProductsByCategoryIds(@Param("ids") List<Long> ids);
}