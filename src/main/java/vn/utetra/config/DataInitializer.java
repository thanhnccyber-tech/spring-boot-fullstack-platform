package vn.utetra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private BranchRepository branchRepo;
    @Autowired private CategoryRepository catRepo;
    @Autowired private ProductRepository prodRepo;
    @Autowired private ToppingRepository topRepo;
    @Autowired private PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        // ===== Roles =====
        Role adminRole   = ensureRole(RoleName.ADMIN,   "Quản trị viên");
        Role managerRole = ensureRole(RoleName.MANAGER, "Quản lý chi nhánh");
        Role staffRole   = ensureRole(RoleName.STAFF,   "Nhân viên pha chế");
        Role shipperRole = ensureRole(RoleName.SHIPPER, "Nhân viên giao hàng");
        Role userRole    = ensureRole(RoleName.USER,    "Khách hàng");

        // ===== Branches =====
        if (branchRepo.count() == 0) {
            Branch b1 = new Branch();
            b1.setName("UTeTra Quận 1");
            b1.setAddress("123 Nguyễn Huệ, Q.1, TP.HCM");
            b1.setPhone("0901234567");
            branchRepo.save(b1);

            Branch b2 = new Branch();
            b2.setName("UTeTra Thủ Đức");
            b2.setAddress("45 Võ Văn Ngân, TP.Thủ Đức, TP.HCM");
            b2.setPhone("0909876543");
            branchRepo.save(b2);
        }
        Branch firstBranch = branchRepo.findAll().stream().findFirst().orElse(null);

        // ===== Seed Users =====
        seedUser("admin@utetra.vn",   "admin123",   "Admin UTeTra",     adminRole,   null);
        seedUser("manager@utetra.vn", "manager123", "Quản lý Q1",       managerRole, firstBranch);
        seedUser("staff@utetra.vn",   "staff123",   "Nhân viên Q1",     staffRole,   firstBranch);
        seedUser("shipper@utetra.vn", "ship123",    "Shipper Q1",       shipperRole, firstBranch);
        seedUser("user@utetra.vn",    "user123",    "Khách hàng mẫu",   userRole,    null);

        // ===== Categories =====
        if (catRepo.count() == 0) {
            createCategory("Trà sữa",      "Các loại trà sữa thơm ngon");
            createCategory("Trà trái cây", "Trà trái cây tươi mát");
            createCategory("Bánh ngọt",    "Bánh ngọt ăn kèm");
        }

        // ===== Toppings =====
        if (topRepo.count() == 0) {
            createTopping("Trân châu đen",   "8000");
            createTopping("Pudding trứng",   "10000");
            createTopping("Thạch dừa",       "7000");
            createTopping("Kem phô mai",     "12000");
        }

        // ===== Products =====
        if (prodRepo.count() == 0) {
            Category ts  = catRepo.findAll().get(0);
            Category ttc = catRepo.findAll().get(1);
            List<Topping> tops = topRepo.findAll();

            createProduct("Trà sữa trân châu UTe",
                    "Trà sữa đặc trưng với trân châu dai mềm",
                    "35000", true, false, ts, tops);
            createProduct("Trà sữa matcha",
                    "Matcha Nhật Bản đậm đà",
                    "40000", false, true, ts, tops);
            createProduct("Trà đào cam sả",
                    "Trà đào tươi mát kết hợp cam sả",
                    "38000", true, false, ttc, null);
        }
    }

    private Role ensureRole(String name, String description) {
        return roleRepo.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            r.setDescription(description);
            return roleRepo.save(r);
        });
    }

    private void seedUser(String email, String rawPass, String fullName,
                          Role role, Branch branch) {
        if (userRepo.existsByEmail(email)) return;
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPass));
        u.setFullName(fullName);
        u.setEnabled(true);
        u.setRoles(Set.of(role));
        u.setBranch(branch);
        userRepo.save(u);
    }

    private void createCategory(String name, String desc) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(desc);
        catRepo.save(c);
    }

    private void createTopping(String name, String price) {
        Topping t = new Topping();
        t.setName(name);
        t.setPrice(new BigDecimal(price));
        topRepo.save(t);
    }

    private void createProduct(String name, String desc, String price,
                               boolean featured, boolean isNew,
                               Category cat, List<Topping> tops) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setBasePrice(new BigDecimal(price));
        p.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        p.setFeatured(featured);
        p.setIsNew(isNew);
        p.setCategory(cat);
        p.setToppings(tops);
        prodRepo.save(p);
    }
}