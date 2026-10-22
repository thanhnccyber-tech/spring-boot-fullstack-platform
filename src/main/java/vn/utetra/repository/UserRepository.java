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

    /**
     * Query search với LEFT JOIN FETCH để luôn khởi tạo branch + roles,
     * tránh LazyInitializationException khi kết hợp Pageable.
     *
     * Lưu ý: dùng DISTINCT để tránh duplicate khi JOIN với roles (ManyToMany).
     */
    @Query(value = "SELECT DISTINCT u FROM User u " +
                   "LEFT JOIN FETCH u.branch " +
                   "LEFT JOIN FETCH u.roles " +
                   "WHERE (:kw IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                   "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                   "  OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                   "AND (:roleName IS NULL OR EXISTS " +
                   "  (SELECT r FROM Role r WHERE r.name = :roleName AND r MEMBER OF u.roles)) " +
                   "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
                   "AND (:enabled IS NULL OR u.enabled = :enabled)",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u " +
                        "WHERE (:kw IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                        "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                        "  OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                        "AND (:roleName IS NULL OR EXISTS " +
                        "  (SELECT r FROM Role r WHERE r.name = :roleName AND r MEMBER OF u.roles)) " +
                        "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
                        "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> search(@Param("kw") String kw,
                      @Param("roleName") String roleName,
                      @Param("branchId") Long branchId,
                      @Param("enabled") Boolean enabled,
                      Pageable pageable);

    /**
     * Lấy user kèm roles + branch để dùng cho form edit.
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.branch " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndBranch(@Param("id") Long id);

    /**
     * Lấy danh sách user có role SHIPPER (để Admin phân công giao hàng).
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r " +
           "LEFT JOIN FETCH u.branch " +
           "WHERE r.name = 'ROLE_SHIPPER' " +
           "AND u.enabled = true " +
           "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
           "ORDER BY u.fullName")
    List<User> findActiveShippers(@Param("branchId") Long branchId);
}