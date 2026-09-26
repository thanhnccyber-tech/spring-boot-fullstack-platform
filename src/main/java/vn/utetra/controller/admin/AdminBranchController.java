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
        model.addAttribute("branch", new Branch());
        return "admin/branch-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("branch", branchService.get(id));
        return "admin/branch-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Branch branch, RedirectAttributes ra) {
        branchService.save(branch);
        ra.addFlashAttribute("success", "Lưu chi nhánh thành công!");
        return "redirect:/admin/branches";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        branchService.delete(id);
        return "redirect:/admin/branches";
    }
}