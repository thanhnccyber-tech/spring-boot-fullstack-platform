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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
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
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Việc forward nội bộ tới file JSP / trang lỗi không phải request của người dùng: luôn cho phép
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.INCLUDE).permitAll()
                .requestMatchers(
                        "/", "/home", "/products", "/products/**",
                        "/product/**", "/login", "/register",
                        "/auth/**", "/api/auth/**",
                        "/otp/**",
                        "/forgot-password", "/reset-password",
                        "/static/**", "/ws/**", "/error", "/logout", "/favicon.ico"
                ).permitAll()

                .requestMatchers("/admin/dashboard/**",
                                 "/admin/products/**",
                                 "/admin/categories/**",
                                 "/admin/toppings/**",
                                 "/admin/branches/**",
                                 "/admin/sizes/**",
                                 "/admin/sugars/**",
                                 "/admin/ices/**",
                                 "/admin/reports/**")
                    .hasRole("ADMIN")

                .requestMatchers("/admin/orders/**")
                    .hasAnyRole("ADMIN", "MANAGER", "STAFF")

                .requestMatchers("/admin/users/**")
                    .hasAnyRole("ADMIN", "MANAGER")

                .requestMatchers("/shipper/**")
                    .hasAnyRole("SHIPPER", "ADMIN")

                .requestMatchers(
                        "/cart/**", "/order/**", "/profile/**",
                        "/notifications/**", "/checkout/**", "/review/**"
                ).authenticated()

                // Mặc định TỪ CHỐI: đường dẫn nào chưa khai báo ở trên đều phải đăng nhập
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
            .addFilterBefore(new SecurityHeadersFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new OriginCheckFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}