package com.ptit.tour.domain.newsletter.service;

import com.ptit.tour.domain.newsletter.dto.NewsletterSubscriptionDto;
import com.ptit.tour.domain.newsletter.dto.NewsletterSubscriptionRequest;
import com.ptit.tour.domain.newsletter.entity.NewsletterSubscription;
import com.ptit.tour.domain.newsletter.repository.NewsletterSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Override
    @Transactional
    public NewsletterSubscriptionDto subscribe(NewsletterSubscriptionRequest request) {
        String email = request.email().trim().toLowerCase();
        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> NewsletterSubscription.builder().email(email).build());
        subscription.setActive(true);
        subscription.setUnsubscribedAt(null);
        return NewsletterSubscriptionDto.from(newsletterSubscriptionRepository.save(subscription));
    }

    @Override
    public Page<NewsletterSubscriptionDto> list(Pageable pageable) {
        return newsletterSubscriptionRepository.findAll(pageable).map(NewsletterSubscriptionDto::from);
    }
}
