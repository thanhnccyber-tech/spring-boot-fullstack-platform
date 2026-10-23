package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Topping;

import java.util.List;
import java.util.Optional;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findByActiveTrue();

    List<Topping> findByActiveTrueAndOutOfStockFalse();

    @Query("SELECT t FROM Topping t " +
           "WHERE (:kw IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "AND (:active IS NULL OR t.active = :active) " +
           "AND (:outOfStock IS NULL OR t.outOfStock = :outOfStock)")
    Page<Topping> searchForAdmin(@Param("kw") String kw,
                                 @Param("active") Boolean active,
                                 @Param("outOfStock") Boolean outOfStock,
                                 Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    Optional<Topping> findByNameIgnoreCase(String name);
}