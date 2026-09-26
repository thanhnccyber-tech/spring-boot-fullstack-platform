package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Category;
import vn.utetra.service.CategoryService;
import vn.utetra.service.CloudinaryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired private CategoryService categoryService;
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.all());
        return "admin/categories";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.get(id));
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        if (imageFile != null && !imageFile.isEmpty())
            category.setImageUrl(cloudinaryService.upload(imageFile, "categories"));
        categoryService.save(category);
        ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}