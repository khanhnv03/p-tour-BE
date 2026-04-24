package com.ptit.tour.domain.media.dto;

import com.ptit.tour.domain.media.entity.MediaAsset;

public record MediaAssetDto(
    Long id,
    String url,
    String alt,
    String contentType,
    long size,
    String originalFilename
) {
    public static MediaAssetDto from(MediaAsset asset) {
        return new MediaAssetDto(asset.getId(), asset.getUrl(), asset.getAltText(),
            asset.getContentType(), asset.getSize(), asset.getOriginalFilename());
    }
}
