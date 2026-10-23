package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.IceLevel;
import java.util.List;

public interface IceLevelRepository extends JpaRepository<IceLevel, Long> {
    List<IceLevel> findByActiveTrueOrderBySortOrderAscIdAsc();
    List<IceLevel> findAllByOrderBySortOrderAscIdAsc();
}