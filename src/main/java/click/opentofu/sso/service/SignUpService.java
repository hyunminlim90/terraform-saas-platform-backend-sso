package click.opentofu.sso.service;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import click.opentofu.sso.dto.User;
import click.opentofu.sso.entity.UserEntity;
import click.opentofu.sso.repository.UserRepository;

import java.security.SecureRandom;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final SecureRandom random = new SecureRandom();
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String sendValidationCode (String email) {
        String validationCode = generateValidationCode();
        String subject = "OpenTofu Studio - Email Verification Code";
        String text = "Hello,\n\n"
            + "Thank you for signing up with OpenTofu!\n\n"
            + "Please use the verification code below to confirm your email address:\n\n"
            + "🔐 Verification Code: " + validationCode + "\n\n"
            + "This code will expire in 3 minutes.\n"
            + "If you didn't request this, feel free to ignore this email.\n\n"
            + "Thanks,\n"
            + "The OpenTofu Team";
        sendSimpleMessage(email, subject, text);
        return validationCode;
    }

    private String generateValidationCode() {
        return IntStream.range(0, 6)
            .mapToObj((index) -> {
                return String.valueOf(random.nextInt(10));
            })
            .collect(Collectors.joining());
    }

    private void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("noreply.opentofu@gmail.com");
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(text);
        emailSender.send(mail);
    }

    public Map<String, Object> memberRegistration(User user) {
        Optional<UserEntity> userEmail = userRepository.findByEmailId(user.getEmail().split("@")[0]);
        Map<String, Object> response = new HashMap<>();
        if (userEmail.isEmpty()) {
            response.put("isEmail", false);
            saveUser(user);
            response.put("isRegistered",true);
            String email = user.getEmail();
            String subject = "Sign-up request is received, from the opentofu.";
            String text = "Requester: " + email;
            sendSimpleMessage("hyunmin.lim.90@icloud.com", subject, text);
        } else if (userEmail.isPresent()) {
            response.put("isEmail", true);
            response.put("isRegistered",false);
        } else if (userEmail.isEmpty()) {
            response.put("isEmail", false);
            response.put("isRegistered",false);
        } else {
            response.put("isEmail", true);
            response.put("isRegistered",false);
        }
        return response;
    }

    private void saveUser(User user) {
        String uuid = UUID.randomUUID().toString();
        Integer nextIndex = userRepository.findMaxUserIndex().orElse(0) + 1;
        UserEntity userEntity = UserEntity.builder()
            .uuid(uuid)
            .userIndex(nextIndex)
            .password(passwordEncoder.encode(user.getPassword()))
            .emailId(user.getEmail().split("@")[0])
            .isEnabled(false)
            .isAdmin(false)
            .dbTable(uuid + "_" + user.getEmail().split("@")[0])
            .build();
        userRepository.save(userEntity);
    }
}
