package vn.utetra.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.utetra.entity.User;
import vn.utetra.repository.UserRepository;
import vn.utetra.util.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_COOKIE = "UTETRA_JWT";

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepo;
    @Autowired private vn.utetra.security.TokenBlacklist blacklist;

    /** Tài nguyên tĩnh không cần xác thực nên bỏ qua (đỡ truy vấn DB). */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith(request.getContextPath() + "/static/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (JWT_COOKIE.equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }

        if (token != null && !blacklist.isRevoked(token) && jwtUtil.isValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtUtil.getEmail(token);
            java.util.Date issuedAt = jwtUtil.parse(token).getIssuedAt();
            if (issuedAt != null && blacklist.isIssuedBeforeRevocation(email, issuedAt.getTime())) {
                chain.doFilter(request, response);
                return;
            }

            // Đối chiếu lại với database: tài khoản bị khoá/xoá thì token cũ vô hiệu ngay,
            // và quyền (role) luôn lấy mới nhất từ DB thay vì tin quyền cũ trong token.
            Optional<User> user = userRepo.findByEmail(email);
            if (user.isPresent() && Boolean.TRUE.equals(user.get().getEnabled())) {
                List<SimpleGrantedAuthority> authorities = user.get().getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .toList();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Header/JSP dựa vào HttpSession (userId, fullName, roles) còn đăng nhập thật dựa vào JWT.
                // Phiên HTTP mất (hết 30 phút, quay về từ VNPay, restart server...) trong khi JWT còn hạn
                // sẽ làm giao diện hiện như "bị đăng xuất": dựng lại phiên từ JWT/DB.
                jakarta.servlet.http.HttpSession session = request.getSession();
                if (session.getAttribute("userId") == null) {
                    session.setAttribute("userId",   user.get().getId());
                    session.setAttribute("fullName", user.get().getFullName());
                    session.setAttribute("email",    user.get().getEmail());
                    session.setAttribute("roles",    authorities.stream().map(SimpleGrantedAuthority::getAuthority).toList());
                }
            }
        }
        chain.doFilter(request, response);
    }
}
