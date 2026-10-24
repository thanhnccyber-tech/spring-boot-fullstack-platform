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
                .requestMatchers(
                        "/", "/home", "/products", "/products/**",
                        "/product/**", "/login", "/register",
                        "/auth/**", "/api/auth/**",
                        "/otp/**",
                        "/forgot-password", "/reset-password",
                        "/static/**", "/ws/**", "/error"
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

                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}