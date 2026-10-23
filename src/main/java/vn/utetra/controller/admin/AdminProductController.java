package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Product;
import vn.utetra.service.*;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

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
    public String list(Model model) {
        model.addAttribute("products", productService.allWithCategory());
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
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {

        boolean isNew = (product.getId() == null);
        try {
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

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        productService.toggle(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa sản phẩm");
        return "redirect:/admin/products";
    }
}