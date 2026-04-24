package com.ptit.tour.domain.media.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.media.dto.MediaAssetDto;
import com.ptit.tour.domain.media.entity.MediaAsset;
import com.ptit.tour.domain.media.repository.MediaAssetRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;

    @Value("${app.media.upload-dir:uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public MediaAssetDto upload(Long uploadedByUserId, MultipartFile file, String alt) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File upload không được để trống");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("Chỉ hỗ trợ upload ảnh JPEG, PNG, WEBP hoặc GIF");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
        String extension = extensionOf(original);
        String stored = UUID.randomUUID() + extension;
        Path target = rootDir().resolve(stored).normalize();

        try {
            Files.createDirectories(rootDir());
            file.transferTo(target);
        } catch (IOException ex) {
            throw new BusinessException("Không thể lưu file upload");
        }

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/media/")
            .path(stored)
            .toUriString();

        MediaAsset asset = MediaAsset.builder()
            .originalFilename(original)
            .storedFilename(stored)
            .url(url)
            .altText(alt)
            .contentType(contentType)
            .size(file.getSize())
            .uploadedBy(userRepository.findById(uploadedByUserId).orElse(null))
            .build();
        return MediaAssetDto.from(mediaAssetRepository.save(asset));
    }

    @Override
    public Resource load(String storedFilename) {
        MediaAsset asset = mediaAssetRepository.findByStoredFilename(storedFilename)
            .orElseThrow(() -> new ResourceNotFoundException("MediaAsset", "storedFilename", storedFilename));
        try {
            Resource resource = new UrlResource(rootDir().resolve(asset.getStoredFilename()).normalize().toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("MediaAsset", "storedFilename", storedFilename);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("MediaAsset", "storedFilename", storedFilename);
        }
    }

    private Path rootDir() {
        return Path.of(uploadDir).toAbsolutePath().normalize();
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return filename.substring(dot).toLowerCase();
    }
}
