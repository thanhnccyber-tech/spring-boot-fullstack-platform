package vn.utetra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.utetra.entity.BranchProductStock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BranchProductStockRepository extends JpaRepository<BranchProductStock, Long> {
    Optional<BranchProductStock> findByBranchIdAndProductId(Long branchId, Long productId);
    List<BranchProductStock> findByBranchIdAndProductIdIn(Long branchId, Collection<Long> productIds);
}