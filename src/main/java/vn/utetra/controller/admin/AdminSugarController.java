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
    public String list(@RequestParam(required = false) Long edit, Model model) {
        SugarLevel form = new SugarLevel();
        if (edit != null) {
            try { form = sugarService.get(edit); } catch (Exception ignored) { }
        }
        model.addAttribute("sugars", sugarService.all());
        model.addAttribute("form", form);
        return "admin/sugars";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SugarLevel s,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        boolean isNew = (s.getId() == null);
        s.setActive(active);
        try {
            sugarService.save(s, active);
            ra.addFlashAttribute("success", isNew ? "Đã thêm mức đường." : "Đã cập nhật mức đường.");
            return "redirect:/admin/sugars";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("form", s);
            model.addAttribute("sugars", sugarService.all());
            model.addAttribute("error", ex.getMessage());
            return "admin/sugars";
        }
    }

    @PostMapping("/move-up/{id}")
    public String moveUp(@PathVariable Long id, RedirectAttributes ra) {
        sugarService.move(id, -1);
        return "redirect:/admin/sugars";
    }

    @PostMapping("/move-down/{id}")
    public String moveDown(@PathVariable Long id, RedirectAttributes ra) {
        sugarService.move(id, 1);
        return "redirect:/admin/sugars";
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