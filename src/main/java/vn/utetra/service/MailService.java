package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void send(String to, String subject, String content) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);
        if (from != null && !from.isBlank()) msg.setFrom(from);
        mailSender.send(msg);
    }

    public void sendOtp(String to, String code) {
        String subject = "UTeTra - Mã xác thực OTP";
        String body = "Xin chào!\n\n"
                + "Mã OTP của bạn là: " + code + "\n"
                + "Mã có hiệu lực trong 5 phút.\n\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\n"
                + "— UTeTra Team";
        send(to, subject, body);
    }
}