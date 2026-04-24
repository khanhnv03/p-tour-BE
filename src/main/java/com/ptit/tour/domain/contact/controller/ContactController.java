package com.ptit.tour.domain.contact.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.contact.dto.ContactMessageDto;
import com.ptit.tour.domain.contact.dto.CreateContactMessageRequest;
import com.ptit.tour.domain.contact.dto.UpdateContactStatusRequest;
import com.ptit.tour.domain.contact.enums.ContactStatus;
import com.ptit.tour.domain.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contacts")
@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @Operation(summary = "Gửi liên hệ")
    @PostMapping("/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ContactMessageDto> create(@Valid @RequestBody CreateContactMessageRequest request) {
        return ApiResponse.created(contactService.create(request));
    }

    @Operation(summary = "[Admin] Danh sách liên hệ")
    @GetMapping("/admin/contacts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ContactMessageDto>> list(
        @RequestParam(required = false) ContactStatus status,
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(contactService.searchAdmin(status, keyword, pageable)));
    }

    @Operation(summary = "[Admin] Cập nhật trạng thái liên hệ")
    @PatchMapping("/admin/contacts/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContactMessageDto> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateContactStatusRequest request) {
        return ApiResponse.ok(contactService.updateStatus(id, request));
    }
}
