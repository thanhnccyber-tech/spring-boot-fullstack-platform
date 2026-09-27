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
    @Autowired private CategoryRepository catRepo;
    @Autowired private ProductRepository prodRepo;
    @Autowired private ToppingRepository topRepo;
    @Autowired private BranchRepository branchRepo;
    @Autowired private PasswordEncoder encoder;

    @Override
    public void run(String... args) {

        // ==================== ROLES ====================
        Role adminRole = roleRepo.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_ADMIN"); r.setDescription("Quản trị viên hệ thống");
            return roleRepo.save(r);
        });
        Role managerRole = roleRepo.findByName("ROLE_MANAGER").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_MANAGER"); r.setDescription("Quản lý chi nhánh");
            return roleRepo.save(r);
        });
        Role staffRole = roleRepo.findByName("ROLE_STAFF").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_STAFF"); r.setDescription("Nhân viên");
            return roleRepo.save(r);
        });
        Role userRole = roleRepo.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_USER"); r.setDescription("Khách hàng");
            return roleRepo.save(r);
        });

        // ==================== BRANCHES (tạo trước để gán cho manager/staff) ====================
        if (branchRepo.count() == 0) {
            Branch b1 = new Branch();
            b1.setName("UTeTra Quận 1");
            b1.setAddress("123 Nguyễn Huệ, Quận 1, TP.HCM");
            b1.setPhone("0901234567");
            branchRepo.save(b1);

            Branch b2 = new Branch();
            b2.setName("UTeTra Thủ Đức");
            b2.setAddress("45 Võ Văn Ngân, Thủ Đức, TP.HCM");
            b2.setPhone("0909876543");
            branchRepo.save(b2);
        }
        Branch firstBranch = branchRepo.findAll().stream().findFirst().orElse(null);

        // ==================== SEED USERS ====================
        if (!userRepo.existsByEmail("admin@utetra.vn")) {
            User admin = new User();
            admin.setEmail("admin@utetra.vn");
            admin.setPassword(encoder.encode("admin123"));
            admin.setFullName("Admin UTeTra");
            admin.setRoles(Set.of(adminRole));
            userRepo.save(admin);
        }
        if (!userRepo.existsByEmail("manager@utetra.vn")) {
            User m = new User();
            m.setEmail("manager@utetra.vn");
            m.setPassword(encoder.encode("manager123"));
            m.setFullName("Quản lý Quận 1");
            m.setRoles(Set.of(managerRole));
            m.setBranch(firstBranch);
            userRepo.save(m);
        }
        if (!userRepo.existsByEmail("staff@utetra.vn")) {
            User staff = new User();
            staff.setEmail("staff@utetra.vn");
            staff.setPassword(encoder.encode("staff123"));
            staff.setFullName("Nhân viên UTeTra");
            staff.setRoles(Set.of(staffRole));
            staff.setBranch(firstBranch);
            userRepo.save(staff);
        }
        if (!userRepo.existsByEmail("user@utetra.vn")) {
            User user = new User();
            user.setEmail("user@utetra.vn");
            user.setPassword(encoder.encode("user123"));
            user.setFullName("Khách hàng UTeTra");
            user.setRoles(Set.of(userRole));
            userRepo.save(user);
        }

        // ==================== TOPPINGS ====================
        if (topRepo.count() == 0) {
            Topping t1 = new Topping(); t1.setName("Trân châu đen"); t1.setPrice(new BigDecimal("8000")); topRepo.save(t1);
            Topping t2 = new Topping(); t2.setName("Pudding trứng"); t2.setPrice(new BigDecimal("10000")); topRepo.save(t2);
            Topping t3 = new Topping(); t3.setName("Thạch dừa");     t3.setPrice(new BigDecimal("7000"));  topRepo.save(t3);
            Topping t4 = new Topping(); t4.setName("Kem phô mai");   t4.setPrice(new BigDecimal("12000")); topRepo.save(t4);
        }

        // ==================== CATEGORIES ====================
        if (catRepo.count() == 0) {
            Category c1 = new Category(); c1.setName("Trà sữa");       c1.setDescription("Các loại trà sữa thơm ngon"); catRepo.save(c1);
            Category c2 = new Category(); c2.setName("Trà trái cây");  c2.setDescription("Trà trái cây tươi mát");      catRepo.save(c2);
            Category c3 = new Category(); c3.setName("Bánh ngọt");     c3.setDescription("Bánh ngọt ăn kèm");           catRepo.save(c3);
        }

        // ==================== PRODUCTS ====================
        if (prodRepo.count() == 0) {
            Category ts  = catRepo.findAll().get(0);
            Category ttc = catRepo.findAll().get(1);
            List<Topping> tops = topRepo.findAll();

            Product p1 = new Product();
            p1.setName("Trà sữa trân châu UTe");
            p1.setDescription("Trà sữa đặc trưng với trân châu dai mềm");
            p1.setBasePrice(new BigDecimal("35000"));
            p1.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
            p1.setFeatured(true);
            p1.setCategory(ts);
            p1.setToppings(tops);
            prodRepo.save(p1);

            Product p2 = new Product();
            p2.setName("Trà sữa matcha");
            p2.setDescription("Matcha Nhật Bản đậm đà");
            p2.setBasePrice(new BigDecimal("40000"));
            p2.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
            p2.setIsNew(true);
            p2.setCategory(ts);
            p2.setToppings(tops);
            prodRepo.save(p2);

            Product p3 = new Product();
            p3.setName("Trà đào cam sả");
            p3.setDescription("Trà đào tươi mát");
            p3.setBasePrice(new BigDecimal("38000"));
            p3.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
            p3.setFeatured(true);
            p3.setCategory(ttc);
            prodRepo.save(p3);
        }
    }
}