package vn.utetra.config;

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
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers(
                        "/", "/home", "/products", "/products/**",
                        "/product/**", "/login", "/register",
                        "/auth/**", "/api/auth/**",
                        "/otp/**",
                        "/static/**", "/ws/**", "/error"
                ).permitAll()

                // Admin toàn quyền
                .requestMatchers("/admin/dashboard/**",
                                 "/admin/products/**",
                                 "/admin/categories/**",
                                 "/admin/toppings/**",
                                 "/admin/branches/**",
                                 "/admin/reports/**")
                    .hasRole("ADMIN")

                // Đơn hàng: Admin + Manager + Staff
                .requestMatchers("/admin/orders/**")
                    .hasAnyRole("ADMIN", "MANAGER", "STAFF")

                // Người dùng: Admin + Manager
                .requestMatchers("/admin/users/**")
                    .hasAnyRole("ADMIN", "MANAGER")

                // Shipper
                .requestMatchers("/shipper/**")
                    .hasAnyRole("SHIPPER", "ADMIN")

                // Khách đã đăng nhập
                .requestMatchers(
                        "/cart/**", "/order/**", "/profile/**",
                        "/notifications/**", "/checkout/**"
                ).authenticated()

                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}