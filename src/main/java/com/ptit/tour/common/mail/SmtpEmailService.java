package com.ptit.tour.common.mail;

import com.ptit.tour.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${app.alerts.error-email-to:}")
    private String errorAlertEmailTo;

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink, Instant expiresAt) {
        ensureMailConfigured();

        String subject = "[PTour] Dat lai mat khau";
        String body = String.join("\n",
            "Xin chao " + safeName(recipientName) + ",",
            "",
            "Chung toi da nhan duoc yeu cau dat lai mat khau cho tai khoan PTour cua ban.",
            "Truy cap lien ket ben duoi de dat lai mat khau:",
            resetLink,
            "",
            "Lien ket nay se het han vao: " + DATE_TIME_FORMATTER.format(expiresAt),
            "Neu ban khong thuc hien yeu cau nay, ban co the bo qua email nay.",
            "",
            "PTour"
        );

        sendPlainText(recipientEmail, subject, body, true);
    }

    @Override
    public void sendBookingConfirmationEmail(
        String recipientEmail,
        String recipientName,
        String orderCode,
        String bookingCode,
        String tourTitle,
        LocalDate departureDate,
        int guestCount,
        BigDecimal totalAmount
    ) {
        if (!isMailConfigured()) {
            log.warn("Skip booking confirmation email because mail is not configured");
            return;
        }

        String subject = "[PTour] Xac nhan dat tour thanh cong";
        String body = String.join("\n",
            "Xin chao " + safeName(recipientName) + ",",
            "",
            "Ho so xac nhan da duoc tao thanh cong cho don dat tour cua ban.",
            "Ma don hang: " + orderCode,
            "Ma dat cho: " + bookingCode,
            "Hanh trinh: " + tourTitle,
            "Ngay khoi hanh: " + formatDate(departureDate),
            "So khach: " + guestCount,
            "Tong thanh toan: " + formatCurrency(totalAmount) + " VND",
            "",
            "Ban co the dang nhap PTour de xem chi tiet booking va e-ticket.",
            "",
            "PTour"
        );

        sendPlainText(recipientEmail, subject, body, false);
    }

    @Override
    public void sendErrorAlert(String subject, String body) {
        if (!isMailConfigured() || errorAlertEmailTo == null || errorAlertEmailTo.isBlank()) {
            return;
        }

        sendPlainText(errorAlertEmailTo, subject, body, false);
    }

    private void sendPlainText(String to, String subject, String body, boolean failOnError) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            mailSender.send(message);
        } catch (MailException ex) {
            if (failOnError) {
                throw new BusinessException("Khong the gui email luc nay, vui long thu lai sau", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            log.error("Failed to send alert email to {}", to, ex);
        }
    }

    private void ensureMailConfigured() {
        if (!isMailConfigured()) {
            throw new BusinessException("Cau hinh email chua san sang", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isMailConfigured() {
        return mailHost != null && !mailHost.isBlank()
            && mailUsername != null && !mailUsername.isBlank();
    }

    private String safeName(String recipientName) {
        return recipientName == null || recipientName.isBlank() ? "ban" : recipientName;
    }

    private String formatDate(LocalDate departureDate) {
        return departureDate == null ? "Dang cap nhat" : DATE_FORMATTER.format(departureDate);
    }

    private String formatCurrency(BigDecimal amount) {
        return amount == null ? "0" : CURRENCY_FORMAT.format(amount);
    }
}
