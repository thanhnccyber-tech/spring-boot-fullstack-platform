package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.*;
import vn.utetra.repository.*;
import vn.utetra.security.SafeRedirect;
import vn.utetra.service.OrderService;
import vn.utetra.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Khu vực "Tài khoản" của khách hàng: mọi mục (thành viên, ưu đãi, địa chỉ, yêu thích, đã đặt, trợ giúp)
 * nằm trong cùng một khung có menu bên trái, không đưa khách sang trang khác.
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    private static final int MAX_ADDRESSES = 10;

    @Autowired private UserService userService;
    @Autowired private OrderService orderService;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private UserAddressRepository addressRepo;
    @Autowired private FavoriteRepository favoriteRepo;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;

    /** Mốc điểm lên hạng */
    @Value("${member.vip-points:400}")      private int vipPoints;
    @Value("${member.diamond-points:1000}") private int diamondPoints;

    private User me(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElseThrow();
    }

    @GetMapping
    public String home() { return "redirect:/profile"; }

    /* ---------- Khách hàng thành viên ---------- */
    @GetMapping("/member")
    public String member(@RequestParam(required = false) String month, Authentication auth, Model model) {
        User u = me(auth);
        // Hạng dựa trên tổng điểm đã kiếm được; số dư đổi thưởng có thể thấp hơn.
        int pts = orderService.lifetimePointsFor(u);

        String rank, nextRank = null;
        int remaining = 0, progress;
        if (pts >= diamondPoints) {
            rank = "DIAMOND"; progress = 100;
        } else if (pts >= vipPoints) {
            rank = "VIP"; nextRank = "DIAMOND"; remaining = diamondPoints - pts;
            progress = 50 + (int) (50L * (pts - vipPoints) / Math.max(1, diamondPoints - vipPoints));
        } else {
            rank = "MEMBER"; nextRank = "VIP"; remaining = vipPoints - pts;
            progress = (int) (50L * pts / Math.max(1, vipPoints));
        }

        // Lịch sử cộng điểm: các đơn đã hoàn thành và được cộng điểm
        List<Order> history = new ArrayList<>();
        String m = (month == null || !month.matches("\\d{4}-\\d{2}")) ? "" : month;
        for (Order o : orderService.ordersOf(auth.getName())) {
            if (o.getPointsEarned() == null || o.getPointsEarned() <= 0) continue;
            if (!m.isEmpty() && (o.getCreatedAt() == null || !o.getCreatedAt().toString().startsWith(m))) continue;
            history.add(o);
        }

        model.addAttribute("me", u);
        model.addAttribute("tierPoints", pts);
        model.addAttribute("rank", rank);
        model.addAttribute("nextRank", nextRank);
        model.addAttribute("remaining", remaining);
        model.addAttribute("progress", progress);
        model.addAttribute("vipPoints", vipPoints);
        model.addAttribute("diamondPoints", diamondPoints);
        model.addAttribute("history", history);
        model.addAttribute("month", m);
        model.addAttribute("memberCode", String.format("UTE%06d", u.getId()));
        model.addAttribute("pointUnit", OrderService.POINT_UNIT);
        return "user/account-member";
    }

    /* ---------- Ưu đãi của tôi ---------- */
    @GetMapping("/vouchers")
    public String vouchers(Authentication auth, Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> list = new ArrayList<>();
        for (Voucher v : voucherRepo.findAllByOrderByIdDesc()) {
            if (!Boolean.TRUE.equals(v.getActive())) continue;
            if (v.getExpiresAt() != null && v.getExpiresAt().isBefore(now)) continue;
            if (v.getUsageLimit() != null && v.getUsedCount() != null && v.getUsedCount() >= v.getUsageLimit()) continue;
            list.add(v);
        }
        model.addAttribute("vouchers", list);
        return "user/account-vouchers";
    }

    /* ---------- Số địa chỉ ---------- */
    @GetMapping("/addresses")
    public String addresses(Authentication auth, Model model) {
        model.addAttribute("addresses", addressRepo.findByUserIdOrderByDefaultAddressDescIdAsc(me(auth).getId()));
        return "user/account-addresses";
    }

    @PostMapping("/addresses/add")
    @Transactional
    public String addAddress(@RequestParam String label, @RequestParam String address,
                             @RequestParam(defaultValue = "false") boolean makeDefault,
                             @RequestParam(required = false) String back,
                             Authentication auth, RedirectAttributes ra) {
        User u = me(auth);
        String l = label == null ? "" : label.trim();
        String a = address == null ? "" : address.trim();
        String dest = SafeRedirect.localPath(back, "/account/addresses");
        if (l.isEmpty() || l.length() > 50 || a.isEmpty() || a.length() > 500) {
            ra.addFlashAttribute("error", "Vui lòng nhập tên gợi nhớ (tối đa 50 ký tự) và địa chỉ (tối đa 500 ký tự).");
            return "redirect:" + dest;
        }
        if (addressRepo.countByUserId(u.getId()) >= MAX_ADDRESSES) {
            ra.addFlashAttribute("error", "Bạn chỉ lưu được tối đa " + MAX_ADDRESSES + " địa chỉ.");
            return "redirect:" + dest;
        }
        UserAddress ua = new UserAddress();
        ua.setUserId(u.getId());
        ua.setLabel(l);
        ua.setAddress(a);
        boolean first = addressRepo.countByUserId(u.getId()) == 0;
        ua.setDefaultAddress(false);
        ua = addressRepo.save(ua);
        if (makeDefault || first) setDefault(u, ua);
        ra.addFlashAttribute("success", "Đã lưu địa chỉ.");
        return "redirect:" + dest;
    }

    @PostMapping("/addresses/default")
    @Transactional
    public String defaultAddress(@RequestParam Long id, Authentication auth, RedirectAttributes ra) {
        User u = me(auth);
        addressRepo.findByIdAndUserId(id, u.getId()).ifPresent(a -> setDefault(u, a));
        ra.addFlashAttribute("success", "Đã đặt làm địa chỉ mặc định.");
        return "redirect:/account/addresses";
    }

    @PostMapping("/addresses/delete")
    @Transactional
    public String deleteAddress(@RequestParam Long id, Authentication auth, RedirectAttributes ra) {
        User u = me(auth);
        addressRepo.findByIdAndUserId(id, u.getId()).ifPresent(addressRepo::delete);
        ra.addFlashAttribute("success", "Đã xoá địa chỉ.");
        return "redirect:/account/addresses";
    }

    /** Đặt địa chỉ mặc định, đồng thời cập nhật địa chỉ trong hồ sơ để trang thanh toán tự điền. */
    private void setDefault(User u, UserAddress target) {
        for (UserAddress a : addressRepo.findByUserIdOrderByDefaultAddressDescIdAsc(u.getId())) {
            boolean isTarget = a.getId().equals(target.getId());
            if (Boolean.TRUE.equals(a.getDefaultAddress()) != isTarget) {
                a.setDefaultAddress(isTarget);
                addressRepo.save(a);
            }
        }
        String addr = target.getAddress();
        u.setAddress(addr.length() > 255 ? addr.substring(0, 255) : addr);
        userRepo.save(u);
    }

    /* ---------- Sản phẩm yêu thích ---------- */
    @GetMapping("/favorites")
    public String favorites(Authentication auth, Model model) {
        model.addAttribute("favorites", favoriteRepo.findByUserIdWithProduct(me(auth).getId()));
        return "user/account-favorites";
    }

    @PostMapping("/favorites/toggle")
    @Transactional
    public String toggleFavorite(@RequestParam Long productId,
                                 @RequestParam(required = false) String back,
                                 Authentication auth, RedirectAttributes ra) {
        User u = me(auth);
        Optional<Favorite> ex = favoriteRepo.findByUserIdAndProductId(u.getId(), productId);
        if (ex.isPresent()) {
            favoriteRepo.delete(ex.get());
            ra.addFlashAttribute("success", "Đã bỏ khỏi danh sách yêu thích.");
        } else {
            Product p = productRepo.findById(productId).orElse(null);
            if (p != null && Boolean.TRUE.equals(p.getActive())) {
                Favorite f = new Favorite();
                f.setUserId(u.getId());
                f.setProduct(p);
                favoriteRepo.save(f);
                ra.addFlashAttribute("success", "Đã thêm \"" + p.getName() + "\" vào yêu thích.");
            }
        }
        return "redirect:" + SafeRedirect.localPath(back, "/account/favorites");
    }

    /* ---------- Sản phẩm đã đặt ---------- */
    @GetMapping("/ordered")
    public String ordered(Authentication auth, Model model) {
        List<Object[]> rows = orderDetailRepo.orderedProducts(me(auth).getId());
        Map<Long, Product> byId = new HashMap<>();
        productRepo.findAllById(rows.stream().map(r -> ((Number) r[0]).longValue()).toList())
                .forEach(p -> byId.put(p.getId(), p));
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] r : rows) {
            Product p = byId.get(((Number) r[0]).longValue());
            if (p == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("product", p);
            m.put("quantity", ((Number) r[1]).longValue());
            m.put("last", r[2] == null ? "" : r[2].toString().replace('T', ' ').substring(0, 16));
            items.add(m);
        }
        model.addAttribute("items", items);
        return "user/account-ordered";
    }

    /* ---------- Trung tâm trợ giúp ---------- */
    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("pointUnit", OrderService.POINT_UNIT);
        return "user/account-help";
    }
}
