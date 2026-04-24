package com.ptit.tour.domain.contact.service;

import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.contact.dto.ContactMessageDto;
import com.ptit.tour.domain.contact.dto.CreateContactMessageRequest;
import com.ptit.tour.domain.contact.dto.UpdateContactStatusRequest;
import com.ptit.tour.domain.contact.entity.ContactMessage;
import com.ptit.tour.domain.contact.enums.ContactStatus;
import com.ptit.tour.domain.contact.repository.ContactMessageRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ContactMessageDto create(CreateContactMessageRequest request) {
        ContactMessage contact = ContactMessage.builder()
            .name(request.name())
            .email(request.email())
            .phone(request.phone())
            .subject(request.subject())
            .message(request.message())
            .status(ContactStatus.NEW)
            .build();
        return ContactMessageDto.from(contactMessageRepository.save(contact));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactMessageDto> searchAdmin(ContactStatus status, String keyword, Pageable pageable) {
        return contactMessageRepository.searchAdmin(status, normalize(keyword), pageable).map(ContactMessageDto::from);
    }

    @Override
    @Transactional
    public ContactMessageDto updateStatus(Long id, UpdateContactStatusRequest request) {
        ContactMessage contact = contactMessageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ContactMessage", id));
        contact.setStatus(request.status());
        contact.setAdminNote(request.adminNote());
        contact.setAssignee(request.assigneeId() == null ? null : userRepository.findById(request.assigneeId())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.assigneeId())));
        return ContactMessageDto.from(contactMessageRepository.save(contact));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
