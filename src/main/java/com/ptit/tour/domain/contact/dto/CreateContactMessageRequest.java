package com.ptit.tour.domain.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContactMessageRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Email @Size(max = 255) String email,
    @Size(max = 30) String phone,
    @NotBlank @Size(max = 255) String subject,
    @NotBlank String message
) {}
