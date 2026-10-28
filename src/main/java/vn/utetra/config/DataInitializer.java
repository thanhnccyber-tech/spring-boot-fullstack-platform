package vn.utetra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
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
    @Autowired private SizeOptionRepository sizeRepo;
    @Autowired private SugarLevelRepository sugarRepo;
    @Autowired private IceLevelRepository iceRepo;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private PromotionRepository promoRepo;
    @Autowired private PasswordEncoder encoder;

    /** Chỉ tạo tài khoản demo khi bật rõ ràng (dev local). Mặc định tắt để không có admin/admin123 trên server thật. */
    @Value("${app.seed-demo-accounts:false}")
    private boolean seedDemoAccounts;

    @Override
    public void run(String... args) {
        Role adminRole   = ensureRole(RoleName.ADMIN,   "Quản trị viên");
        Role managerRole = ensureRole(RoleName.MANAGER, "Quản lý chi nhánh");
        Role staffRole   = ensureRole(RoleName.STAFF,   "Nhân viên pha chế");
        Role shipperRole = ensureRole(RoleName.SHIPPER, "Nhân viên giao hàng");
        Role userRole    = ensureRole(RoleName.USER,    "Khách hàng");

        if (branchRepo.count() == 0) {
            Branch b1 = new Branch();
            b1.setName("UTeTra Quận 1");
            b1.setAddress("123 Nguyễn Huệ, Q.1, TP.HCM");
            b1.setPhone("0901234567");
            b1.setLatitude(10.7743);
            b1.setLongitude(106.7040);
            branchRepo.save(b1);

            Branch b2 = new Branch();
            b2.setName("UTeTra Thủ Đức");
            b2.setAddress("45 Võ Văn Ngân, TP.Thủ Đức, TP.HCM");
            b2.setPhone("0909876543");
            b2.setLatitude(10.8505);
            b2.setLongitude(106.7717);
            branchRepo.save(b2);
        }
        Branch firstBranch = branchRepo.findAll().stream().findFirst().orElse(null);

        if (seedDemoAccounts) {
            seedUser("admin@utetra.vn",   "admin123",   "Admin UTeTra",     adminRole,   null);
            seedUser("manager@utetra.vn", "manager123", "Quản lý Q1",       managerRole, firstBranch);
            seedUser("staff@utetra.vn",   "staff123",   "Nhân viên Q1",     staffRole,   firstBranch);
            seedUser("shipper@utetra.vn", "ship123",    "Shipper Q1",       shipperRole, firstBranch);
            seedUser("user@utetra.vn",    "user123",    "Khách hàng mẫu",   userRole,    null);
        }

        if (sizeRepo.count() == 0) {
            createSize("S", "0", 1);
            createSize("M", "5000", 2);
            createSize("L", "10000", 3);
        }

        if (sugarRepo.count() == 0) {
            createSugar("0%", 0, 1);
            createSugar("30%", 30, 2);
            createSugar("50%", 50, 3);
            createSugar("100%", 100, 4);
        }

        if (iceRepo.count() == 0) {
            createIce("Không đá", 0, 1);
            createIce("Ít đá", 30, 2);
            createIce("Vừa đá", 50, 3);
            createIce("Nhiều đá", 100, 4);
        }

        if (catRepo.count() == 0) {
            createCategory("Trà sữa",      "Các loại trà sữa thơm ngon");
            createCategory("Trà trái cây", "Trà trái cây tươi mát");
            createCategory("Bánh ngọt",    "Bánh ngọt ăn kèm");
        }

        if (topRepo.count() == 0) {
            createTopping("Trân châu đen",   "8000");
            createTopping("Pudding trứng",   "10000");
            createTopping("Thạch dừa",       "7000");
            createTopping("Kem phô mai",     "12000");
        }

        if (prodRepo.count() == 0) {
            Category ts  = catRepo.findAll().get(0);
            Category ttc = catRepo.findAll().get(1);
            List<Topping> tops = topRepo.findAll();
            SizeOption sSize   = sizeRepo.findAllByOrderBySortOrderAscIdAsc().get(0);
            SugarLevel sSugar  = sugarRepo.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                    .filter(x -> x.getPercent() == 50).findFirst().orElse(null);
            IceLevel sIce      = iceRepo.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                    .filter(x -> x.getPercent() == 50).findFirst().orElse(null);

            createProduct("Trà sữa trân châu UTe",
                    "Trà sữa đặc trưng với trân châu dai mềm",
                    "35000", true, false, ts, tops, sSize, sSugar, sIce);
            createProduct("Trà sữa matcha",
                    "Matcha Nhật Bản đậm đà",
                    "40000", false, true, ts, tops, sSize, sSugar, sIce);
            createProduct("Trà đào cam sả",
                    "Trà đào tươi mát kết hợp cam sả",
                    "38000", true, false, ttc, null, sSize, sSugar, sIce);
        }

        seedBookMenu();
        seedMarketing();
    }

    /** Thêm 7 món của Sổ thực đơn nếu chưa có (so khớp theo tên, không ghi đè món admin đã sửa). */
    private void seedBookMenu() {
        List<Category> cats = catRepo.findAll();
        if (cats.isEmpty()) return;
        Category milk = cats.stream().filter(c -> "Trà sữa".equalsIgnoreCase(c.getName())).findFirst().orElse(cats.get(0));
        Category fruit = cats.stream().filter(c -> "Trà trái cây".equalsIgnoreCase(c.getName())).findFirst().orElse(milk);
        List<Topping> tops = topRepo.findAll();
        SizeOption defSize = sizeRepo.findAllByOrderBySortOrderAscIdAsc().stream().findFirst().orElse(null);
        SugarLevel defSugar = sugarRepo.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .filter(x -> x.getPercent() == 50).findFirst().orElse(null);
        IceLevel defIce = iceRepo.findByActiveTrueOrderBySortOrderAscIdAsc().stream()
                .filter(x -> x.getPercent() == 50).findFirst().orElse(null);

        for (BookMenu.Item item : BookMenu.ITEMS) {
            if (prodRepo.existsByNameIgnoreCase(item.name())) continue;
            createProduct(item.name(), item.description(), item.price(), false, false,
                    item.fruitTea() ? fruit : milk, item.milkTea() ? tops : null, defSize, defSugar, defIce);
            prodRepo.findAll().stream().filter(p -> item.name().equalsIgnoreCase(p.getName())).findFirst()
                    .ifPresent(p -> { p.setImageUrl(item.imageUrl()); prodRepo.save(p); });
        }
    }

    private void seedMarketing() {
        if (voucherRepo.count() == 0) {
            Voucher a = new Voucher();
            a.setCode("WELCOME10");
            a.setDescription("Giảm 10% (tối đa 30.000đ) cho đơn từ 50.000đ");
            a.setDiscountType(Voucher.PERCENT);
            a.setDiscountValue(new BigDecimal("10"));
            a.setMinOrderAmount(new BigDecimal("50000"));
            a.setMaxDiscountAmount(new BigDecimal("30000"));
            voucherRepo.save(a);

            Voucher b = new Voucher();
            b.setCode("GIAM15K");
            b.setDescription("Giảm 15.000đ cho đơn từ 80.000đ");
            b.setDiscountType(Voucher.AMOUNT);
            b.setDiscountValue(new BigDecimal("15000"));
            b.setMinOrderAmount(new BigDecimal("80000"));
            voucherRepo.save(b);
        }
        if (promoRepo.count() == 0) {
            createPromo("Khai trương: giảm 10% đơn đầu tiên",
                    "Nhập mã WELCOME10 khi thanh toán để được giảm 10%, tối đa 30.000đ.",
                    "Chào mừng bạn đến với UTeTra! Nhập mã WELCOME10 ở bước thanh toán để được giảm 10% (tối đa 30.000đ) cho đơn từ 50.000đ.",
                    "WELCOME10");
            createPromo("Giảm ngay 15.000đ cho đơn từ 80.000đ",
                    "Nhập mã GIAM15K, áp dụng cho cả giao hàng và đến lấy tại cửa hàng.",
                    "Đặt nhóm cùng bạn bè? Nhập mã GIAM15K để được giảm 15.000đ cho đơn từ 80.000đ.",
                    "GIAM15K");
            createPromo("Tích điểm đổi quà: 10.000đ = 1 điểm",
                    "Mỗi đơn hoàn thành đều được cộng điểm hội viên tự động.",
                    "Mỗi 10.000đ bạn chi tiêu được 1 điểm hội viên. Điểm được cộng sau khi đơn hoàn thành.",
                    null);
        }
    }

    private void createPromo(String title, String summary, String content, String voucher) {
        Promotion p = new Promotion();
        p.setTitle(title);
        p.setSummary(summary);
        p.setContent(content);
        p.setVoucherCode(voucher);
        promoRepo.save(p);
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

    private void createSize(String name, String extra, int order) {
        SizeOption s = new SizeOption();
        s.setName(name);
        s.setExtraPrice(new BigDecimal(extra));
        s.setSortOrder(order);
        sizeRepo.save(s);
    }

    private void createSugar(String name, int percent, int order) {
        SugarLevel s = new SugarLevel();
        s.setName(name);
        s.setPercent(percent);
        s.setSortOrder(order);
        sugarRepo.save(s);
    }

    private void createIce(String name, int percent, int order) {
        IceLevel s = new IceLevel();
        s.setName(name);
        s.setPercent(percent);
        s.setSortOrder(order);
        iceRepo.save(s);
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
                               Category cat, List<Topping> tops,
                               SizeOption defSize, SugarLevel defSugar, IceLevel defIce) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setBasePrice(new BigDecimal(price));
        p.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        p.setFeatured(featured);
        p.setIsNew(isNew);
        p.setCategory(cat);
        p.setToppings(tops == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tops));
        p.setAllowSize(true);
        p.setAllowSugar(true);
        p.setAllowIce(true);
        p.setDefaultSize(defSize);
        p.setDefaultSugar(defSugar);
        p.setDefaultIce(defIce);
        prodRepo.save(p);
    }
}