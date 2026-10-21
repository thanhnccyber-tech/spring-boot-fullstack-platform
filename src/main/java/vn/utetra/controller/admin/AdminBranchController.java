package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Branch;
import vn.utetra.repository.BranchRepository;

@Controller
@RequestMapping("/admin/branches")
public class AdminBranchController {

    @Autowired private BranchRepository repo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", repo.findAll());
        return "admin/branches";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("branch", new Branch());
        return "admin/branch-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("branch", repo.findById(id).orElseThrow());
        return "admin/branch-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Branch b, RedirectAttributes ra) {
        repo.save(b);
        ra.addFlashAttribute("success", "Lưu chi nhánh thành công!");
        return "redirect:/admin/branches";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Branch b = repo.findById(id).orElseThrow();
        b.setActive(false); // xóa mềm
        repo.save(b);
        ra.addFlashAttribute("success", "Đã vô hiệu hóa chi nhánh");
        return "redirect:/admin/branches";
    }
}