package click.opentofu.sso.controller;

import java.security.PublicKey;
import java.util.Base64;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import click.opentofu.sso.util.JwtGenerator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class JwtPublicKeyProvider {

    private final JwtGenerator jwtGenerator;
    
    @CrossOrigin(origins = {
        "https://java-vpc.opentofu.click"
    }) 
    @GetMapping("/public-key")
    public String getPublicKey() {
        PublicKey publicKey = jwtGenerator.getVerificationKey();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
