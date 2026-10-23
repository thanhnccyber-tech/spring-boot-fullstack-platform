package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Product;
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
        return "admin/products";
    }

    @GetMapping("/new")
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
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getWithDetails(id));
        loadOptions(model);
        return "admin/product-form";
    }

    @PostMapping("/save")
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
    public String toggleActive(@RequestParam Long id,
                               @RequestParam(required = false) String returnUrl,
                               RedirectAttributes ra) {
        productService.toggle(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái hiển thị");
        return "redirect:" + (returnUrl != null && !returnUrl.isBlank()
                ? returnUrl : "/admin/products");
    }

    @PostMapping("/toggle-stock")
    public String toggleStock(@RequestParam Long id,
                              @RequestParam(required = false) String returnUrl,
                              RedirectAttributes ra) {
        productService.toggleOutOfStock(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái tồn kho");
        return "redirect:" + (returnUrl != null && !returnUrl.isBlank()
                ? returnUrl : "/admin/products");
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa sản phẩm");
        return "redirect:/admin/products";
    }
}