package click.opentofu.sso.controller;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import click.opentofu.sso.service.LoginCheckService;
import click.opentofu.sso.util.JwtGenerator;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class LoginCheck {

    private final LoginCheckService loginCheckService;
    private final JwtGenerator jwtGenerator;
    
    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        },
        allowCredentials = "true"
    )
    @PostMapping(path = "/login-check")
    public ResponseEntity<Map<String, Object>> loginCheck (
        HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> unauthorizedResponse = new HashMap<>();
        String jwtAccessTokenEmail = (String) request.getAttribute("jwtAccessTokenEmail");
        String jwtRefreshTokenEmail = (String) request.getAttribute("jwtRefreshTokenEmail");
        Boolean isJwtAccessTokenValid = (Boolean) request.getAttribute("isJwtAccessTokenValid");
        Boolean isJwtRefreshTokenValid = (Boolean) request.getAttribute("isJwtRefreshTokenValid");
        Date now = new Date();
        Date jwtAccessTokenExpiration = (Date) request.getAttribute("jwtAccessTokenExpiration");
        Date jwtRefreshTokenExpiration = (Date) request.getAttribute("jwtRefreshTokenExpiration");
        long remainingTimeInMillis = 0;
        long refreshTokenRemainingTime = 0;
        if (jwtAccessTokenExpiration != null) {
            remainingTimeInMillis = (jwtAccessTokenExpiration.getTime() - now.getTime()) + 500;
        }
        if (jwtRefreshTokenExpiration != null) {
            refreshTokenRemainingTime = (jwtRefreshTokenExpiration.getTime() - now.getTime());
        }
        response.put("remainingTimeInMillis", remainingTimeInMillis);
        response.put("refreshTokenRemainingTime", refreshTokenRemainingTime);
        log.warn("------------------------------------------");
        log.warn("jwtAccessTokenExpiration: " + jwtAccessTokenExpiration);
        log.warn("------------------------------------------");
        log.warn("------------------------------------------");
        log.warn("remainingTimeInMillis: " + remainingTimeInMillis);
        log.warn("------------------------------------------");
        if (
            jwtRefreshTokenEmail != null &&
            isJwtRefreshTokenValid
        ) {
            response = loginCheckService.validationLoginCheck(jwtRefreshTokenEmail, response);
        }
        if (
            jwtAccessTokenEmail != null &&
            isJwtAccessTokenValid &&
            jwtRefreshTokenEmail != null &&
            isJwtRefreshTokenValid &&
            !jwtAccessTokenEmail.equals(jwtRefreshTokenEmail)
        ) {
            unauthorizedResponse.put("message", "jwtTokenUserEmailNotMatching");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedResponse);
        }
        if (
            !isJwtAccessTokenValid &&
            jwtRefreshTokenEmail != null &&
            isJwtRefreshTokenValid
        ) {
            final String jwtAccessToken = jwtGenerator.generateToken(jwtRefreshTokenEmail, 10 * 60 * 1000);
            ResponseCookie jwtAccessTokenCookie = ResponseCookie.from("jwtAccessToken", jwtAccessToken)
                .httpOnly(true) 
                .secure(true) 
                .path("/")
                .domain("opentofu.click")
                .maxAge(10 * 60)
                .sameSite("None") 
                .build();
            HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, jwtAccessTokenCookie.toString());
            log.warn("------------------------------------------");
            log.warn("jwtAccessToken has been successfully reissued");
            log.warn("------------------------------------------");
            
            response.put("remainingTimeInMillis", 10 * 60 * 1000 + 500);
            return ResponseEntity.ok().headers(headers).body(response);
        }
        log.warn("------------------------------------------");
        log.warn(jwtAccessTokenEmail);
        log.warn(jwtRefreshTokenEmail);
        log.warn(isJwtAccessTokenValid.toString());
        log.warn(isJwtRefreshTokenValid.toString());
        log.warn("------------------------------------------");
        return ResponseEntity.ok().body(response);
    }
}
