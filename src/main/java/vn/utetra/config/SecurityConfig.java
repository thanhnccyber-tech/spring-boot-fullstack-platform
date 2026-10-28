package vn.utetra.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.utetra.security.OriginCheckFilter;
import vn.utetra.security.SecurityHeadersFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .logout(l -> l.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.INCLUDE).permitAll()
                .requestMatchers(
                        "/", "/home", "/products", "/products/**",
                        "/product/**", "/login", "/register",
                        "/auth/**", "/api/auth/**",
                        "/otp/**",
                        "/forgot-password", "/reset-password",
                        "/static/**", "/ws/**", "/error", "/logout", "/favicon.ico",
                        "/khuyen-mai", "/khuyen-mai/**", "/hoi-vien", "/cua-hang", "/receive/**",
                        "/cart", "/cart/**", "/payment/vnpay-return", "/payment/vnpay-ipn",
                        "/payment/momo-return", "/payment/momo-ipn"
                ).permitAll()

                // ================================================================
                // ADMIN + MANAGER: Dashboard (ép branch ở controller), Sản phẩm
                // (chỉ toggle hết hàng), Topping (chỉ toggle hết hàng), Khuyến mãi,
                // Voucher (chỉ xem). Các method mutate dùng @PreAuthorize(ADMIN)
                // để khoá lại ở tầng method.
                // ================================================================
                .requestMatchers("/admin/dashboard/**",
                                 "/admin/reports/**",
                                 "/admin/products/**",
                                 "/admin/toppings/**",
                                 "/admin/promotions/**",
                                 "/admin/vouchers/**")
                    .hasAnyRole("ADMIN", "MANAGER")

                // ADMIN-only (giữ nguyên)
                .requestMatchers("/admin/categories/**",
                                 "/admin/branches/**",
                                 "/admin/sizes/**",
                                 "/admin/sugars/**",
                                 "/admin/ices/**")
                    .hasRole("ADMIN")

                .requestMatchers("/admin/orders/**")
                    .hasAnyRole("ADMIN", "MANAGER", "STAFF")

                .requestMatchers("/admin/users/**")
                    .hasAnyRole("ADMIN", "MANAGER")

                .requestMatchers("/shipper/**")
                    .hasRole("SHIPPER")

                .requestMatchers(
                        "/order/**", "/payment/**", "/profile/**",
                        "/notifications/**", "/checkout/**", "/review/**", "/account/**"
                ).authenticated()

                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, ex) -> {
                    String next;
                    if ("GET".equals(request.getMethod())) {
                        next = request.getRequestURI()
                                + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
                    } else {
                        next = "/cart";
                        String ref = request.getHeader("Referer");
                        if (ref != null) {
                            try {
                                java.net.URI u = java.net.URI.create(ref);
                                if (request.getServerName().equalsIgnoreCase(u.getHost()) && u.getRawPath() != null) {
                                    next = u.getRawPath() + (u.getRawQuery() == null ? "" : "?" + u.getRawQuery());
                                }
                            } catch (IllegalArgumentException ignored) { }
                        }
                    }
                    next = vn.utetra.security.SafeRedirect.localPath(next, "/");
                    response.sendRedirect(request.getContextPath() + "/login?need=1&next="
                            + org.springframework.web.util.UriUtils.encodeQueryParam(
                                    next, java.nio.charset.StandardCharsets.UTF_8));
                }))
            .addFilterBefore(new SecurityHeadersFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new OriginCheckFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}