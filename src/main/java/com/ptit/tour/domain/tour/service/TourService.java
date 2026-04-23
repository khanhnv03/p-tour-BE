package com.ptit.tour.domain.tour.service;

import com.ptit.tour.domain.tour.dto.SaveTourRequest;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TourService {
    Page<TourSummaryDto> search(String keyword, Long destinationId,
                                 LocalDate departureDate, Integer guestCount, Integer durationDays,
                                 TourDifficulty difficulty,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 BigDecimal minRating,
                                 Pageable pageable);

    TourDetailDto getById(Long id);

    TourDetailDto getBySlug(String slug);

    List<TourSummaryDto> getFeatured(int limit);

    List<TourSummaryDto> getPopular(int limit);

    TourDetailDto create(SaveTourRequest request);

    TourDetailDto update(Long id, SaveTourRequest request);

    void delete(Long id);

    Tour getEntityById(Long id);
}
