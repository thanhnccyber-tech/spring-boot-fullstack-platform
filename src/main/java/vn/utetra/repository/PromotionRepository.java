package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Promotion;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByActiveTrueOrderByCreatedAtDescIdDesc();

    List<Promotion> findAllByOrderByCreatedAtDescIdDesc();

    @Modifying
    @Query("UPDATE Promotion p SET p.views = p.views + 1 WHERE p.id = :id")
    void increaseViews(@Param("id") Long id);
}
