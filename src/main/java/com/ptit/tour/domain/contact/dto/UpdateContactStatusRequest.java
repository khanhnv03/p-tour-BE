package com.ptit.tour.domain.contact.dto;

import com.ptit.tour.domain.contact.enums.ContactStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateContactStatusRequest(
    @NotNull ContactStatus status,
    Long assigneeId,
    String adminNote
) {}
