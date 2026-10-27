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
    public String list(@RequestParam(required = false) Long edit, Model model) {
        IceLevel form = new IceLevel();
        if (edit != null) {
            try { form = iceService.get(edit); } catch (Exception ignored) { }
        }
        model.addAttribute("ices", iceService.all());
        model.addAttribute("form", form);
        return "admin/ices";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute IceLevel s,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        boolean isNew = (s.getId() == null);
        s.setActive(active);
        try {
            iceService.save(s, active);
            ra.addFlashAttribute("success", isNew ? "Đã thêm mức đá." : "Đã cập nhật mức đá.");
            return "redirect:/admin/ices";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("form", s);
            model.addAttribute("ices", iceService.all());
            model.addAttribute("error", ex.getMessage());
            return "admin/ices";
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