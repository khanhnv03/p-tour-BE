package com.ptit.tour.domain.newsletter.service;

import com.ptit.tour.domain.newsletter.dto.NewsletterSubscriptionDto;
import com.ptit.tour.domain.newsletter.dto.NewsletterSubscriptionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsletterService {
    NewsletterSubscriptionDto subscribe(NewsletterSubscriptionRequest request);
    Page<NewsletterSubscriptionDto> list(Pageable pageable);
}
