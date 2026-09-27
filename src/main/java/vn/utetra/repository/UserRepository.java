package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetToken(String token);

    List<User> findByRoles_Name(String roleName);

    /**
     * Tìm kiếm đa trường + lọc kết hợp + phân trang.
     * Tất cả tham số đều optional (null = bỏ qua filter đó).
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:kw IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR LOWER(u.phone)    LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "AND (:roleName IS NULL OR EXISTS " +
           "       (SELECT r FROM Role r WHERE r.name = :roleName AND r MEMBER OF u.roles)) " +
           "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> search(@Param("kw")       String kw,
                      @Param("roleName") String roleName,
                      @Param("branchId") Long branchId,
                      @Param("enabled")  Boolean enabled,
                      Pageable pageable);
}