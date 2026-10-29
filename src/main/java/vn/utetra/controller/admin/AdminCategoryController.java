package vn.utetra.controller.admin;

import vn.utetra.security.SafeRedirect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Category;
import vn.utetra.service.CategoryService;
import vn.utetra.service.CloudinaryService;
import vn.utetra.service.FileValidator;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private static final int PAGE_SIZE = 10;

    @Autowired private CategoryService categoryService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FileValidator fileValidator;

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String active,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        Boolean activeP = "true".equalsIgnoreCase(active) ? Boolean.TRUE
                        : "false".equalsIgnoreCase(active) ? Boolean.FALSE
                        : null;

        Page<Category> pageData = categoryService.searchForAdmin(kw, activeP, page, PAGE_SIZE);

        List<Long> ids = pageData.getContent().stream().map(Category::getId).toList();
        Map<Long, Long> productCounts = categoryService.countProductsByCategoryIds(ids);

        model.addAttribute("pageData", pageData);
        model.addAttribute("productCounts", productCounts);
        model.addAttribute("kw", kw);
        model.addAttribute("activeFilter", active);
        return "admin/categories";
    }

    @GetMapping("/new")
    public String create(Model model) {
        Category c = new Category();
        c.setActive(true);
        c.setSortOrder(0);
        model.addAttribute("category", c);
        model.addAttribute("isNew", true);
        return "admin/category-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.get(id));
        model.addAttribute("isNew", false);
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category c,
                       @RequestParam(required = false) MultipartFile imageFile,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model,
                       RedirectAttributes ra) {

        boolean isNew = (c.getId() == null);
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                fileValidator.validateImage(imageFile);
                c.setImageUrl(cloudinaryService.upload(imageFile, "categories"));
            }
            c.setActive(active);
            categoryService.save(c);
            ra.addFlashAttribute("success",
                    isNew ? "Đã thêm danh mục." : "Đã cập nhật danh mục.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("category", c);
            model.addAttribute("isNew", isNew);
            model.addAttribute("error", ex.getMessage());
            return "admin/category-form";
        }
    }

    @PostMapping("/move-up/{id}")
    public String moveUp(@PathVariable Long id,
                         @RequestParam(required = false) String returnUrl,
                         RedirectAttributes ra) {
        categoryService.move(id, -1);
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/categories");
    }

    @PostMapping("/move-down/{id}")
    public String moveDown(@PathVariable Long id,
                           @RequestParam(required = false) String returnUrl,
                           RedirectAttributes ra) {
        categoryService.move(id, 1);
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/categories");
    }

    @PostMapping("/toggle-active")
    public String toggleActive(@RequestParam Long id,
                               @RequestParam(required = false) String returnUrl,
                               RedirectAttributes ra) {
        categoryService.toggleActive(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái danh mục.");
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/categories");
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa danh mục.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}