package click.opentofu.sso.controller;

import java.util.HashMap;
import java.util.Map;

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
@RequestMapping(path = "/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailAuth {

    private final SignUpService signUpService;

    @CrossOrigin(origins = {
        "https://studio.opentofu.click"
    })
    @PostMapping(path = "/send-validation-code")
    public ResponseEntity<Map<String, String>> sendValidationCode (
        @RequestBody User user
    ) {
        String validationCode = signUpService.sendValidationCode(user.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("validationCode", validationCode);
        return ResponseEntity.ok(response);
    }
}
