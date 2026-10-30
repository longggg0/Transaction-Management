package com.example.transaction_management.service.impl;

import com.example.transaction_management.common.errors.ErrorCode;
import com.example.transaction_management.common.exceptions.BusinessException;
import com.example.transaction_management.service.OtpSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrevoOtpSender implements OtpSenderService {

    @Value("${brevo.api-key}")
    private String apiKey;
    @Value("${app.mail.from}")
    private String from;
    @Value("${app.mail.from-name}")
    private String fromName;

    private final RestClient restClient = RestClient.create("https://api.brevo.com");

    @Override
    public void send(String email, String code) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", from, "name", fromName),
                "to", List.of(Map.of("email", email)),
                "subject", "Your verification code",
                "textContent", "Your verification code is: " + code
                        + "\n\nIt expires in 5 minutes. If you did not request this, ignore this email.");
        try {
            restClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Brevo failed to send OTP to {}", email, e);   // never log the code
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
