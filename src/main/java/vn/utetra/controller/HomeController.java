package vn.utetra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.utetra.entity.IceLevel;
import vn.utetra.entity.Product;
import vn.utetra.entity.User;
import vn.utetra.dto.RatingSummary;
import vn.utetra.entity.SizeOption;
import vn.utetra.entity.SugarLevel;
import vn.utetra.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    /** Khoá session lưu danh sách id sản phẩm đã xem, mới nhất ở đầu. */
    public static final String VIEWED_SESSION_KEY = "VIEWED_PRODUCTS";
    /** Tổng số sản phẩm lưu tối đa trong lịch sử. */
    private static final int MAX_VIEWED = 20;
    /** Số thẻ hiển thị ở mục "Sản phẩm đã xem gần đây" dưới trang chi tiết. */
    private static final int MINI_VIEWED_LIMIT = 8;

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ToppingService toppingService;
    @Autowired private SizeService sizeService;
    @Autowired private SugarService sugarService;
    @Autowired private IceService iceService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;
    @Autowired private PromotionService promotionService;
    @Autowired private vn.utetra.repository.FavoriteRepository favoriteRepo;
    @Autowired private BranchService branchService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Product> featured = productService.featured();
        List<Product> newArrivals = productService.newArrivals();
        model.addAttribute("featured", featured);
        model.addAttribute("newArrivals", newArrivals);

        List<Product> bookProducts = new ArrayList<>();
        if (!featured.isEmpty()) bookProducts.add(featured.get(0));
        for (Product candidate : newArrivals) {
            if (bookProducts.size() >= 3) break;
            boolean exists = bookProducts.stream().anyMatch(x -> x.getId().equals(candidate.getId()));
            if (!exists) bookProducts.add(candidate);
        }
        for (Product candidate : featured) {
            if (bookProducts.size() >= 3) break;
            boolean exists = bookProducts.stream().anyMatch(x -> x.getId().equals(candidate.getId()));
            if (!exists) bookProducts.add(candidate);
        }
        model.addAttribute("bookProducts", bookProducts);

        List<Long> ids = new ArrayList<>();
        featured.forEach(p -> ids.add(p.getId()));
        newArrivals.forEach(p -> ids.add(p.getId()));
        model.addAttribute("ratings", reviewService.summaries(ids));
        model.addAttribute("categories", categoryService.active());
        List<vn.utetra.entity.Promotion> promos = promotionService.active();
        model.addAttribute("promotions", promos.size() > 4 ? promos.subList(0, 4) : promos);
        model.addAttribute("branches", branchService.active());
        return "user/home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String kw,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(defaultValue = "newest") String sort,
                           Model model) {
        Page<Product> p = productService.search(kw, categoryId, minPrice, maxPrice, page, size, sort);
        model.addAttribute("pageData", p);
        model.addAttribute("kw", kw);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", categoryService.active());
        model.addAttribute("ratings", reviewService.summaries(
                p.getContent().stream().map(Product::getId).toList()));
        return "user/products";
    }

    /* ==================== SẢN PHẨM ĐÃ XEM ==================== */

    /**
     * Ghi nhận lượt xem vào session: đưa id lên đầu, loại trùng, giới hạn MAX_VIEWED.
     * Dùng session thay vì DB → khách chưa đăng nhập vẫn có lịch sử xem.
     */
    private void trackView(HttpSession session, Long productId) {
        if (session == null || productId == null) return;
        @SuppressWarnings("unchecked")
        List<Long> existing = (List<Long>) session.getAttribute(VIEWED_SESSION_KEY);
        List<Long> ids = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        ids.remove(productId);          // bỏ vị trí cũ nếu đã có
        ids.add(0, productId);          // đẩy lên đầu
        if (ids.size() > MAX_VIEWED) {
            ids = new ArrayList<>(ids.subList(0, MAX_VIEWED));
        }
        session.setAttribute(VIEWED_SESSION_KEY, ids);
    }

    /** Đọc danh sách id đã xem từ session. */
    @SuppressWarnings("unchecked")
    private List<Long> viewedIds(HttpSession session) {
        if (session == null) return List.of();
        Object o = session.getAttribute(VIEWED_SESSION_KEY);
        return (o instanceof List<?>) ? (List<Long>) o : List.of();
    }

    /** Trang xem toàn bộ sản phẩm đã xem (tối đa 20). */
    @GetMapping("/products/recently-viewed")
    public String recentlyViewedPage(HttpSession session, Model model) {
        List<Product> products = productService.findActiveInOrder(viewedIds(session));
        model.addAttribute("products", products);
        if (!products.isEmpty()) {
            model.addAttribute("ratings", reviewService.summaries(
                    products.stream().map(Product::getId).toList()));
        }
        return "user/recently-viewed";
    }

    /** Xoá lịch sử xem trong session. */
    @PostMapping("/products/recently-viewed/clear")
    public String clearRecentlyViewed(HttpSession session) {
        session.removeAttribute(VIEWED_SESSION_KEY);
        return "redirect:/products/recently-viewed";
    }

    /* ==================== GỢI Ý & CHI TIẾT ==================== */

    @GetMapping("/products/suggest")
    @ResponseBody
    public List<Map<String, Object>> suggest(@RequestParam(name = "q", required = false) String q) {
        String kw = q == null ? "" : q.trim();
        List<Map<String, Object>> out = new ArrayList<>();
        if (kw.length() < 2 || kw.length() > 50) return out;
        for (Product p : productService.search(kw, null, null, null, 0, 6, "newest").getContent()) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("price", p.getBasePrice());
            m.put("image", p.getThumbUrl());
            m.put("outOfStock", Boolean.TRUE.equals(p.getOutOfStock()));
            out.add(m);
        }
        return out;
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                                Authentication auth, Model model, HttpSession session) {
        Product p = productService.get(id);
        if (!Boolean.TRUE.equals(p.getActive())) {
            return "redirect:/products";
        }

        // Ghi nhận lượt xem vào session TRƯỚC (để danh sách đã xem khác món đang xem)
        trackView(session, id);

        // Mục "Sản phẩm đã xem gần đây" — bỏ món hiện tại, tối đa 8 món
        List<Long> otherIds = viewedIds(session).stream()
                .filter(x -> !x.equals(id))
                .limit(MINI_VIEWED_LIMIT)
                .toList();
        List<Product> recentlyViewed = productService.findActiveInOrder(otherIds);
        model.addAttribute("recentlyViewed", recentlyViewed);
        if (!recentlyViewed.isEmpty()) {
            model.addAttribute("ratings", reviewService.summaries(
                    recentlyViewed.stream().map(Product::getId).toList()));
        }

        model.addAttribute("product", p);
        model.addAttribute("productDetailQuantity", Math.max(1, Math.min(quantity, 50)));
        model.addAttribute("toppings", toppingService.activeInStock());

        List<SizeOption> allowedSizes = (p.getSizes() == null || p.getSizes().isEmpty())
                ? sizeService.active()
                : new ArrayList<>(p.getSizes());
        allowedSizes = allowedSizes.stream()
                .filter(SizeOption::getActive)
                .sorted((a, b) -> {
                    int c = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    return c != 0 ? c : a.getId().compareTo(b.getId());
                })
                .toList();

        List<SugarLevel> allowedSugars = (p.getSugars() == null || p.getSugars().isEmpty())
                ? sugarService.active()
                : new ArrayList<>(p.getSugars());
        allowedSugars = allowedSugars.stream()
                .filter(SugarLevel::getActive)
                .sorted((a, b) -> {
                    int c = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    return c != 0 ? c : a.getId().compareTo(b.getId());
                })
                .toList();

        List<IceLevel> allowedIces = (p.getIces() == null || p.getIces().isEmpty())
                ? iceService.active()
                : new ArrayList<>(p.getIces());
        allowedIces = allowedIces.stream()
                .filter(IceLevel::getActive)
                .sorted((a, b) -> {
                    int c = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    return c != 0 ? c : a.getId().compareTo(b.getId());
                })
                .toList();

        model.addAttribute("sizes", allowedSizes);
        model.addAttribute("sugars", allowedSugars);
        model.addAttribute("ices", allowedIces);

        RatingSummary summary = reviewService.summary(id);
        model.addAttribute("ratingSummary", summary);
        model.addAttribute("reviews", reviewService.listByProduct(id));
        model.addAttribute("requirePurchase", reviewService.isRequirePurchase());

        boolean loggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
        model.addAttribute("loggedIn", loggedIn);
        if (loggedIn) {
            Optional<User> me = userService.findByEmail(auth.getName());
            if (me.isPresent()) {
                Long uid = me.get().getId();
                model.addAttribute("currentUserId", uid);
                model.addAttribute("myReview", reviewService.findMine(uid, id).orElse(null));
                model.addAttribute("canReview", reviewService.canReview(uid, id));
                model.addAttribute("isAdminUser", me.get().isAdmin());
                model.addAttribute("isFavorite", favoriteRepo.existsByUserIdAndProductId(uid, id));
            }
        }
        return "user/product-detail";
    }
}