package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.SizeOption;
import java.util.List;

public interface SizeOptionRepository extends JpaRepository<SizeOption, Long> {
    List<SizeOption> findByActiveTrueOrderBySortOrderAscIdAsc();
    List<SizeOption> findAllByOrderBySortOrderAscIdAsc();
}