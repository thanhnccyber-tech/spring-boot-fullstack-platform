package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * @EntityGraph giúp fetch sẵn branch VÀ roles mà KHÔNG bị mất khi kết hợp Pageable.
     * Đây là cách chuẩn để fix LazyInitializationException với Page.
     * - branch: tránh lỗi khi JSP truy cập u.branch.name
     * - roles: tránh lỗi khi JSP truy cập u.roles (dù đã EAGER, nhưng pagination có thể làm lazy)
     */
    @EntityGraph(attributePaths = {"branch", "roles"})
    @Query("SELECT u FROM User u WHERE " +
           "(:kw IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
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
     * Lấy user kèm roles và branch để dùng cho form edit.
     * Tránh LazyInitializationException khi JSP truy cập u.roles hoặc u.branch.
     */
    @EntityGraph(attributePaths = {"branch", "roles"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndBranch(@Param("id") Long id);
}