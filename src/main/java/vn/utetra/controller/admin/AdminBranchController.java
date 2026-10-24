package vn.utetra.controller.admin;

import vn.utetra.security.SafeRedirect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Branch;
import vn.utetra.service.BranchService;

@Controller
@RequestMapping("/admin/branches")
public class AdminBranchController {

    private static final int PAGE_SIZE = 10;

    @Autowired private BranchService branchService;

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String active,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        Boolean activeP = "true".equalsIgnoreCase(active) ? Boolean.TRUE
                        : "false".equalsIgnoreCase(active) ? Boolean.FALSE
                        : null;

        Page<Branch> pageData = branchService.searchForAdmin(kw, activeP, page, PAGE_SIZE);

        model.addAttribute("pageData", pageData);
        model.addAttribute("kw", kw);
        model.addAttribute("activeFilter", active);
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

    @PostMapping("/toggle-active")
    public String toggleActive(@RequestParam Long id,
                               @RequestParam(required = false) String returnUrl,
                               RedirectAttributes ra) {
        branchService.toggleActive(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái chi nhánh.");
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/branches");
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        branchService.lock(id);
        ra.addFlashAttribute("success", "Đã khóa chi nhánh.");
        return "redirect:/admin/branches";
    }

    @PostMapping("/restore")
    public String restore(@RequestParam Long id, RedirectAttributes ra) {
        branchService.unlock(id);
        ra.addFlashAttribute("success", "Đã mở khóa chi nhánh.");
        return "redirect:/admin/branches";
    }
}