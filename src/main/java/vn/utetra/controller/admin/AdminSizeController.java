package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.SizeOption;
import vn.utetra.service.SizeService;

@Controller
@RequestMapping("/admin/sizes")
public class AdminSizeController {

    @Autowired private SizeService sizeService;

    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        SizeOption form = new SizeOption();
        if (edit != null) {
            try { form = sizeService.get(edit); } catch (Exception ignored) { }
        }
        model.addAttribute("sizes", sizeService.all());
        model.addAttribute("form", form);
        return "admin/sizes";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SizeOption s,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        boolean isNew = (s.getId() == null);
        s.setActive(active);
        try {
            sizeService.save(s, active);
            ra.addFlashAttribute("success", isNew ? "Đã thêm size." : "Đã cập nhật size.");
            return "redirect:/admin/sizes";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("form", s);
            model.addAttribute("sizes", sizeService.all());
            model.addAttribute("error", ex.getMessage());
            return "admin/sizes";
        }
    }

    @PostMapping("/move-up/{id}")
    public String moveUp(@PathVariable Long id, RedirectAttributes ra) {
        sizeService.move(id, -1);
        return "redirect:/admin/sizes";
    }

    @PostMapping("/move-down/{id}")
    public String moveDown(@PathVariable Long id, RedirectAttributes ra) {
        sizeService.move(id, 1);
        return "redirect:/admin/sizes";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        sizeService.lock(id);
        ra.addFlashAttribute("success", "Đã khóa size.");
        return "redirect:/admin/sizes";
    }

    @GetMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        sizeService.unlock(id);
        ra.addFlashAttribute("success", "Đã mở khóa size.");
        return "redirect:/admin/sizes";
    }
}