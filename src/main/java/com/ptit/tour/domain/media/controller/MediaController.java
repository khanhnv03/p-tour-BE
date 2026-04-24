package com.ptit.tour.domain.media.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.media.dto.MediaAssetDto;
import com.ptit.tour.domain.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Tag(name = "Media")
@RestController
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "[Admin] Upload ảnh")
    @PostMapping(value = {"/admin/media", "/media/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MediaAssetDto> upload(@AuthenticationPrincipal UserPrincipal principal,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestParam(required = false) String alt) {
        return ApiResponse.created(mediaService.upload(principal.getId(), file, alt));
    }

    @Operation(summary = "Đọc media đã upload")
    @GetMapping("/media/{storedFilename:.+}")
    public ResponseEntity<Resource> read(@PathVariable String storedFilename) {
        Resource resource = mediaService.load(storedFilename);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
}
