package com.ptit.tour.common.mail;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public interface EmailService {
    void sendPasswordResetEmail(String recipientEmail, String recipientName, String resetLink, Instant expiresAt);
    void sendBookingConfirmationEmail(
        String recipientEmail,
        String recipientName,
        String orderCode,
        String bookingCode,
        String tourTitle,
        LocalDate departureDate,
        int guestCount,
        BigDecimal totalAmount
    );
    void sendErrorAlert(String subject, String body);
}
