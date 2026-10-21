package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Product;
import vn.utetra.repository.CategoryRepository;
import vn.utetra.repository.ProductRepository;
import vn.utetra.repository.ToppingRepository;
import vn.utetra.service.CloudinaryService;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired private ProductRepository productRepo;
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private ToppingRepository toppingRepo;
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productRepo.findAll());
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("toppings", toppingRepo.findAll());
        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productRepo.findById(id).orElseThrow());
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("toppings", toppingRepo.findAll());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) List<Long> toppingIds,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        if (categoryId != null) product.setCategory(categoryRepo.findById(categoryId).orElse(null));
        if (toppingIds != null && !toppingIds.isEmpty())
            product.setToppings(toppingRepo.findAllById(toppingIds));
        else
            product.setToppings(List.of());
        if (imageFile != null && !imageFile.isEmpty())
            product.setImageUrl(cloudinaryService.upload(imageFile, "products"));

        productRepo.save(product);
        ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Product p = productRepo.findById(id).orElseThrow();
        p.setActive(!Boolean.TRUE.equals(p.getActive()));
        productRepo.save(p);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productRepo.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa sản phẩm");
        return "redirect:/admin/products";
    }
}