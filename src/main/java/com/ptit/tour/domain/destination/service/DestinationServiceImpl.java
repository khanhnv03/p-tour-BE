package com.ptit.tour.domain.destination.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.destination.dto.DestinationDto;
import com.ptit.tour.domain.destination.dto.SaveDestinationRequest;
import com.ptit.tour.domain.destination.entity.Destination;
import com.ptit.tour.domain.destination.repository.DestinationRepository;
import com.ptit.tour.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;

    @Override
    public List<DestinationDto> getFeatured() {
        return destinationRepository.findByFeaturedTrueOrderByTourCountDesc()
            .stream().map(DestinationDto::from).toList();
    }

    @Override
    public Page<DestinationDto> findAll(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return destinationRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(DestinationDto::from);
        }
        return destinationRepository.findAll(pageable).map(DestinationDto::from);
    }

    @Override
    public DestinationDto getById(Long id) {
        return DestinationDto.from(getEntityById(id));
    }

    @Override
    public DestinationDto getBySlug(String slug) {
        return destinationRepository.findBySlug(slug)
            .map(DestinationDto::from)
            .orElseThrow(() -> new ResourceNotFoundException("Destination", "slug", slug));
    }

    @Override
    @Transactional
    public DestinationDto create(SaveDestinationRequest request) {
        String slug = SlugUtils.toSlug(request.name());
        if (destinationRepository.existsBySlug(slug)) {
            throw new BusinessException("Tên điểm đến đã tồn tại");
        }
        Destination destination = Destination.builder()
            .name(request.name())
            .slug(slug)
            .description(request.description())
            .coverImageUrl(request.coverImageUrl())
            .country(request.country() != null ? request.country() : "Việt Nam")
            .region(request.region())
            .featured(request.featured())
            .build();
        return DestinationDto.from(destinationRepository.save(destination));
    }

    @Override
    @Transactional
    public DestinationDto update(Long id, SaveDestinationRequest request) {
        Destination destination = getEntityById(id);
        destination.setName(request.name());
        destination.setDescription(request.description());
        destination.setCoverImageUrl(request.coverImageUrl());
        if (request.country() != null) destination.setCountry(request.country());
        destination.setRegion(request.region());
        destination.setFeatured(request.featured());
        return DestinationDto.from(destinationRepository.save(destination));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        destinationRepository.delete(getEntityById(id));
    }

    @Override
    public Destination getEntityById(Long id) {
        return destinationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Destination", id));
    }
}
