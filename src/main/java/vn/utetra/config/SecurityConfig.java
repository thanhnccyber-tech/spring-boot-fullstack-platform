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
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           HttpFirewall httpFirewall) throws Exception {
        http.setSharedObject(HttpFirewall.class, httpFirewall);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home", "/products/**", "/product/**",
                    "/login", "/register", "/auth/**",
                    "/static/**", "/css/**", "/js/**", "/images/**",
                    "/api/auth/**", "/ws/**"
                ).permitAll()
                // Đơn hàng: ADMIN + MANAGER + STAFF
                .requestMatchers("/admin/orders/**")
                    .hasAnyRole("ADMIN", "MANAGER", "STAFF")
                // Quản lý người dùng: ADMIN + MANAGER
                .requestMatchers("/admin/users/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                // Còn lại: chỉ ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Khách hàng đã đăng nhập
                .requestMatchers("/cart/**", "/order/**", "/profile/**", "/notifications/**")
                    .authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}