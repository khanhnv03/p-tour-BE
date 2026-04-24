package com.ptit.tour.domain.media.repository;

import com.ptit.tour.domain.media.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByStoredFilename(String storedFilename);
}
