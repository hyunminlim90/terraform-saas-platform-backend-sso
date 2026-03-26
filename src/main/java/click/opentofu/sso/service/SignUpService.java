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
        Optional<UserEntity> userEmail = userRepository.findByEmailId(user.getEmail());
        Map<String, Object> response = new HashMap<>();
        if (userEmail.isEmpty()) {
            response.put("isEmail", false);
            saveUser(user);
            response.put("isRegistered",true);
            String email = user.getEmail();
            String subject = "Sign-up request is received, from the opentofu.";
            String text = ""
                + email + "님,\n\n"
                + "안녕하세요.\n\n"
                + "OpenTofu 사이트 이용을 위해 아래 신청서를 작성해 주세요.\n"
                + "작성 완료 후 승인된 사용자에 한해 사이트 이용이 가능합니다.\n\n"
                + "👉 신청서 링크:\n"
                + "https://forms.gle/kbzNo53bYnz2NNxx6\n\n"
                + "[작성 시 주의사항]\n"
                + "- 반드시 OpenTofu 가입 이메일과 동일하게 입력해 주세요.\n"
                + "- 다를 경우 승인되지 않을 수 있습니다.\n\n"
                + "감사합니다.";
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
            .emailId(user.getEmail())
            .isEnabled(false)
            .isAdmin(false)
            .dbTable(uuid + "_" + user.getEmail())
            .build();
        userRepository.save(userEntity);
    }
}
