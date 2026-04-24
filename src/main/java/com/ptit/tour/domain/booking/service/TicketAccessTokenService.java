package com.ptit.tour.domain.booking.service;

import com.ptit.tour.domain.booking.entity.Booking;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TicketAccessTokenService {

    private static final String TOKEN_PURPOSE = "booking-ticket";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.ticket.expiration-ms:604800000}")
    private long ticketExpirationMs;

    public String generate(Booking booking) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject("ticket-download")
            .claim("purpose", TOKEN_PURPOSE)
            .claim("bookingId", booking.getId())
            .claim("bookingCode", booking.getBookingCode())
            .claim("userId", booking.getUser().getId())
            .issuedAt(new Date(now))
            .expiration(new Date(now + ticketExpirationMs))
            .signWith(signingKey())
            .compact();
    }

    public boolean isValid(String token, Booking booking) {
        try {
            var claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return TOKEN_PURPOSE.equals(claims.get("purpose", String.class))
                && booking.getId().equals(claims.get("bookingId", Long.class))
                && booking.getUser().getId().equals(claims.get("userId", Long.class))
                && booking.getBookingCode().equals(claims.get("bookingCode", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
