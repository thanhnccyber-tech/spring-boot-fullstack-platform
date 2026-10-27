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
        FilterRegistrationBean<ConfigurableSiteMeshFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new ConfigurableSiteMeshFilter() {
            @Override
            protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
                builder
                    .addDecoratorPath("/admin/*", "admin-layout.jsp")
                    .addDecoratorPath("/shipper/*", "shipper-layout.jsp")
                    .addDecoratorPath("/*",       "customer-layout.jsp")
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