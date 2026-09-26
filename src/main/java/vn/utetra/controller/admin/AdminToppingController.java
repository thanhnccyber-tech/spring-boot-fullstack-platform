package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Topping;
import vn.utetra.service.ToppingService;

@Controller
@RequestMapping("/admin/toppings")
public class AdminToppingController {

    @Autowired private ToppingService toppingService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("toppings", toppingService.all());
        return "admin/toppings";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("topping", new Topping());
        return "admin/topping-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("topping", toppingService.get(id));
        return "admin/topping-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Topping topping, RedirectAttributes ra) {
        toppingService.save(topping);
        ra.addFlashAttribute("success", "Lưu topping thành công!");
        return "redirect:/admin/toppings";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        toppingService.delete(id);
        return "redirect:/admin/toppings";
    }
}