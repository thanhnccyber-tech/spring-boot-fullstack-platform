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
import vn.utetra.entity.Topping;
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
import java.util.Set;

@Controller
public class HomeController {

    public static final String VIEWED_SESSION_KEY = "VIEWED_PRODUCTS";
    private static final int MAX_VIEWED = 20;
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
    @Autowired private vn.utetra.repository.ProductRepository productRepo;
    @Autowired private CartService cartService;

    /** Chi nhánh khách đã chọn trong popup "Cách nhận hàng" (null nếu chưa chọn). */
    private static Long branchIdOf(HttpSession session) {
        if (session == null) return null;
        Object v = session.getAttribute("RECV_BRANCH");
        return v instanceof Long id ? id : null;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
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
        for (vn.utetra.config.BookMenu.Item item : vn.utetra.config.BookMenu.ITEMS) {
            productRepo.findFirstByNameIgnoreCaseAndActiveTrue(item.name()).ifPresent(p -> {
                boolean exists = bookProducts.stream().anyMatch(x -> x.getId().equals(p.getId()));
                if (!exists) bookProducts.add(p);
            });
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

        // Badge "Hết tại chi nhánh" cho thẻ sản phẩm trên trang chủ
        Long branchId = branchIdOf(session);
        List<Product> all = new ArrayList<>(featured);
        all.addAll(newArrivals);
        all.addAll(bookProducts);
        model.addAttribute("effectiveProductOOS",
                productService.effectiveOOSMap(branchId, all));
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
                           HttpSession session, Model model) {
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

        Long branchId = branchIdOf(session);
        model.addAttribute("effectiveProductOOS",
                productService.effectiveOOSMap(branchId, p.getContent()));
        return "user/products";
    }

    /* ==================== SẢN PHẨM ĐÃ XEM ==================== */

    private void trackView(HttpSession session, Long productId) {
        if (session == null || productId == null) return;
        @SuppressWarnings("unchecked")
        List<Long> existing = (List<Long>) session.getAttribute(VIEWED_SESSION_KEY);
        List<Long> ids = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        ids.remove(productId);
        ids.add(0, productId);
        if (ids.size() > MAX_VIEWED) {
            ids = new ArrayList<>(ids.subList(0, MAX_VIEWED));
        }
        session.setAttribute(VIEWED_SESSION_KEY, ids);
    }

    @SuppressWarnings("unchecked")
    private List<Long> viewedIds(HttpSession session) {
        if (session == null) return List.of();
        Object o = session.getAttribute(VIEWED_SESSION_KEY);
        return (o instanceof List<?>) ? (List<Long>) o : List.of();
    }

    @GetMapping("/products/recently-viewed")
    public String recentlyViewedPage(HttpSession session, Model model) {
        List<Product> products = productService.findActiveInOrder(viewedIds(session));
        model.addAttribute("products", products);
        if (!products.isEmpty()) {
            model.addAttribute("ratings", reviewService.summaries(
                    products.stream().map(Product::getId).toList()));
        }
        Long branchId = branchIdOf(session);
        model.addAttribute("effectiveProductOOS",
                productService.effectiveOOSMap(branchId, products));
        return "user/recently-viewed";
    }

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

        trackView(session, id);

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

        Long branchId = branchIdOf(session);

        // ===== Tồn kho tại chi nhánh đã chọn =====
        boolean productOOSAtBranch = Boolean.TRUE.equals(p.getOutOfStock());
        if (!productOOSAtBranch && branchId != null) {
            productOOSAtBranch = Boolean.TRUE.equals(
                    productService.branchStockMap(branchId, List.of(p.getId()))
                            .get(p.getId()));
        }
        model.addAttribute("productOOSAtBranch", productOOSAtBranch);

        String currentBranchName = null;
        if (branchId != null) {
            try { currentBranchName = branchService.get(branchId).getName(); } catch (Exception ignored) { }
        }
        model.addAttribute("currentBranchName", currentBranchName);

        // Dùng toppingsForDisplay để GIỮ LẠI topping đang tạm hết hàng toàn hệ thống
        // (hiện nhãn "Hết hàng" + disable), đồng bộ với Manager "Hết tại CN".
        List<Topping> toppings = cartService.toppingsForDisplay(p);
        model.addAttribute("toppings", toppings);
        Set<Long> branchOOSToppingIds = toppingService.branchOOSIds(branchId,
                toppings.stream().map(Topping::getId).toList());
        model.addAttribute("branchOOSToppingIds", branchOOSToppingIds);

        // Badge cho sản phẩm đã xem gần đây
        model.addAttribute("effectiveProductOOS",
                productService.effectiveOOSMap(branchId, recentlyViewed));

        // ===== Size / Sugar / Ice =====
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