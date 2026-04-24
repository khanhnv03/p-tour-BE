package com.ptit.tour.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "booking_alerts", nullable = false)
    private boolean bookingAlerts = true;

    @Builder.Default
    @Column(name = "editorial_comments", nullable = false)
    private boolean editorialComments = true;

    @Builder.Default
    @Column(name = "system_status", nullable = false)
    private boolean systemStatus = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public static UserNotificationPreferences defaultFor(User user) {
        return UserNotificationPreferences.builder()
                .user(user)
                .bookingAlerts(true)
                .editorialComments(true)
                .systemStatus(false)
                .build();
    }
}
