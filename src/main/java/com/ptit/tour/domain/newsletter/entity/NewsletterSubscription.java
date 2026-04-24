package com.ptit.tour.domain.newsletter.entity;

import com.ptit.tour.domain.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "newsletter_subscriptions", indexes = {
    @Index(name = "idx_newsletter_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscription extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "unsubscribed_at")
    private Instant unsubscribedAt;
}
