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
import vn.utetra.service.FileValidator;
import vn.utetra.security.PasswordPolicy;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired private FileValidator fileValidator;
    @Autowired private vn.utetra.security.TokenBlacklist tokenBlacklist;

    private User me(Authentication a) {
        return userRepo.findByEmailWithBranch(a.getName()).orElseThrow();
    }

    private List<Role> assignableRoles(User me) {
        if (me.isAdmin()) return roleRepo.findAll();
        return roleRepo.findByName(RoleName.STAFF).map(List::of).orElse(List.of());
    }

    private Page<User> hydrate(Page<User> page) {
        List<Long> ids = page.getContent().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return page;

        List<User> full = userRepo.findByIdsWithRolesAndBranch(ids);
        Map<Long, User> map = full.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<User> ordered = ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(ordered, page.getPageable(), page.getTotalElements());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) String role,
                       @RequestParam(required = false) Long branchId,
                       @RequestParam(required = false) String enabled,
                       @RequestParam(defaultValue = "0") int page,
                       Authentication auth, Model model,
                       RedirectAttributes ra) {

        User me = me(auth);
        boolean admin = me.isAdmin();

        String roleP;
        String excludeRoleP;
        Long branchIdP;

        if (admin) {
            roleP = (role == null || role.isBlank()) ? null : role;
            excludeRoleP = null;
            branchIdP = branchId;
        } else {
            if (me.getBranch() == null) {
                ra.addFlashAttribute("error",
                        "Tài khoản Manager chưa được gán chi nhánh. Vui lòng liên hệ Admin.");
                return "redirect:/admin/dashboard";
            }
            roleP = RoleName.STAFF;
            excludeRoleP = RoleName.ADMIN;
            branchIdP = me.getBranch().getId();
        }

        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        Boolean enabledP = "true".equalsIgnoreCase(enabled) ? Boolean.TRUE
                : "false".equalsIgnoreCase(enabled) ? Boolean.FALSE : null;

        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("id").descending());
        Page<User> raw = userRepo.search(kwP, roleP, excludeRoleP, branchIdP, enabledP, pageable);
        Page<User> userPage = hydrate(raw);

        model.addAttribute("userPage", userPage);
        model.addAttribute("kw", kw);
        model.addAttribute("roleFilter", roleP);
        model.addAttribute("branchFilter", branchIdP);
        model.addAttribute("enabledFilter", enabled);
        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("branches", admin ? branchService.all()
                : List.of(me.getBranch()));
        model.addAttribute("isAdmin", admin);
        model.addAttribute("currentUserId", me.getId());
        model.addAttribute("managerBranchName", !admin && me.getBranch() != null
                ? me.getBranch().getName() : null);
        return "admin/users";
    }

    @GetMapping("/new")
    public String createForm(Authentication auth, Model model, RedirectAttributes ra) {
        User me = me(auth);
        boolean admin = me.isAdmin();

        if (!admin && me.getBranch() == null) {
            ra.addFlashAttribute("error",
                    "Tài khoản Manager chưa được gán chi nhánh.");
            return "redirect:/admin/dashboard";
        }

        User u = new User();
        u.setEnabled(true);
        List<Role> assignable = assignableRoles(me);
        if (!admin && !assignable.isEmpty()) u.setRoles(Set.of(assignable.get(0)));
        if (!admin) u.setBranch(me.getBranch());

        model.addAttribute("user", u);
        model.addAttribute("roles", assignable);
        model.addAttribute("branches", admin ? branchService.all()
                : List.of(me.getBranch()));
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

        if (!admin) {
            boolean isStaffOnly = target.isStaff() && !target.isAdmin()
                    && !target.isManager() && !target.isShipper();
            if (!isStaffOnly || !sameBranch(me, target)) {
                ra.addFlashAttribute("error",
                        "Bạn không có quyền chỉnh sửa người dùng này.");
                return "redirect:/admin/users";
            }
        }
        model.addAttribute("user", target);
        model.addAttribute("roles", assignableRoles(me));
        model.addAttribute("branches", admin ? branchService.all()
                : List.of(me.getBranch()));
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
                       Authentication auth, Model model, RedirectAttributes ra) {

        User me = me(auth);
        boolean admin = me.isAdmin();
        boolean isNew = (id == null);
        User user;

        if (isNew) user = new User();
        else {
            user = userRepo.findByIdWithRolesAndBranch(id).orElseThrow();
            if (!admin) {
                boolean isStaffOnly = user.isStaff() && !user.isAdmin()
                        && !user.isManager() && !user.isShipper();
                if (!isStaffOnly || !sameBranch(me, user)) {
                    ra.addFlashAttribute("error", "Không có quyền.");
                    return "redirect:/admin/users";
                }
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
            if (!PasswordPolicy.isValid(newPassword)) {
                return rejectForm(model, user, isNew, me, admin,
                        PasswordPolicy.MESSAGE, email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
            user.setEmail(email.trim());
            user.setPassword(passwordEncoder.encode(newPassword));
        } else if (newPassword != null && !newPassword.isBlank()) {
            if (!PasswordPolicy.isValid(newPassword)) {
                return rejectForm(model, user, isNew, me, admin,
                        PasswordPolicy.MESSAGE, email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            tokenBlacklist.revokeAllFor(user.getEmail());
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

        if (avatar != null && !avatar.isEmpty()) {
            try {
                fileValidator.validateImage(avatar);
            } catch (IllegalArgumentException ex) {
                return rejectForm(model, user, isNew, me, admin,
                        ex.getMessage(), email, fullName, phone, address,
                        roleIds, branchId, enabled);
            }
            user.setAvatarUrl(cloudinaryService.upload(avatar, "avatars"));
        }

        userRepo.save(user);
        ra.addFlashAttribute("success", isNew ? "Đã thêm" : "Đã cập nhật");
        return "redirect:/admin/users";
    }

    private String rejectForm(Model model, User user, boolean isNew, User me, boolean admin,
                              String errorMsg,
                              String email, String fullName, String phone, String address,
                              List<Long> roleIds, Long branchId, Boolean enabled) {
        if (isNew) user.setEmail(email);
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
                : List.of(me.getBranch()));
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
        if (!me.isAdmin()) {
            boolean isStaffOnly = target.isStaff() && !target.isAdmin()
                    && !target.isManager() && !target.isShipper();
            if (!isStaffOnly || !sameBranch(me, target)) {
                ra.addFlashAttribute("error", "Không có quyền.");
                return "redirect:/admin/users";
            }
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