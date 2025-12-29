package click.opentofu.sso.util;

import java.util.Optional;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.springframework.stereotype.Component;

import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtGenerator {

    private KeyPair keyPair;
    private final UserRepository userRepository;

    public String generateToken(String email, long expireTime) {
        Optional<UserEntity> userEntity = userRepository.findById(email.split("@")[0]);
        UserEntity foundUser = userEntity.get();
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", foundUser.getUuid());
        claims.put("roles", new String[] {"read", "write", "execute"});
        return createToken(claims, foundUser.getEmailId(), expireTime);
    }

    private String createToken(Map<String, Object> claims, String subject, long expireTime) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expireTime))
            .signWith(getSigningKey(), SignatureAlgorithm.RS256)
            .compact();
    }

    @PostConstruct
    public void init() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
    }

    private PrivateKey getSigningKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getVerificationKey() {
        return keyPair.getPublic();
    }
}
