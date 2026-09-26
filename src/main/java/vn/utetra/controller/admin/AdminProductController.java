package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ToppingService toppingService;
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.all());
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.all());
        model.addAttribute("toppings", toppingService.all());
        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.get(id));
        model.addAttribute("categories", categoryService.all());
        model.addAttribute("toppings", toppingService.all());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) List<Long> toppingIds,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        if (categoryId != null) product.setCategory(categoryService.get(categoryId));
        if (toppingIds != null && !toppingIds.isEmpty())
            product.setToppings(toppingService.all().stream()
                    .filter(t -> toppingIds.contains(t.getId())).toList());
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageUrl(cloudinaryService.upload(imageFile, "products"));
        }
        productService.save(product);
        ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Xóa thành công!");
        return "redirect:/admin/products";
    }
}