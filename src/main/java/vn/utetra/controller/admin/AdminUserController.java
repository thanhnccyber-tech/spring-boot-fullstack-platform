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
import vn.utetra.entity.*;
import vn.utetra.repository.*;
import vn.utetra.service.BranchService;
import vn.utetra.service.CloudinaryService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminUserController {

    private static final int PAGE_SIZE = 10;

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private BranchService branchService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CloudinaryService cloudinaryService;

    private User me(Authentication a) {
        return userRepo.findByEmail(a.getName()).orElseThrow();
    }

    private List<Role> assignableRoles(User me) {
        if (me.isAdmin()) return roleRepo.findAll();
        return roleRepo.findByName(RoleName.STAFF).map(List::of).orElse(List.of());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String role,
                       @RequestParam(required = false) Long branchId,
                       @RequestParam(required = false) String enabled,
                       @RequestParam(defaultValue = "0") int page,
                       Authentication auth, Model model) {

        User me = me(auth);
        boolean admin = me.isAdmin();

        if (!admin) {
            role = RoleName.STAFF;
            branchId = me.getBranch() != null ? me.getBranch().getId() : -1L;
        }
        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        String roleP = (role == null || role.isBlank()) ? null : role;
        Boolean enabledP = "true".equalsIgnoreCase(enabled) ? Boolean.TRUE
                : "false".equalsIgnoreCase(enabled) ? Boolean.FALSE : null;

        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("id").descending());
        Page<User> userPage = userRepo.search(kwP, roleP, branchId, enabledP, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("kw", kw);
        model.addAttribute("roleFilter", role);
        model.addAttribute("branchFilter", branchId);
        model.addAttribute("enabledFilter", enabled);
        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("branches", admin ? branchService.all()
                : (me.getBranch() != null ? List.of(me.getBranch()) : List.of()));
        model.addAttribute("isAdmin", admin);
        model.addAttribute("currentUserId", me.getId());
        return "admin/users";
    }

    @GetMapping("/new")
    public String createForm(Authentication auth, Model model) {
        User me = me(auth);
        boolean admin = me.isAdmin();
        User u = new User();
        u.setEnabled(true);
        List<Role> assignable = assignableRoles(me);
        if (!admin && !assignable.isEmpty()) u.setRoles(Set.of(assignable.get(0)));
        if (!admin && me.getBranch() != null) u.setBranch(me.getBranch());

        model.addAttribute("user", u);
        model.addAttribute("roles", assignable);
        model.addAttribute("branches", admin ? branchService.all()
                : (me.getBranch() != null ? List.of(me.getBranch()) : List.of()));
        model.addAttribute("isNew", true);
        model.addAttribute("isAdmin", admin);
        return "admin/user-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Authentication auth,
                           Model model, RedirectAttributes ra) {
        User me = me(auth);
        boolean admin = me.isAdmin();
        User target = userRepo.findByIdWithRolesAndBranch(id).orElseThrow();

        if (!admin && !(target.isStaff() && sameBranch(me, target))) {
            ra.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa người dùng này");
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

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                       @RequestParam(required = false) String email,
                       @RequestParam String fullName,
                       @RequestParam(required = false) String phone,
                       @RequestParam(required = false) String address,
                       @RequestParam(required = false) List<Long> roleIds,
                       @RequestParam(required = false) Long branchId,
                       @RequestParam(required = false) String newPassword,
                       @RequestParam(defaultValue = "false") Boolean enabled,
                       @RequestParam(required = false) MultipartFile avatar,
                       Authentication auth, Model model) {

        User me = me(auth);
        boolean admin = me.isAdmin();
        boolean isNew = (id == null);
        User user;

        if (isNew) user = new User();
        else {
            user = userRepo.findByIdWithRolesAndBranch(id).orElseThrow();
            if (!admin && !(user.isStaff() && sameBranch(me, user))) {
                model.addAttribute("error", "Không có quyền");
                model.addAttribute("isNew", isNew);
                model.addAttribute("user", user);
                model.addAttribute("roles", assignableRoles(me));
                model.addAttribute("branches", admin ? branchService.all() : List.of(me.getBranch()));
                return "admin/user-form";
            }
        }

        if (isNew) {
            if (email == null || email.isBlank()) {
                return rejectForm(model, user, isNew, me, admin,
                        "Email bắt buộc", email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
            if (userRepo.existsByEmail(email)) {
                return rejectForm(model, user, isNew, me, admin,
                        "Email đã tồn tại", email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
            if (newPassword == null || newPassword.isBlank()) {
                return rejectForm(model, user, isNew, me, admin,
                        "Mật khẩu bắt buộc khi tạo mới", email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(newPassword));
        } else if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        Set<Role> selected = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) selected.addAll(roleRepo.findAllById(roleIds));
        if (!admin) {
            selected.removeIf(r -> !RoleName.STAFF.equals(r.getName()));
            roleRepo.findByName(RoleName.STAFF).ifPresent(selected::add);
        }

        boolean needsBranch = selected.stream().anyMatch(r ->
                RoleName.STAFF.equals(r.getName()) || RoleName.MANAGER.equals(r.getName()));
        if (needsBranch) {
            if (!admin) user.setBranch(me.getBranch());
            else if (branchId != null) user.setBranch(branchService.get(branchId));
            else {
                return rejectForm(model, user, isNew, me, admin,
                        "Vui lòng chọn chi nhánh cho STAFF/MANAGER",
                        email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
        } else user.setBranch(null);

        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setEnabled(enabled);
        user.setRoles(selected);
        if (avatar != null && !avatar.isEmpty())
            user.setAvatarUrl(cloudinaryService.upload(avatar, "avatars"));

        userRepo.save(user);
        return "redirect:/admin/users";
    }

    private String rejectForm(Model model, User user, boolean isNew, User me, boolean admin,
                              String errorMsg,
                              String email, String fullName, String phone, String address,
                              List<Long> roleIds, Long branchId, Boolean enabled) {
        if (isNew) {
            user.setEmail(email);
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setEnabled(enabled);

        model.addAttribute("user", user);
        model.addAttribute("error", errorMsg);
        model.addAttribute("isNew", isNew);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("roles", assignableRoles(me));
        model.addAttribute("branches", admin ? branchService.all()
                : (me.getBranch() != null ? List.of(me.getBranch()) : List.of()));
        model.addAttribute("selectedRoleIds", roleIds);
        model.addAttribute("selectedBranchId", branchId);
        return "admin/user-form";
    }

    @PostMapping("/toggle-enabled")
    public String toggle(@RequestParam Long id, Authentication auth, RedirectAttributes ra) {
        User me = me(auth);
        if (me.getId().equals(id)) {
            ra.addFlashAttribute("error", "Không thể tự khóa");
            return "redirect:/admin/users";
        }
        User target = userRepo.findByIdWithRolesAndBranch(id).orElseThrow();
        if (!me.isAdmin() && !(target.isStaff() && sameBranch(me, target))) {
            ra.addFlashAttribute("error", "Không có quyền");
            return "redirect:/admin/users";
        }
        target.setEnabled(!Boolean.TRUE.equals(target.getEnabled()));
        userRepo.save(target);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái");
        return "redirect:/admin/users";
    }

    private boolean sameBranch(User a, User b) {
        return a.getBranch() != null && b.getBranch() != null
                && a.getBranch().getId().equals(b.getBranch().getId());
    }
}