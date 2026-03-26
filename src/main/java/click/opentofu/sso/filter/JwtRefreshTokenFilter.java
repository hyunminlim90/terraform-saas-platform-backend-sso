package click.opentofu.sso.filter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;
import click.opentofu.sso.util.JwtGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtRefreshTokenFilter implements Filter {

    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;
    
    public JwtRefreshTokenFilter(JwtGenerator jwtGenerator, UserRepository userRepository) {
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        String jwtRefreshToken = null;
        String jwtRefreshTokenEmail = null;
        PublicKey publicKey = jwtGenerator.getVerificationKey();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();
        if (
            path.equals("/api/v1/auth/login") ||
            path.equals("/api/v1/auth/sign-up") ||
            path.equals("/api/v1/auth/email/send-validation-code") ||
            path.equals("/api/v1/public-key")
        ) {
            chain.doFilter(request, response);
            return;
        }
        if (httpRequest.getCookies() != null) {
            Map<String, String> cookies = Arrays.stream(httpRequest.getCookies())
                .collect(Collectors.toMap(
                    (cookie) -> { return cookie.getName(); },
                    (cookie) -> { return cookie.getValue(); }
                ));
            jwtRefreshToken = cookies.get("jwtRefreshToken");
            log.warn("------------------------------------------");
            log.warn("JWT Refresh Token: ");
            log.warn("------------------------------------------");
            log.warn(jwtRefreshToken);
        }
        httpRequest.setAttribute("isJwtRefreshTokenValid", true);
        if (jwtRefreshToken != null) {
            try {
                Jws<Claims> jwtRefreshTokenClaims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jwtRefreshToken);
                Claims jwtRefreshTokenClaimsBody = jwtRefreshTokenClaims.getBody();
                Date jwtRefreshTokenIssuedAt = jwtRefreshTokenClaimsBody.getIssuedAt();
                Date jwtRefreshTokenExpiration = jwtRefreshTokenClaimsBody.getExpiration();
                httpRequest.setAttribute("jwtRefreshTokenExpiration", jwtRefreshTokenExpiration);
                jwtRefreshTokenEmail = jwtRefreshTokenClaimsBody.getSubject();
                httpRequest.setAttribute("jwtRefreshTokenEmail", jwtRefreshTokenEmail);
                log.warn("------------------------------------------");
                log.warn("JWT Refresh Token Issued At: " + jwtRefreshTokenIssuedAt);
                log.warn("JWT Refresh Token Expiration: " + jwtRefreshTokenExpiration);
                log.warn("------------------------------------------");
            } catch (JwtException error) {
                log.warn("------------------------------------------");
                log.warn("JWT Refresh Token has expired");
                log.warn("------------------------------------------");
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "invalidJwtRefreshToken");
                return;
            }
            Optional<UserEntity> userEntity = userRepository.findById(jwtRefreshTokenEmail);
            if (userEntity.isEmpty()) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "jwtRefreshTokenUserEmailDoesNotExist"); 
                return;
            }
        } else {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "expiredJwtRefreshToken"); 
            return;
        }
        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", message);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
    }
}
