package com.ptit.tour.domain.contact.service;

import com.ptit.tour.domain.contact.dto.ContactMessageDto;
import com.ptit.tour.domain.contact.dto.CreateContactMessageRequest;
import com.ptit.tour.domain.contact.dto.UpdateContactStatusRequest;
import com.ptit.tour.domain.contact.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    ContactMessageDto create(CreateContactMessageRequest request);
    Page<ContactMessageDto> searchAdmin(ContactStatus status, String keyword, Pageable pageable);
    ContactMessageDto updateStatus(Long id, UpdateContactStatusRequest request);
}
