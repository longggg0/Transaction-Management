package com.example.transaction_management.service.impl;

import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.exceptions.BusinessException;
import com.example.transaction_management.service.OtpSenderService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrevoOtpSender implements OtpSenderService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Override
    public void send(String email, String code) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from, fromName);
            helper.setTo(email);
            helper.setSubject("Your verification code");

            helper.setText(
                    "Your verification code is: " + code +
                            "\n\nIt expires in 5 minutes." +
                            "\nIf you did not request this, please ignore this email."
            );

            mailSender.send(message);

            log.info("OTP email sent successfully to {}", email);

        } catch (Exception e) {

            log.error("Failed to send OTP email to {}", email, e);

            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}