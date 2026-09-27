package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Role;
import vn.utetra.entity.User;
import vn.utetra.repository.RoleRepository;
import vn.utetra.repository.UserRepository;
import vn.utetra.service.BranchService;
import vn.utetra.service.CloudinaryService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminUserController {

    private static final int PAGE_SIZE = 10;

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private BranchService branchService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CloudinaryService cloudinaryService;

    /* ==================== HELPERS ==================== */

    private User current(Authentication auth) {
        return userRepo.findByEmail(auth.getName()).orElseThrow();
    }

    private boolean isAdmin(User u) {
        return u.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
    }

    private boolean isStaff(User u) {
        return u.getRoles().stream().anyMatch(r -> "ROLE_STAFF".equals(r.getName()));
    }

    private boolean sameBranch(User a, User b) {
        return a.getBranch() != null && b.getBranch() != null
                && a.getBranch().getId().equals(b.getBranch().getId());
    }

    /** Roles mà user hiện tại được phép gán cho người khác. */
    private List<Role> assignableRoles(User me) {
        if (isAdmin(me)) return roleRepo.findAll();
        // MANAGER: chỉ STAFF
        return roleRepo.findByName("ROLE_STAFF").map(List::of).orElse(List.of());
    }

    private String safeReturn(String url) {
        return (url == null || url.isBlank()) ? "/admin/users" : url;
    }

    /* ==================== LIST ==================== */

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String role,
                       @RequestParam(required = false) Long branchId,
                       @RequestParam(required = false) String enabled,
                       @RequestParam(defaultValue = "0") int page,
                       Authentication auth,
                       Model model) {
        User me = current(auth);
        boolean admin = isAdmin(me);

        // Data Scope: MANAGER bị khóa vào STAFF chi nhánh mình
        if (!admin) {
            role = "ROLE_STAFF";
            branchId = (me.getBranch() != null) ? me.getBranch().getId() : -1L;
        }

        String kwParam   = (kw == null || kw.isBlank()) ? null : kw.trim();
        String roleParam = (role == null || role.isBlank()) ? null : role;
        Boolean enabledBool = null;
        if ("true".equalsIgnoreCase(enabled))  enabledBool = Boolean.TRUE;
        if ("false".equalsIgnoreCase(enabled)) enabledBool = Boolean.FALSE;

        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by("id").descending());
        Page<User> userPage = userRepo.search(kwParam, roleParam, branchId, enabledBool, pageable);

        model.addAttribute("userPage",      userPage);
        model.addAttribute("kw",            kw);
        model.addAttribute("roleFilter",    role);
        model.addAttribute("branchFilter",  branchId);
        model.addAttribute("enabledFilter", enabled);
        model.addAttribute("roles",         roleRepo.findAll());
        model.addAttribute("branches",      admin ? branchService.all()
                : (me.getBranch() != null ? List.of(me.getBranch()) : List.of()));
        model.addAttribute("isAdmin",       admin);
        model.addAttribute("currentUserId", me.getId());
        return "admin/users";
    }

    /* ==================== CREATE FORM ==================== */

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) String presetRole,
                             Authentication auth,
                             Model model) {
        User me = current(auth);
        boolean admin = isAdmin(me);
        List<Role> assignable = assignableRoles(me);

        User u = new User();
        u.setEnabled(true);

        // Preset role
        if (presetRole != null && !presetRole.isBlank()) {
            assignable.stream()
                    .filter(r -> r.getName().equals(presetRole))
                    .findFirst()
                    .ifPresent(r -> u.setRoles(Set.of(r)));
        } else if (!admin && !assignable.isEmpty()) {
            u.setRoles(Set.of(assignable.get(0)));   // MANAGER mặc định STAFF
        }

        // MANAGER: auto gán branch mình
        if (!admin && me.getBranch() != null) {
            u.setBranch(me.getBranch());
        }

        model.addAttribute("user", u);
        model.addAttribute("roles", assignable);
        model.addAttribute("branches", admin ? branchService.all()
                : (me.getBranch() != null ? List.of(me.getBranch()) : List.of()));
        model.addAttribute("isNew", true);
        model.addAttribute("isAdmin", admin);
        return "admin/user-form";
    }

    /* ==================== EDIT FORM ==================== */

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Authentication auth,
                           Model model,
                           RedirectAttributes ra) {
        User me = current(auth);
        boolean admin = isAdmin(me);
        User target = userRepo.findById(id).orElseThrow();

        // Data Scope check
        if (!admin && !(isStaff(target) && sameBranch(me, target))) {
            ra.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa người dùng này!");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", target);
        model.addAttribute("roles", assignableRoles(me));
        model.addAttribute("branches", admin ? branchService.all()
                : (me.getBranch() != null ? List.of(me.getBranch()) : List.of()));
        model.addAttribute("isNew", false);
        model.addAttribute("isAdmin", admin);
        return "admin/user-form";
    }

    /* ==================== SAVE ==================== */

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam(required = false) String email,
                       @RequestParam String fullName,
                       @RequestParam(required = false) String phone,
                       @RequestParam(required = false) String address,
                       @RequestParam(required = false) List<Long> roleIds,
                       @RequestParam(required = false) Long branchId,
                       @RequestParam(required = false) String newPassword,
                       @RequestParam(required = false, defaultValue = "false") Boolean enabled,
                       @RequestParam(required = false) MultipartFile avatar,
                       Authentication auth,
                       RedirectAttributes ra) {

        User me = current(auth);
        boolean admin = isAdmin(me);
        boolean isNew = (id == null);
        User user;

        // ---- Authorization ----
        if (isNew) {
            user = new User();
        } else {
            user = userRepo.findById(id).orElseThrow();
            if (!admin && !(isStaff(user) && sameBranch(me, user))) {
                ra.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa người dùng này!");
                return "redirect:/admin/users";
            }
        }

        // ---- Validate cho create ----
        if (isNew) {
            if (email == null || email.isBlank()) {
                ra.addFlashAttribute("error", "Email là bắt buộc!");
                return "redirect:/admin/users/new";
            }
            if (userRepo.existsByEmail(email)) {
                ra.addFlashAttribute("error", "Email đã tồn tại!");
                return "redirect:/admin/users/new";
            }
            if (newPassword == null || newPassword.isBlank()) {
                ra.addFlashAttribute("error", "Mật khẩu là bắt buộc khi tạo mới!");
                return "redirect:/admin/users/new";
            }
        }

        // ---- Roles ----
        Set<Role> selectedRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            selectedRoles.addAll(roleRepo.findAllById(roleIds));
        }
        if (!admin) {
            // MANAGER chỉ được gán STAFF
            selectedRoles.removeIf(r -> !"ROLE_STAFF".equals(r.getName()));
            roleRepo.findByName("ROLE_STAFF").ifPresent(selectedRoles::add);
        }

        // ---- Branch constraint ----
        boolean needsBranch = selectedRoles.stream()
                .anyMatch(r -> "ROLE_STAFF".equals(r.getName())
                            || "ROLE_MANAGER".equals(r.getName()));

        if (needsBranch) {
            if (!admin) {
                if (me.getBranch() == null) {
                    ra.addFlashAttribute("error", "Tài khoản quản lý chưa được gán chi nhánh!");
                    return "redirect:/admin/users";
                }
                user.setBranch(me.getBranch());
            } else if (branchId != null) {
                user.setBranch(branchService.get(branchId));
            } else {
                ra.addFlashAttribute("error",
                        "Vui lòng chọn Chi nhánh cho tài khoản STAFF/MANAGER!");
                return "redirect:/admin/users" + (isNew ? "/new" : "/edit/" + id);
            }
        } else {
            user.setBranch(null);
        }

        // ---- Fields ----
        if (isNew) {
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(newPassword));
        } else if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setEnabled(enabled);
        user.setRoles(selectedRoles);

        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatarUrl(cloudinaryService.upload(avatar, "avatars"));
        }

        userRepo.save(user);
        ra.addFlashAttribute("success",
                isNew ? "Thêm người dùng thành công!" : "Cập nhật thành công!");
        return "redirect:/admin/users";
    }

    /* ==================== TOGGLE ENABLED ==================== */

    @PostMapping("/toggle-enabled")
    public String toggleEnabled(@RequestParam Long id,
                                @RequestParam(required = false) String returnUrl,
                                Authentication auth,
                                RedirectAttributes ra) {
        User me = current(auth);
        User target = userRepo.findById(id).orElseThrow();

        // Self-lock prevention
        if (me.getId().equals(id)) {
            ra.addFlashAttribute("error", "Bạn không thể tự khóa chính mình!");
            return "redirect:" + safeReturn(returnUrl);
        }

        // Data scope
        if (!isAdmin(me) && !(isStaff(target) && sameBranch(me, target))) {
            ra.addFlashAttribute("error", "Bạn không có quyền thao tác người dùng này!");
            return "redirect:" + safeReturn(returnUrl);
        }

        target.setEnabled(!Boolean.TRUE.equals(target.getEnabled()));
        userRepo.save(target);
        ra.addFlashAttribute("success",
                Boolean.TRUE.equals(target.getEnabled())
                        ? "Đã mở khóa tài khoản " + target.getEmail()
                        : "Đã khóa tài khoản " + target.getEmail());
        return "redirect:" + safeReturn(returnUrl);
    }

    /* ==================== RESET PASSWORD ==================== */

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam Long id,
                                @RequestParam String newPassword,
                                @RequestParam(required = false) String returnUrl,
                                Authentication auth,
                                RedirectAttributes ra) {
        User me = current(auth);
        User target = userRepo.findById(id).orElseThrow();

        if (!isAdmin(me) && !(isStaff(target) && sameBranch(me, target))) {
            ra.addFlashAttribute("error", "Bạn không có quyền thao tác người dùng này!");
            return "redirect:" + safeReturn(returnUrl);
        }

        if (newPassword == null || newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:" + safeReturn(returnUrl);
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(target);
        ra.addFlashAttribute("success", "Đã cấp lại mật khẩu cho " + target.getEmail());
        return "redirect:" + safeReturn(returnUrl);
    }
}