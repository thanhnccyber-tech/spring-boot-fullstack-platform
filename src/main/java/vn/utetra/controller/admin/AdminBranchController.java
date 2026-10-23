package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Branch;
import vn.utetra.service.BranchService;

@Controller
@RequestMapping("/admin/branches")
public class AdminBranchController {

    @Autowired private BranchService branchService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", branchService.all());
        return "admin/branches";
    }

    @GetMapping("/new")
    public String create(Model model) {
        Branch b = new Branch();
        b.setActive(true);
        model.addAttribute("branch", b);
        model.addAttribute("isNew", true);
        return "admin/branch-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("branch", branchService.get(id));
        model.addAttribute("isNew", false);
        return "admin/branch-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Branch b,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        boolean isNew = (b.getId() == null);
        try {
            branchService.save(b, active);
            ra.addFlashAttribute("success",
                    isNew ? "Đã thêm chi nhánh mới." : "Đã cập nhật chi nhánh.");
            return "redirect:/admin/branches";
        } catch (IllegalArgumentException ex) {
            b.setActive(active);
            model.addAttribute("branch", b);
            model.addAttribute("isNew", isNew);
            model.addAttribute("error", ex.getMessage());
            return "admin/branch-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        branchService.lock(id);
        ra.addFlashAttribute("success", "Đã khóa chi nhánh.");
        return "redirect:/admin/branches";
    }

    @GetMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        branchService.unlock(id);
        ra.addFlashAttribute("success", "Đã mở khóa chi nhánh.");
        return "redirect:/admin/branches";
    }
}