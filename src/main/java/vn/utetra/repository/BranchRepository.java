package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.Branch;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByActiveTrue();

    @Query("SELECT b FROM Branch b " +
           "WHERE (:kw IS NULL " +
           "     OR LOWER(b.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "     OR LOWER(b.address) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "     OR LOWER(b.phone) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "AND (:active IS NULL OR b.active = :active)")
    Page<Branch> searchForAdmin(@Param("kw") String kw,
                                @Param("active") Boolean active,
                                Pageable pageable);

    Optional<Branch> findByNameIgnoreCase(String name);
}