package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.SugarLevel;
import vn.utetra.service.SugarService;

@Controller
@RequestMapping("/admin/sugars")
public class AdminSugarController {

    @Autowired private SugarService sugarService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sugars", sugarService.all());
        return "admin/sugars";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("sugar", new SugarLevel());
        return "admin/sugar-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("sugar", sugarService.get(id));
        return "admin/sugar-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SugarLevel s,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        try {
            sugarService.save(s, active);
            ra.addFlashAttribute("success", "Đã lưu mức đường.");
            return "redirect:/admin/sugars";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("sugar", s);
            model.addAttribute("error", ex.getMessage());
            return "admin/sugar-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        sugarService.lock(id);
        ra.addFlashAttribute("success", "Đã khóa mức đường.");
        return "redirect:/admin/sugars";
    }

    @GetMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        sugarService.unlock(id);
        ra.addFlashAttribute("success", "Đã mở khóa mức đường.");
        return "redirect:/admin/sugars";
    }
}