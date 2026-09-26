package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.dto.RegisterRequest;
import vn.utetra.entity.Role;
import vn.utetra.entity.User;
import vn.utetra.repository.RoleRepository;
import vn.utetra.repository.UserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    r.setDescription("Khách hàng");
                    return roleRepository.save(r);
                });

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setRoles(Set.of(userRole));
        user.setVerificationToken(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateProfile(String email, String fullName, String phone,
                              String address, String avatarUrl) {
        User u = userRepository.findByEmail(email).orElseThrow();
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setAddress(address);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        userRepository.save(u);
    }

    @Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
        User u = userRepository.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(oldPwd, u.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        u.setPassword(passwordEncoder.encode(newPwd));
        userRepository.save(u);
    }
}