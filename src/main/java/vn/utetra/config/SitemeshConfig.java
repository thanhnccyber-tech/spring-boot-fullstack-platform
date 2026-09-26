package vn.utetra.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SitemeshConfig {

    @Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filter =
                new FilterRegistrationBean<>();
        filter.setFilter(new ConfigurableSiteMeshFilter() {
            @Override
            protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
                builder
                    // Decorator cho admin (chỉ ghi TÊN FILE, SiteMesh tự prepend /WEB-INF/decorators/)
                    .addDecoratorPath("/admin/*", "admin-layout.jsp")
                    // Decorator cho customer
                    .addDecoratorPath("/*",       "customer-layout.jsp")
                    // Bỏ qua các path không cần decorate
                    .addExcludedPath("/static/*")
                    .addExcludedPath("/api/*")
                    .addExcludedPath("/ws/*")
                    .addExcludedPath("/error");
            }
        });
        filter.addUrlPatterns("/*");
        filter.setOrder(1);
        return filter;
    }
}