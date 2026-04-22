package com.ptit.tour.domain.tour.service;

import com.ptit.tour.domain.tour.dto.SaveTourRequest;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TourService {
    Page<TourSummaryDto> search(String keyword, Long destinationId,
                                 TourDifficulty difficulty,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 Pageable pageable);

    TourDetailDto getById(Long id);

    TourDetailDto getBySlug(String slug);

    TourDetailDto create(SaveTourRequest request);

    TourDetailDto update(Long id, SaveTourRequest request);

    void delete(Long id);

    Tour getEntityById(Long id);
}
