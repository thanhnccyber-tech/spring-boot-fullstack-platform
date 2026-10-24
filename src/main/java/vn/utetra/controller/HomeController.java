package vn.utetra.controller;

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

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ToppingService toppingService;
    @Autowired private SizeService sizeService;
    @Autowired private SugarService sugarService;
    @Autowired private IceService iceService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Product> featured = productService.featured();
        List<Product> newArrivals = productService.newArrivals();
        model.addAttribute("featured", featured);
        model.addAttribute("newArrivals", newArrivals);
        List<Long> ids = new ArrayList<>();
        featured.forEach(p -> ids.add(p.getId()));
        newArrivals.forEach(p -> ids.add(p.getId()));
        model.addAttribute("ratings", reviewService.summaries(ids));
        model.addAttribute("categories", categoryService.active());
        return "user/home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String kw,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
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

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Authentication auth, Model model) {
        Product p = productService.get(id);
        if (!Boolean.TRUE.equals(p.getActive())) {
            return "redirect:/products";
        }
        model.addAttribute("product", p);
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

        // ----- Đánh giá & bình luận -----
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
            }
        }
        return "user/product-detail";
    }
}