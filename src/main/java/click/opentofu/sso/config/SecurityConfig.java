package click.opentofu.sso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf((csrf) -> {
                csrf
                    .ignoringRequestMatchers(
                        (request) -> request.getRequestURI().equals("/api/v1/auth/login-check"),
                        (request) -> request.getRequestURI().equals("/api/v1/auth/login"),
                        (request) -> request.getRequestURI().equals("/api/v1/auth/sign-up"),
                        (request) -> request.getRequestURI().equals("/api/v1/auth/logout"),
                        (request) -> request.getRequestURI().equals("/api/v1/auth/email/send-validation-code"),
                        (request) -> request.getRequestURI().equals("/api/v1/request/accounts/load-approvals"),
                        (request) -> request.getRequestURI().equals("/api/v1/request/accounts/load-all"),
                        (request) -> request.getRequestURI().equals("/api/v1/request/accounts/approve"),
                        (request) -> request.getRequestURI().equals("/api/v1/public-key")
                    );
            });
        return http.build();
    }
}
