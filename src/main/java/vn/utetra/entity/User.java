package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    private String address;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Đã hiện lời chào "hội viên mới" ở lần đăng nhập đầu tiên chưa */
    @Column(name = "welcomed")
    private Boolean welcomed = false;

    /** Điểm hội viên (10.000đ = 1 điểm), cộng khi đơn hoàn thành */
    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /* Derived helpers cho JSP */
    public int getPoints() { return loyaltyPoints == null ? 0 : loyaltyPoints; }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> roleName.equals(r.getName()));
    }

    public boolean isAdmin()   { return hasRole(RoleName.ADMIN);   }
    public boolean isManager() { return hasRole(RoleName.MANAGER); }
    public boolean isStaff()   { return hasRole(RoleName.STAFF);   }
    public boolean isShipper() { return hasRole(RoleName.SHIPPER); }
}