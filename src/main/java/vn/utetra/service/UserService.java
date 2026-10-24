package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.dto.RegisterRequest;
import vn.utetra.entity.Role;
import vn.utetra.entity.RoleName;
import vn.utetra.entity.User;
import vn.utetra.repository.RoleRepository;
import vn.utetra.repository.UserRepository;
import vn.utetra.security.PasswordPolicy;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerPending(RegisterRequest req) {
        Optional<User> existing = userRepo.findByEmail(req.getEmail());

        if (existing.isPresent()) {
            User u = existing.get();
            if (Boolean.TRUE.equals(u.getEnabled())) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống");
            }
            u.setPassword(passwordEncoder.encode(req.getPassword()));
            u.setFullName(req.getFullName());
            u.setPhone(req.getPhone());
            return userRepo.save(u);
        }

        Role userRole = roleRepo.findByName(RoleName.USER).orElseGet(() -> {
            Role r = new Role();
            r.setName(RoleName.USER);
            r.setDescription("Khách hàng");
            return roleRepo.save(r);
        });

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setEnabled(false);
        user.setRoles(Set.of(userRole));
        return userRepo.save(user);
    }

    @Transactional
    public void activate(String email) {
        User u = userRepo.findByEmail(email).orElseThrow();
        u.setEnabled(true);
        userRepo.save(u);
    }

    public Optional<User> findByEmail(String email) { return userRepo.findByEmail(email); }

    @Transactional
    public void updateProfile(String email, String fullName, String phone,
                              String address, String avatarUrl) {
        String name = fullName == null ? "" : fullName.trim();
        if (name.isEmpty() || name.length() > 100)
            throw new IllegalArgumentException("Họ tên không được để trống và tối đa 100 ký tự.");
        String ph = phone == null ? "" : phone.trim();
        if (!ph.matches("^(0[0-9]{9,10})?$"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        String addr = address == null ? "" : address.trim();
        if (addr.length() > 255)
            throw new IllegalArgumentException("Địa chỉ tối đa 255 ký tự.");

        User u = userRepo.findByEmail(email).orElseThrow();
        u.setFullName(name);
        u.setPhone(ph.isEmpty() ? null : ph);
        u.setAddress(addr.isEmpty() ? null : addr);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        userRepo.save(u);
    }

    @Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
        User u = userRepo.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(oldPwd, u.getPassword()))
            throw new RuntimeException("Mật khẩu cũ không đúng");
        PasswordPolicy.require(newPwd);
        u.setPassword(passwordEncoder.encode(newPwd));
        userRepo.save(u);
    }

    /**
     * Đặt lại mật khẩu sau khi user đã xác thực OTP reset thành công.
     * Không cần mật khẩu cũ.
     */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User u = userRepo.findByEmail(email).orElseThrow();
        PasswordPolicy.require(newPassword);
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(u);
    }
}