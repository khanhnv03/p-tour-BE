package com.ptit.tour.domain.tour.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.destination.service.DestinationService;
import com.ptit.tour.domain.tour.dto.SaveTourRequest;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.entity.*;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final DestinationService destinationService;

    @Override
    public Page<TourSummaryDto> search(String keyword, Long destinationId,
                                        TourDifficulty difficulty,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        Pageable pageable) {
        return tourRepository.search(keyword, destinationId, difficulty, minPrice, maxPrice, pageable)
            .map(TourSummaryDto::from);
    }

    @Override
    public TourDetailDto getById(Long id) {
        return TourDetailDto.from(getEntityById(id));
    }

    @Override
    public TourDetailDto getBySlug(String slug) {
        return tourRepository.findBySlug(slug)
            .map(TourDetailDto::from)
            .orElseThrow(() -> new ResourceNotFoundException("Tour", "slug", slug));
    }

    @Override
    @Transactional
    public TourDetailDto create(SaveTourRequest req) {
        String slug = SlugUtils.toSlug(req.title());
        if (tourRepository.existsBySlug(slug)) {
            throw new BusinessException("Tên tour đã tồn tại, vui lòng đổi tên");
        }
        Tour tour = Tour.builder()
            .destination(destinationService.getEntityById(req.destinationId()))
            .title(req.title())
            .slug(slug)
            .description(req.description())
            .durationDays(req.durationDays())
            .durationNights(req.durationNights())
            .maxGuests(req.maxGuests())
            .difficulty(req.difficulty())
            .pricePerPerson(req.pricePerPerson())
            .coverImageUrl(req.coverImageUrl())
            .status(req.status())
            .build();

        applySubEntities(tour, req);
        return TourDetailDto.from(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public TourDetailDto update(Long id, SaveTourRequest req) {
        Tour tour = getEntityById(id);
        tour.setDestination(destinationService.getEntityById(req.destinationId()));
        tour.setTitle(req.title());
        tour.setDescription(req.description());
        tour.setDurationDays(req.durationDays());
        tour.setDurationNights(req.durationNights());
        tour.setMaxGuests(req.maxGuests());
        tour.setDifficulty(req.difficulty());
        tour.setPricePerPerson(req.pricePerPerson());
        tour.setCoverImageUrl(req.coverImageUrl());
        tour.setStatus(req.status());

        tour.getGalleryImages().clear();
        tour.getHighlights().clear();
        tour.getInclusions().clear();
        tour.getItineraryDays().clear();
        applySubEntities(tour, req);
        return TourDetailDto.from(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        tourRepository.delete(getEntityById(id));
    }

    @Override
    public Tour getEntityById(Long id) {
        return tourRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tour", id));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void applySubEntities(Tour tour, SaveTourRequest req) {
        if (req.galleryImages() != null) {
            List<TourGalleryImage> images = req.galleryImages().stream()
                .map(g -> TourGalleryImage.builder()
                    .tour(tour).imageUrl(g.imageUrl()).sortOrder(g.sortOrder()).build())
                .toList();
            tour.getGalleryImages().addAll(images);
        }
        if (req.highlights() != null) {
            List<TourHighlight> highlights = req.highlights().stream()
                .map(h -> TourHighlight.builder()
                    .tour(tour).icon(h.icon()).label(h.label()).sortOrder(h.sortOrder()).build())
                .toList();
            tour.getHighlights().addAll(highlights);
        }
        if (req.inclusions() != null) {
            List<TourInclusion> inclusions = req.inclusions().stream()
                .map(i -> TourInclusion.builder()
                    .tour(tour).type(i.type()).description(i.description()).sortOrder(i.sortOrder()).build())
                .toList();
            tour.getInclusions().addAll(inclusions);
        }
        if (req.itineraryDays() != null) {
            List<ItineraryDay> days = req.itineraryDays().stream().map(d -> {
                ItineraryDay day = ItineraryDay.builder()
                    .tour(tour).dayNumber(d.dayNumber()).title(d.title())
                    .summary(d.summary()).coverImageUrl(d.coverImageUrl()).build();
                if (d.activities() != null) {
                    d.activities().forEach(a -> day.getActivities().add(
                        ItineraryActivity.builder()
                            .itineraryDay(day).activityTime(a.activityTime())
                            .description(a.description()).sortOrder(a.sortOrder()).build()
                    ));
                }
                return day;
            }).toList();
            tour.getItineraryDays().addAll(days);
        }
    }
}
