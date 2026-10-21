package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Topping;
import vn.utetra.repository.ToppingRepository;

@Controller
@RequestMapping("/admin/toppings")
public class AdminToppingController {

    @Autowired private ToppingRepository repo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("toppings", repo.findAll());
        return "admin/toppings";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("topping", new Topping());
        return "admin/topping-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("topping", repo.findById(id).orElseThrow());
        return "admin/topping-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Topping t, RedirectAttributes ra) {
        repo.save(t);
        ra.addFlashAttribute("success", "Lưu topping thành công!");
        return "redirect:/admin/toppings";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa topping");
        return "redirect:/admin/toppings";
    }
}