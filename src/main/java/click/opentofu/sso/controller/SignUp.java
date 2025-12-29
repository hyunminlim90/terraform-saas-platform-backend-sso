package click.opentofu.sso.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import click.opentofu.sso.dto.User;
import click.opentofu.sso.service.SignUpService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class SignUp {

    private final SignUpService signUpService;
    
    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        }
    )
    @PostMapping(path = "/sign-up")
    public ResponseEntity<Map<String, Object>> signUp (
        @RequestBody User user
    ) {
        Map<String, Object> response = signUpService.memberRegistration(user);
        if ((Boolean) response.get("isRegistered")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
}
