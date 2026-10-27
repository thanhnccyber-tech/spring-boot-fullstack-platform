package vn.utetra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.utetra.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.branch WHERE u.email = :email")
    Optional<User> findByEmailWithBranch(@Param("email") String email);

    @Query(value = "SELECT u FROM User u " +
                   "WHERE (:kw IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                   "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                   "  OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                   "AND (:roleName IS NULL OR (" +
                   "     EXISTS (SELECT r FROM Role r WHERE r.name = :roleName AND r MEMBER OF u.roles) " +
                   "     AND (:excludeRoleName IS NULL OR NOT EXISTS " +
                   "          (SELECT r2 FROM Role r2 WHERE r2.name = :excludeRoleName AND r2 MEMBER OF u.roles)))) " +
                   "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
                   "AND (:enabled IS NULL OR u.enabled = :enabled)",
           countQuery = "SELECT COUNT(u) FROM User u " +
                        "WHERE (:kw IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                        "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) " +
                        "  OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                        "AND (:roleName IS NULL OR (" +
                        "     EXISTS (SELECT r FROM Role r WHERE r.name = :roleName AND r MEMBER OF u.roles) " +
                        "     AND (:excludeRoleName IS NULL OR NOT EXISTS " +
                        "          (SELECT r2 FROM Role r2 WHERE r2.name = :excludeRoleName AND r2 MEMBER OF u.roles)))) " +
                        "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
                        "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> search(@Param("kw") String kw,
                      @Param("roleName") String roleName,
                      @Param("excludeRoleName") String excludeRoleName,
                      @Param("branchId") Long branchId,
                      @Param("enabled") Boolean enabled,
                      Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.branch " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.id IN :ids")
    List<User> findByIdsWithRolesAndBranch(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.branch " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndBranch(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r " +
           "LEFT JOIN FETCH u.branch " +
           "WHERE r.name = 'ROLE_SHIPPER' " +
           "AND u.enabled = true " +
           "AND (:branchId IS NULL OR u.branch.id = :branchId) " +
           "ORDER BY u.fullName")
    List<User> findActiveShippers(@Param("branchId") Long branchId);

    /** Trừ điểm nguyên tử; trả 0 nếu không đủ điểm (chống 2 đơn cùng lúc dùng chung điểm). */
    @Modifying
    @Query("UPDATE User u SET u.loyaltyPoints = u.loyaltyPoints - :p WHERE u.id = :id AND u.loyaltyPoints >= :p")
    int spendPoints(@Param("id") Long id, @Param("p") int p);

    @Modifying
    @Query("UPDATE User u SET u.loyaltyPoints = COALESCE(u.loyaltyPoints, 0) + :p WHERE u.id = :id")
    int addPoints(@Param("id") Long id, @Param("p") int p);

    /** Số tài khoản đã kích hoạt, đăng ký trong khoảng thời gian. */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.createdAt BETWEEN :from AND :to")
    long countNewBetween(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}