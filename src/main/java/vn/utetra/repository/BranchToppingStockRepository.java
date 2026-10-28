package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.BranchToppingStock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BranchToppingStockRepository extends JpaRepository<BranchToppingStock, Long> {
    Optional<BranchToppingStock> findByBranchIdAndToppingId(Long branchId, Long toppingId);
    List<BranchToppingStock> findByBranchIdAndToppingIdIn(Long branchId, Collection<Long> toppingIds);
}