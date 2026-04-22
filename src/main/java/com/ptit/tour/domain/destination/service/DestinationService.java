package com.ptit.tour.domain.destination.service;

import com.ptit.tour.domain.destination.dto.DestinationDto;
import com.ptit.tour.domain.destination.dto.SaveDestinationRequest;
import com.ptit.tour.domain.destination.entity.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DestinationService {
    List<DestinationDto> getFeatured();
    Page<DestinationDto> findAll(String keyword, Pageable pageable);
    DestinationDto getById(Long id);
    DestinationDto getBySlug(String slug);
    DestinationDto create(SaveDestinationRequest request);
    DestinationDto update(Long id, SaveDestinationRequest request);
    void delete(Long id);
    Destination getEntityById(Long id);
}
