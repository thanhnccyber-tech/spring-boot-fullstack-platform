package vn.utetra.controller.admin;

import vn.utetra.security.SafeRedirect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Topping;
import vn.utetra.service.ToppingService;

@Controller
@RequestMapping("/admin/toppings")
public class AdminToppingController {

    private static final int PAGE_SIZE = 10;

    @Autowired private ToppingService toppingService;

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String active,
                       @RequestParam(required = false) String outOfStock,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        Boolean activeP = "true".equalsIgnoreCase(active) ? Boolean.TRUE
                        : "false".equalsIgnoreCase(active) ? Boolean.FALSE
                        : null;

        Boolean outOfStockP = "true".equalsIgnoreCase(outOfStock) ? Boolean.TRUE
                            : "false".equalsIgnoreCase(outOfStock) ? Boolean.FALSE
                            : null;

        Page<Topping> pageData = toppingService.searchForAdmin(
                kw, activeP, outOfStockP, page, PAGE_SIZE);

        model.addAttribute("pageData", pageData);
        model.addAttribute("kw", kw);
        model.addAttribute("activeFilter", active);
        model.addAttribute("outOfStockFilter", outOfStock);
        return "admin/toppings";
    }

    @GetMapping("/new")
    public String create(Model model) {
        Topping t = new Topping();
        t.setActive(true);
        t.setOutOfStock(false);
        model.addAttribute("topping", t);
        model.addAttribute("isNew", true);
        return "admin/topping-form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("topping", toppingService.get(id));
        model.addAttribute("isNew", false);
        return "admin/topping-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Topping t,
                       @RequestParam(defaultValue = "false") Boolean active,
                       @RequestParam(defaultValue = "false") Boolean outOfStock,
                       Model model,
                       RedirectAttributes ra) {
        boolean isNew = (t.getId() == null);
        try {
            t.setActive(active);
            t.setOutOfStock(outOfStock);
            toppingService.save(t);
            ra.addFlashAttribute("success",
                    isNew ? "Đã thêm topping." : "Đã cập nhật topping.");
            return "redirect:/admin/toppings";
        } catch (IllegalArgumentException ex) {
            t.setActive(active);
            t.setOutOfStock(outOfStock);
            model.addAttribute("topping", t);
            model.addAttribute("isNew", isNew);
            model.addAttribute("error", ex.getMessage());
            return "admin/topping-form";
        }
    }

    @PostMapping("/toggle-active")
    public String toggleActive(@RequestParam Long id,
                               @RequestParam(required = false) String returnUrl,
                               RedirectAttributes ra) {
        toppingService.toggleActive(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái hiển thị.");
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/toppings");
    }

    @PostMapping("/toggle-stock")
    public String toggleStock(@RequestParam Long id,
                              @RequestParam(required = false) String returnUrl,
                              RedirectAttributes ra) {
        toppingService.toggleOutOfStock(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái tồn kho.");
        return "redirect:" + SafeRedirect.localPath(returnUrl, "/admin/toppings");
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        try {
            toppingService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa topping.");
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Không thể xóa topping (đang được dùng trong đơn hàng).");
        }
        return "redirect:/admin/toppings";
    }
}