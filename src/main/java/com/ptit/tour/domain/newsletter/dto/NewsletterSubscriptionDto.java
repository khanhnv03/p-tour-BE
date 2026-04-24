package com.ptit.tour.domain.newsletter.dto;

import com.ptit.tour.domain.newsletter.entity.NewsletterSubscription;

import java.time.Instant;

public record NewsletterSubscriptionDto(
    Long id,
    String email,
    boolean active,
    Instant createdAt,
    Instant unsubscribedAt
) {
    public static NewsletterSubscriptionDto from(NewsletterSubscription subscription) {
        return new NewsletterSubscriptionDto(
            subscription.getId(), subscription.getEmail(), subscription.isActive(),
            subscription.getCreatedAt(), subscription.getUnsubscribedAt()
        );
    }
}
