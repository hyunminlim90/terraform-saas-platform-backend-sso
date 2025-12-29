package click.opentofu.sso.service;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import click.opentofu.sso.dto.User;
import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> validationLogin(User user) {
        Optional<UserEntity> userEntity = userRepository.findById(user.getEmail().split("@")[0]);
        Map<String, Object> response = new HashMap<>();
        if (userEntity.isPresent()) {
            UserEntity foundUser = userEntity.get();
            response.put("userIndex", String.valueOf(foundUser.getUserIndex()));
            response.put("email", foundUser.getEmailId());
            response.put("isEmail", foundUser.getEmailId().equals(user.getEmail().split("@")[0]));
            response.put("isPassword", passwordEncoder.matches(user.getPassword(), foundUser.getPassword()));
            response.put("isAdmin", foundUser.getIsAdmin());
            response.put("isEnabled", foundUser.getIsEnabled());
        } else {
            response.put("isEmail", false);
            response.put("isPassword", false); 
        }
        if ((boolean) response.get("isEmail") && (boolean) response.get("isPassword")) {
            response.put("isLoggedIn", true);
        } else {
            response.put("isLoggedIn", false);
        }
        return response;
    }
}
