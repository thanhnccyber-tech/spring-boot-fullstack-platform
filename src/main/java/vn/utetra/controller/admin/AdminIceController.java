package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.IceLevel;
import vn.utetra.service.IceService;

@Controller
@RequestMapping("/admin/ices")
public class AdminIceController {

    @Autowired private IceService iceService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("ices", iceService.all());
        return "admin/ices";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("ice", new IceLevel());
        return "admin/ice-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("ice", iceService.get(id));
        return "admin/ice-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute IceLevel s,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        try {
            iceService.save(s, active);
            ra.addFlashAttribute("success", "Đã lưu mức đá.");
            return "redirect:/admin/ices";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("ice", s);
            model.addAttribute("error", ex.getMessage());
            return "admin/ice-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        iceService.lock(id);
        ra.addFlashAttribute("success", "Đã khóa mức đá.");
        return "redirect:/admin/ices";
    }

    @GetMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        iceService.unlock(id);
        ra.addFlashAttribute("success", "Đã mở khóa mức đá.");
        return "redirect:/admin/ices";
    }
}