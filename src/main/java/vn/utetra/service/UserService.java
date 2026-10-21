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

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Bước 1 đăng ký: tạo user ở trạng thái enabled=false,
     * password đã mã hóa, chưa kích hoạt.
     */
    @Transactional
    public User registerPending(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email đã tồn tại trong hệ thống");

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
        User u = userRepo.findByEmail(email).orElseThrow();
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setAddress(address);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        userRepo.save(u);
    }

    @Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
        User u = userRepo.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(oldPwd, u.getPassword()))
            throw new RuntimeException("Mật khẩu cũ không đúng");
        if (newPwd == null || newPwd.length() < 6)
            throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự");
        u.setPassword(passwordEncoder.encode(newPwd));
        userRepo.save(u);
    }
}