package com.ptit.tour.domain.newsletter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsletterSubscriptionRequest(
    @NotBlank @Email @Size(max = 255) String email
) {}
