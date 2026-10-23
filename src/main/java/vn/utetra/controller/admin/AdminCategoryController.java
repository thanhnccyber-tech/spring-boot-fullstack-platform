package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Category;
import vn.utetra.repository.CategoryRepository;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.FileValidator;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired private CategoryRepository repo;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FileValidator fileValidator;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", repo.findAll());
        return "admin/categories";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("category", repo.findById(id).orElseThrow());
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category c,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {

        boolean isNew = (c.getId() == null);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                fileValidator.validateImage(imageFile);
            } catch (IllegalArgumentException ex) {
                ra.addFlashAttribute("error", ex.getMessage());
                return "redirect:" + (isNew
                        ? "/admin/categories/new"
                        : "/admin/categories/edit/" + c.getId());
            }
            c.setImageUrl(cloudinaryService.upload(imageFile, "categories"));
        }

        repo.save(c);
        ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            repo.deleteById(id);
            ra.addFlashAttribute("success", "Đã xóa danh mục");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa (còn sản phẩm tham chiếu)");
        }
        return "redirect:/admin/categories";
    }
}