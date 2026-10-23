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
    public String list(Model model) {
        model.addAttribute("sizes", sizeService.all());
        return "admin/sizes";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("size", new SizeOption());
        return "admin/size-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("size", sizeService.get(id));
        return "admin/size-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SizeOption s,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model, RedirectAttributes ra) {
        try {
            sizeService.save(s, active);
            ra.addFlashAttribute("success", "Đã lưu size.");
            return "redirect:/admin/sizes";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("size", s);
            model.addAttribute("error", ex.getMessage());
            return "admin/size-form";
        }
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