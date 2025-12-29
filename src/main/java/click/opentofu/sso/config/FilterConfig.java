package click.opentofu.sso.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import click.opentofu.sso.filter.DomainFilter;
import click.opentofu.sso.filter.JwtAccessTokenFilter;
import click.opentofu.sso.filter.JwtRefreshTokenFilter;
import click.opentofu.sso.repository.UserRepository;
import click.opentofu.sso.util.JwtGenerator;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;
    
    @Bean
    public FilterRegistrationBean<DomainFilter> domainFilterRegistration() {
        FilterRegistrationBean<DomainFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DomainFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAccessTokenFilter> jwtAccessTokenFilterRegistration() {
        FilterRegistrationBean<JwtAccessTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAccessTokenFilter(jwtGenerator, userRepository));
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtRefreshTokenFilter> jwtRefreshTokenFilterRegistration() {
        FilterRegistrationBean<JwtRefreshTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtRefreshTokenFilter(jwtGenerator, userRepository));
        registration.addUrlPatterns("/*");
        registration.setOrder(3);
        return registration;
    }
}
