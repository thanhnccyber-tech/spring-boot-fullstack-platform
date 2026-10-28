package vn.utetra.controller.admin;

import vn.utetra.security.SafeRedirect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Product;
import vn.utetra.entity.User;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final int PAGE_SIZE = 10;

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ToppingService toppingService;
    @Autowired private SizeService sizeService;
    @Autowired private SugarService sugarService;
    @Autowired private IceService iceService;
    @Autowired private UserRepository userRepo;

    private static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void loadOptions(Model model) {
        model.addAttribute("categories", categoryService.all());
        model.addAttribute("toppings", toppingService.all());
        model.addAttribute("sizes", sizeService.active());
        model.addAttribute("sugars", sugarService.active());
        model.addAttribute("ices", iceService.active());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String active,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "newest") String sort,
                       Authentication auth,
                       Model model) {

        Boolean activeP = "true".equalsIgnoreCase(active) ? Boolean.TRUE
                        : "false".equalsIgnoreCase(active) ? Boolean.FALSE
                        : null;

        Page<Product> pageData = productService.searchForAdmin(
                kw, categoryId, activeP, minPrice, maxPrice, page, PAGE_SIZE, sort);

        model.addAttribute("pageData", pageData);
        model.addAttribute("kw", kw);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("activeFilter", active);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", categoryService.all());
        model.addAttribute("isAdmin", isAdmin(auth));

        // Bổ sung: cho Manager thấy trạng thái tồn kho riêng của chi nhánh
        User me = userRepo.findByEmailWithBranch(auth.getName()).orElseThrow();
        if (!me.isAdmin() && me.getBranch() != null) {
            List<Long> ids = pageData.getContent().stream().map(Product::getId).toList();
            model.addAttribute("branchStockMap",
                    productService.branchStockMap(me.getBranch().getId(), ids));
            model.addAttribute("managerBranchName", me.getBranch().getName());
        } else {
            model.addAttribute("branchStockMap", java.util.Map.of());
            model.addAttribute("managerBranchName", null);
        }
        return "admin/products";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        Product p = new Product();
        p.setAllowSize(true);
        p.setAllowSugar(true);
        p.setAllowIce(true);
        model.addAttribute("product", p);
        loadOptions(model);
        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getWithDetails(id));
        loadOptions(model);
        return "admin/product-form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@ModelAttribute Product product,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) List<Long> toppingIds,
                       @RequestParam(required = false) List<Long> sizeIds,
                       @RequestParam(required = false) List<Long> sugarIds,
                       @RequestParam(required = false) List<Long> iceIds,
                       @RequestParam(required = false) Long defaultSizeId,
                       @RequestParam(required = false) Long defaultSugarId,
                       @RequestParam(required = false) Long defaultIceId,
                       @RequestParam(defaultValue = "false") Boolean allowSize,
                       @RequestParam(defaultValue = "false") Boolean allowSugar,
                       @RequestParam(defaultValue = "false") Boolean allowIce,
                       @RequestParam(defaultValue = "false") Boolean outOfStock,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {

        boolean isNew = (product.getId() == null);
        try {
            product.setOutOfStock(outOfStock);
            productService.save(product, categoryId, toppingIds,
                    sizeIds, sugarIds, iceIds,
                    defaultSizeId, defaultSugarId, defaultIceId,
                    allowSize, allowSugar, allowIce,
                    imageFile);
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:" + (isNew
                    ? "/admin/products/new"
                    : "/admin/products/edit/" + product.getId());
        }
    }

    @PostMapping("/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleActive(@RequestParam Long id,
                               @RequestParam(required = false) String returnUrl,
                               RedirectAttributes ra) {
        productService.toggle(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái hiển thị");
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/products");
    }

    /** Admin đổi trạng thái toàn hệ thống; Manager chỉ đổi tại chi nhánh của mình. */
    @PostMapping("/toggle-stock")
    public String toggleStock(@RequestParam Long id,
                              @RequestParam(required = false) String returnUrl,
                              Authentication auth,
                              RedirectAttributes ra) {
        User me = userRepo.findByEmailWithBranch(auth.getName()).orElseThrow();
        if (me.isAdmin()) {
            productService.toggleOutOfStock(id);
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái tồn kho toàn hệ thống");
        } else if (me.getBranch() != null) {
            productService.toggleOutOfStockForBranch(id, me.getBranch().getId());
            ra.addFlashAttribute("success",
                    "Đã cập nhật tồn kho tại chi nhánh " + me.getBranch().getName());
        } else {
            ra.addFlashAttribute("error", "Tài khoản Manager chưa được gán chi nhánh.");
        }
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/products");
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa sản phẩm");
        return "redirect:/admin/products";
    }
}