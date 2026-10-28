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
    @Autowired private vn.utetra.repository.NotificationRepository notiRepo;
    @Autowired private vn.utetra.service.OtpService otpService;
    @Autowired private vn.utetra.security.TokenBlacklist tokenBlacklist;

    @Transactional
    public User registerPending(RegisterRequest req) {
        Optional<User> existing = userRepo.findByEmail(req.getEmail());

        if (existing.isPresent()) {
            User u = existing.get();
            if (Boolean.TRUE.equals(u.getEnabled())) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống");
            }
            // Chống chiếm tài khoản đang chờ kích hoạt: nếu chủ email đang giữ một OTP còn hạn thì
            // người khác không được đăng ký lại cùng email để đổi mật khẩu (rồi chủ email nhập OTP là kích hoạt
            // với mật khẩu của kẻ tấn công).
            if (otpService.hasActive(u.getEmail(), vn.utetra.entity.OtpToken.TYPE_REGISTER)) {
                throw new RuntimeException("Email này đang chờ xác thực OTP. Hãy nhập mã đã gửi tới email "
                        + "hoặc thử lại sau 5 phút.");
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

        vn.utetra.entity.Notification n = new vn.utetra.entity.Notification();
        n.setUserId(u.getId());
        n.setTitle("Kích hoạt Thành viên thành công");
        n.setContent("Bạn đã trở thành Thành viên UTeTra. Mua hàng để tích điểm (10.000đ = 1 điểm) và nhận ưu đãi dành riêng cho hội viên.");
        n.setType("MEMBER");
        notiRepo.save(n);
    }

    public Optional<User> findByEmail(String email) { return userRepo.findByEmail(email); }

    /** true nếu đây là lần đăng nhập đầu tiên của hội viên mới (và đánh dấu đã chào). */
    @Transactional
    public boolean markWelcomedFirstTime(User u) {
        if (Boolean.TRUE.equals(u.getWelcomed())) return false;
        u.setWelcomed(true);
        userRepo.save(u);
        return true;
    }

    /** Tài khoản chưa có thông báo "trở thành hội viên" (ví dụ tài khoản tạo sẵn) thì tạo ở lần đăng nhập đầu. */
    @Transactional
    public void ensureMemberNotification(User u) {
        if (notiRepo.existsByUserIdAndType(u.getId(), "MEMBER")) return;
        vn.utetra.entity.Notification n = new vn.utetra.entity.Notification();
        n.setUserId(u.getId());
        n.setTitle("Kích hoạt Thành viên thành công");
        n.setContent("Bạn đã trở thành Thành viên UTeTra. Mua hàng để tích điểm (10.000đ = 1 điểm) và nhận ưu đãi dành riêng cho hội viên.");
        n.setType("MEMBER");
        notiRepo.save(n);
    }

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
        tokenBlacklist.revokeAllFor(u.getEmail());
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
        tokenBlacklist.revokeAllFor(u.getEmail());
    }
}