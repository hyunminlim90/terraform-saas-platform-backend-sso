package click.opentofu.sso.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
public class Logout {

    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        },
        allowCredentials = "true"
    )
    @PostMapping(path = "/logout")
    public ResponseEntity<Map<String, Object>> loginCheck () {
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        ResponseCookie jwtAccessTokenCookie = ResponseCookie.from("jwtAccessToken", "")
            .httpOnly(true) 
            .secure(true) 
            .path("/") 
            .domain("opentofu.click")
            .maxAge(0)
            .sameSite("None")
            .build();

        ResponseCookie jwtRefreshTokenCookie = ResponseCookie.from("jwtRefreshToken", "")
            .httpOnly(true) 
            .secure(true) 
            .path("/") 
            .domain("opentofu.click")
            .maxAge(0)
            .sameSite("None")
            .build();

        headers.add(HttpHeaders.SET_COOKIE, jwtAccessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, jwtRefreshTokenCookie.toString());

        log.warn("------------------------------------------");
        log.warn("jwtAccessToken & jwtRefreshToken have been successfully deleted.");
        log.warn("------------------------------------------");

        response.put("remainingTimeInMillis", 0);
        return ResponseEntity.ok().headers(headers).body(response);
    }
}
