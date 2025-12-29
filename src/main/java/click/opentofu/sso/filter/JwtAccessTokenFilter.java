package click.opentofu.sso.filter;

import java.io.IOException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.security.PublicKey;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;
import click.opentofu.sso.util.JwtGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
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
public class JwtAccessTokenFilter implements Filter {

    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;

    public JwtAccessTokenFilter(JwtGenerator jwtGenerator, UserRepository userRepository) {
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
    }
    
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        String jwtAccessToken = null;
        String jwtAccessTokenEmail = null;
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
            jwtAccessToken = cookies.get("jwtAccessToken");
            log.warn("------------------------------------------");
            log.warn("JWT Access Token: ");
            log.warn("------------------------------------------");
            log.warn(jwtAccessToken);
        }
        httpRequest.setAttribute("isJwtAccessTokenValid", true);
        if (jwtAccessToken != null) {
            try {
                Jws<Claims> jwtAccessTokenClaims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jwtAccessToken);
                Claims jwtAccessTokenClaimsBody = jwtAccessTokenClaims.getBody();
                Date jwtAccessTokenIssuedAt = jwtAccessTokenClaimsBody.getIssuedAt();
                Date jwtAccessTokenExpiration = jwtAccessTokenClaimsBody.getExpiration();
                jwtAccessTokenEmail = jwtAccessTokenClaimsBody.getSubject();
                httpRequest.setAttribute("jwtAccessTokenEmail", jwtAccessTokenEmail);
                httpRequest.setAttribute("jwtAccessTokenExpiration", jwtAccessTokenExpiration);
                log.warn("------------------------------------------");
                log.warn("JWT Access Token Issued At: " + jwtAccessTokenIssuedAt);
                log.warn("JWT Access Token Expiration: " + jwtAccessTokenExpiration);
                log.warn("------------------------------------------");
            } catch (ExpiredJwtException expiredException) {
                httpRequest.setAttribute("isJwtAccessTokenValid", false);
                jwtAccessTokenEmail = expiredException.getClaims().getSubject();
                httpRequest.setAttribute("jwtAccessTokenEmail", jwtAccessTokenEmail);
                log.warn("------------------------------------------");
                log.warn("JWT Access Token has expired");
                log.warn("------------------------------------------");
            } catch (JwtException error) { 
                httpRequest.setAttribute("isJwtAccessTokenValid", false);
            } catch (Exception error) {
                httpRequest.setAttribute("isJwtAccessTokenValid", false);
            }
            @SuppressWarnings("null")
            Optional<UserEntity> userEntity = userRepository.findById(jwtAccessTokenEmail.split("@")[0]);
            if (userEntity.isEmpty()) {
                httpRequest.setAttribute("isJwtAccessTokenValid", false);
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "jwtAccessTokenUserEmailDoesNotExist");
                return;
            }
        } else {
            httpRequest.setAttribute("isJwtAccessTokenValid", false);
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
