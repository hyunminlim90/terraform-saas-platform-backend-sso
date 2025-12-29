package click.opentofu.sso.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginCheckService {

    private final UserRepository userRepository;

    public Map<String, Object> validationLoginCheck(
        String email,
        Map<String, Object> response
    ) {
        Optional<UserEntity> userEntity = userRepository.findById(email.split("@")[0]);
        if (userEntity.isPresent()) {
            UserEntity foundUser = userEntity.get();
            response.put("userIndex", String.valueOf(foundUser.getUserIndex()));
            response.put("email", foundUser.getEmailId());
            response.put("isEmail", foundUser.getEmailId().equals(email.split("@")[0]));
            response.put("isAdmin", foundUser.getIsAdmin());
            response.put("isEnabled", foundUser.getIsEnabled());
            response.put("dbTable", foundUser.getDbTable());
        } else {
            response.put("isEmail", false);
        }
        return response;
    }
}
