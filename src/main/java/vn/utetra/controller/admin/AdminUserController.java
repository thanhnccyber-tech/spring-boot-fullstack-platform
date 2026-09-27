package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Branch;
import vn.utetra.entity.User;
import vn.utetra.repository.RoleRepository;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.BranchService;

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private BranchService branchService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userRepo.findById(id).orElseThrow());
        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("branches", branchService.all());
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String saveUser(@RequestParam Long id,
                           @RequestParam String fullName,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String address,
                           @RequestParam(required = false) List<Long> roleIds,
                           @RequestParam(required = false) Long branchId,
                           @RequestParam(required = false) String newPassword,
                           @RequestParam(required = false, defaultValue = "false") Boolean enabled,
                           RedirectAttributes ra) {
        User existing = userRepo.findById(id).orElseThrow();
        existing.setFullName(fullName);
        existing.setPhone(phone);
        existing.setAddress(address);
        existing.setEnabled(enabled);

        // Gán vai trò (RBAC)
        if (roleIds != null && !roleIds.isEmpty()) {
            existing.setRoles(new HashSet<>(roleRepo.findAllById(roleIds)));
        }

        // Gán chi nhánh làm việc
        if (branchId != null) {
            Branch b = branchService.get(branchId);
            existing.setBranch(b);
        } else {
            existing.setBranch(null);
        }

        // Đổi mật khẩu (nếu admin nhập)
        if (newPassword != null && !newPassword.isBlank()) {
            existing.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepo.save(existing);
        ra.addFlashAttribute("success", "Cập nhật người dùng thành công!");
        return "redirect:/admin/users";
    }
}