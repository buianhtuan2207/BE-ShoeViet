package com.example.be.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Mã xác thực tài khoản của bạn");

        String content = "<p>Chào bạn,</p>"
                + "<p>Mã xác thực (OTP) để kích hoạt tài khoản của bạn là:</p>"
                + "<h2><b>" + otpCode + "</b></h2>"
                + "<p>Mã này sẽ hết hạn sau 15 phút.</p>";

        helper.setText(content, true);
        mailSender.send(message);
    }
}
