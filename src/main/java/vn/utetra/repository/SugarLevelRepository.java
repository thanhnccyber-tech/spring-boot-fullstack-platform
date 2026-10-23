package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.SugarLevel;
import java.util.List;

public interface SugarLevelRepository extends JpaRepository<SugarLevel, Long> {
    List<SugarLevel> findByActiveTrueOrderBySortOrderAscIdAsc();
    List<SugarLevel> findAllByOrderBySortOrderAscIdAsc();
}