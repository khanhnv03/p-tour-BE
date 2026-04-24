package com.ptit.tour.domain.contact.dto;

import com.ptit.tour.domain.contact.entity.ContactMessage;
import com.ptit.tour.domain.contact.enums.ContactStatus;

import java.time.Instant;

public record ContactMessageDto(
    Long id,
    String name,
    String email,
    String phone,
    String subject,
    String message,
    ContactStatus status,
    Long assigneeId,
    String assigneeName,
    String adminNote,
    Instant createdAt
) {
    public static ContactMessageDto from(ContactMessage contact) {
        return new ContactMessageDto(
            contact.getId(), contact.getName(), contact.getEmail(), contact.getPhone(),
            contact.getSubject(), contact.getMessage(), contact.getStatus(),
            contact.getAssignee() != null ? contact.getAssignee().getId() : null,
            contact.getAssignee() != null ? contact.getAssignee().getFullName() : null,
            contact.getAdminNote(), contact.getCreatedAt()
        );
    }
}
