package com.ssgpack.ssgfc.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[SSGFC] 임시 비밀번호 안내");
        message.setText("임시 비밀번호는 다음과 같습니다: " + tempPassword + "\n로그인 후 비밀번호를 꼭 변경해주세요.");
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[SSGFC] 이메일 인증 코드");
        message.setText("이메일 인증을 위한 코드입니다: " + code + "\n해당 코드를 입력창에 입력해주세요.");
        message.setFrom(fromEmail);
        mailSender.send(message);
    }
}
