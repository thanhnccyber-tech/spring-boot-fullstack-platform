package vn.utetra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private BranchRepository branchRepo;
    @Autowired private PasswordEncoder encoder;

    @Value("${app.seed-demo-accounts:true}")
    private boolean seedDemoAccounts;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemoAccounts) {
            System.out.println("[DataInitializer] seed-demo-accounts=false, skipping password reset");
            return;
        }

        Branch b1 = branchRepo.findAll().stream().findFirst().orElse(null);

        reset("admin@utetra.vn",   "admin123",   RoleName.ADMIN,   null, "Admin UTETra",   "0901111111", null);
        reset("manager@utetra.vn", "manager123", RoleName.MANAGER, b1,   "Quản Lý Demo",   "0902222200", null);
        reset("staff@utetra.vn",   "staff123",   RoleName.STAFF,   b1,   "Nhân Viên Demo", "0903333300", null);
        reset("shipper@utetra.vn", "shipper123", RoleName.SHIPPER, b1,   "Shipper Demo",   "0904444400", null);
        reset("user@utetra.vn",    "user123",    RoleName.USER,    null, "Khách Demo",     "0905555500", "50 Lê Lợi, Q.1, TP.HCM");
    }

    private void reset(String email, String rawPass, String roleName,
                       Branch branch, String fullName, String phone, String address) {
        Optional<User> opt = userRepo.findByEmail(email);
        if (opt.isPresent()) {
            User u = opt.get();
            u.setPassword(encoder.encode(rawPass));
            u.setEnabled(true);
            userRepo.save(u);
            System.out.println("[DataInitializer] Reset password: " + email + " → " + rawPass);
            return;
        }

        // Fallback: user chưa có (DB chưa chạy Data.txt) → tạo mới
        Role role = roleRepo.findByName(roleName).orElseGet(() -> {
            Role r = new Role();
            r.setName(roleName);
            r.setDescription(roleName);
            return roleRepo.save(r);
        });
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPass));
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setAddress(address);
        u.setEnabled(true);
        u.setWelcomed(true);
        u.setRoles(new HashSet<>(Set.of(role)));
        u.setBranch(branch);
        userRepo.save(u);
        System.out.println("[DataInitializer] Tạo mới (fallback): " + email + " → " + rawPass);
    }
}