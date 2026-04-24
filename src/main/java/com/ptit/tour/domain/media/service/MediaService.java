package com.ptit.tour.domain.media.service;

import com.ptit.tour.domain.media.dto.MediaAssetDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    MediaAssetDto upload(Long uploadedByUserId, MultipartFile file, String alt);
    Resource load(String storedFilename);
}
