package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Branch;
import vn.utetra.repository.BranchRepository;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/branches")
public class AdminBranchController {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0[0-9]{9,10}$");

    @Autowired private BranchRepository repo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", repo.findAll());
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
        Branch b = repo.findById(id).orElseThrow();
        model.addAttribute("branch", b);
        model.addAttribute("isNew", false);
        return "admin/branch-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Branch b,
                       @RequestParam(defaultValue = "false") Boolean active,
                       Model model,
                       RedirectAttributes ra) {

        boolean isNew = (b.getId() == null);

        // ===== Trim dữ liệu =====
        String name    = b.getName()    == null ? "" : b.getName().trim();
        String address = b.getAddress() == null ? "" : b.getAddress().trim();
        String phone   = b.getPhone()   == null ? "" : b.getPhone().trim();

        // ===== Validate =====
        String error = null;
        if (name.isEmpty()) {
            error = "Tên chi nhánh không được để trống.";
        } else if (name.length() > 150) {
            error = "Tên chi nhánh không được vượt quá 150 ký tự.";
        } else if (address.isEmpty()) {
            error = "Địa chỉ không được để trống.";
        } else if (address.length() > 255) {
            error = "Địa chỉ không được vượt quá 255 ký tự.";
        } else if (phone.isEmpty()) {
            error = "Số điện thoại không được để trống.";
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            error = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0, từ 10 đến 11 chữ số).";
        }

        if (error != null) {
            b.setName(name);
            b.setAddress(address);
            b.setPhone(phone);
            b.setActive(active);
            model.addAttribute("branch", b);
            model.addAttribute("isNew", isNew);
            model.addAttribute("error", error);
            return "admin/branch-form";
        }

        b.setName(name);
        b.setAddress(address);
        b.setPhone(phone);
        b.setActive(active);

        repo.save(b);
        ra.addFlashAttribute("success",
                isNew ? "Đã thêm chi nhánh mới." : "Đã cập nhật chi nhánh.");
        return "redirect:/admin/branches";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Branch b = repo.findById(id).orElseThrow();
        b.setActive(false);
        repo.save(b);
        ra.addFlashAttribute("success", "Đã khóa chi nhánh.");
        return "redirect:/admin/branches";
    }

    @GetMapping("/restore/{id}")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        Branch b = repo.findById(id).orElseThrow();
        b.setActive(true);
        repo.save(b);
        ra.addFlashAttribute("success", "Đã mở khóa chi nhánh.");
        return "redirect:/admin/branches";
    }
}