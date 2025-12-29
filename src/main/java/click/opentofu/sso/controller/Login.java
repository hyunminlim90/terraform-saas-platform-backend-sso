package click.opentofu.sso.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import click.opentofu.sso.dto.User;
import click.opentofu.sso.service.LoginService;
import click.opentofu.sso.util.JwtGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class Login {
    
    private final LoginService loginService;
    private final JwtGenerator jwtGenerator;

    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        },
        allowCredentials = "true"
    )
    @PostMapping(path = "/login")
    public ResponseEntity<Map<String, Object>> login (
        @RequestBody User user
    ) {
        Map<String, Object> response = loginService.validationLogin(user);
        if ((Boolean) response.get("isLoggedIn")) {
            final String jwtAccessToken = jwtGenerator.generateToken(user.getEmail(), 10 * 60 * 1000); 
            final String jwtRefreshToken = jwtGenerator.generateToken(user.getEmail(), 6 * 60 * 60 * 1000); 
            ResponseCookie jwtAccessTokenCookie = ResponseCookie.from("jwtAccessToken", jwtAccessToken)
                .httpOnly(true) 
                .secure(true) 
                .path("/") 
                .domain("opentofu.click")
                .maxAge(10 * 60)
                .sameSite("None")
                .build();
            ResponseCookie jwtRefreshTokenCookie = ResponseCookie.from("jwtRefreshToken", jwtRefreshToken)
                .httpOnly(true) 
                .secure(true)
                .path("/") 
                .domain("opentofu.click")
                .maxAge(6 * 60 * 60)
                .sameSite("None") 
                .build();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, jwtAccessTokenCookie.toString());
            headers.add(HttpHeaders.SET_COOKIE, jwtRefreshTokenCookie.toString());
            return ResponseEntity.ok().headers(headers).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
