package com.team.cafe.Find;

import com.team.cafe.user.sjhy.SiteUser;
import com.team.cafe.user.sjhy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FindService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // 인증번호 저장
    private final Map<String, String> verificationCodes = new HashMap<>();

    // 아이디 찾기
    // 인증번호 생성 & 이메일 발송
    public void sendVerificationCode(String email) {
        // 사용자 존재 여부 확인
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("입력하신 정보와 일치하는 회원 정보가 없습니다. 다시 한 번 입력해주세요."));

        // 6자리 랜덤 코드 생성
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        verificationCodes.put(email, code);

        // MimeMessage를 사용하여 HTML 메일 발송 준비
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // 메일 작성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[카페 페르소나] 아이디 찾기 인증 코드");
        message.setText("인증 코드: " + code);

        mailSender.send(message);
    }

    // 인증번호 확인
    public String verifyCodeAndFindId(String email, String code) {
        String savedCode = verificationCodes.get(email);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }

        // 인증 성공 → 아이디 반환
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입된 이메일이 없습니다."));
        return user.getUsername();
    }



    // =======비밀번호 찾기========

    public void sendVerificationCodePW(String username, String email) {
        // 사용자 존재 여부 확인
        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("입력하신 아이디가 존재하지 않습니다."));
        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("아이디와 이메일이 일치하지 않습니다.");
        }

        // 6자리 랜덤 코드 생성
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        verificationCodes.put(email, code);

        // 메일 작성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[카페 페르소나] 비밀번호 찾기 인증 코드");
        message.setText("인증 코드: " + code);

        mailSender.send(message);
    }

    // 인증번호 확인
    public String verifyCodeAndFindPW(String email, String code) {
        String savedCode = verificationCodes.get(email);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }
        verificationCodes.remove(email);

        return userRepository.findByEmail(email).get().getUsername();
    }

    // 새 비밀번호 업데이트
    public void updatePassword(String username, String newPassword) {

        // 비밀번호 유효성 검사
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("비밀번호는 최소 8자 이상이고, 특수문자를 포함해야 합니다.");
        }

        SiteUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,}$";
        return password != null && password.matches(pattern);
    }
}