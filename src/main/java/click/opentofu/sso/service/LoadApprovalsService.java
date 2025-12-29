package click.opentofu.sso.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import click.opentofu.sso.dto.User;
import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoadApprovalsService {
    
    private final UserRepository userRepository;

    public Map<String, Object> loadApprovals (User user) {
        Optional<UserEntity> userEntity = userRepository.findById(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        if (userEntity.isPresent()) {
            UserEntity foundUser = userEntity.get();
            response.put("email", foundUser.getEmailId());
            response.put("isEmail", foundUser.getEmailId().equals(user.getEmail()));
            response.put("isAdmin", foundUser.getIsAdmin());
            response.put("isEnabled", foundUser.getIsEnabled());
            response.put("dbTable", foundUser.getDbTable());
        } else {
            response.put("isEmail", false);
        }
        return response;
    }

    public List<Map<String, Object>> loadAll () {
        List<UserEntity> userEntities = userRepository.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            Map<String, Object> response = new HashMap<>();
            response.put("email", userEntity.getEmailId());
            response.put("isAdmin", userEntity.getIsAdmin());
            response.put("isEnabled", userEntity.getIsEnabled());
            response.put("dbTable", userEntity.getDbTable());
            responseList.add(response);
        }
        return responseList;
    }

    public Map<String, Object> approve (User user) {
        Optional<UserEntity> userEntity = userRepository.findById(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        if (userEntity.isPresent()) {
            UserEntity foundUser = userEntity.get();
            foundUser.setIsEnabled(true);
            userRepository.save(foundUser);
            response.put("result", "Successfully approved the sign-up.");
        } else {
            response.put("result", "Sign-up approval failed.");
        }
        return response;
    }
}
