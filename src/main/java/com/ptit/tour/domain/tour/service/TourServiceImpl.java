package com.ptit.tour.domain.tour.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.destination.service.DestinationService;
import com.ptit.tour.domain.tour.dto.SaveTourDepartureRequest;
import com.ptit.tour.domain.tour.dto.SaveTourRequest;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.entity.*;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourDepartureRepository;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final TourDepartureRepository departureRepository;
    private final DestinationService destinationService;
    private final BookingRepository bookingRepository;

    @Override
    public Page<TourSummaryDto> search(String keyword, Long destinationId,
                                        LocalDate departureDate, Integer guestCount, Integer durationDays,
                                        TourDifficulty difficulty,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        BigDecimal minRating,
                                        Pageable pageable) {
        return tourRepository.search(keyword, destinationId, departureDate, guestCount, durationDays,
                difficulty, minPrice, maxPrice, minRating, pageable)
            .map(TourSummaryDto::from);
    }

    @Override
    public TourDetailDto getById(Long id) {
        return TourDetailDto.from(tourRepository.findByIdAndStatus(id, TourStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFoundException("Tour", id)));
    }

    @Override
    public TourDetailDto getBySlug(String slug) {
        return tourRepository.findBySlugAndStatus(slug, TourStatus.PUBLISHED)
            .map(TourDetailDto::from)
            .orElseThrow(() -> new ResourceNotFoundException("Tour", "slug", slug));
    }

    @Override
    public TourDetailDto getAdminById(Long id) {
        return TourDetailDto.from(getEntityById(id));
    }

    @Override
    public List<TourSummaryDto> getFeatured(int limit) {
        return tourRepository.findFeatured(PageRequest.of(0, limit)).stream()
            .map(TourSummaryDto::from).toList();
    }

    @Override
    public List<TourSummaryDto> getPopular(int limit) {
        return tourRepository.findPopular(PageRequest.of(0, limit)).stream()
            .map(TourSummaryDto::from).toList();
    }

    @Override
    public Page<TourSummaryDto> searchAdmin(String keyword, Long destinationId, TourStatus status,
                                            LocalDate departureDate, Integer availableSlots, Pageable pageable) {
        return tourRepository.searchAdmin(keyword, destinationId, status, departureDate, availableSlots, pageable)
            .map(TourSummaryDto::from);
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
        Tour tour = getEntityById(id);
        if (bookingRepository.existsByTourId(id)) {
            tour.setStatus(TourStatus.ARCHIVED);
            tourRepository.save(tour);
            return;
        }
        tourRepository.delete(tour);
    }

    @Override
    @Transactional
    public TourDetailDto createDeparture(Long tourId, SaveTourDepartureRequest request) {
        Tour tour = getEntityById(tourId);
        validateDeparture(request);
        TourDeparture departure = TourDeparture.builder()
            .tour(tour)
            .departureDate(request.departureDate())
            .availableSlots(request.availableSlots())
            .bookedSlots(request.bookedSlots())
            .priceOverride(request.priceOverride())
            .status(request.status())
            .build();
        tour.getDepartures().add(departure);
        return TourDetailDto.from(tourRepository.save(tour));
    }

    @Override
    @Transactional
    public TourDetailDto updateDeparture(Long tourId, Long departureId, SaveTourDepartureRequest request) {
        validateDeparture(request);
        TourDeparture departure = departureRepository.findByIdAndTourId(departureId, tourId)
            .orElseThrow(() -> new ResourceNotFoundException("TourDeparture", departureId));
        departure.setDepartureDate(request.departureDate());
        departure.setAvailableSlots(request.availableSlots());
        departure.setBookedSlots(request.bookedSlots());
        departure.setPriceOverride(request.priceOverride());
        departure.setStatus(request.status());
        departureRepository.save(departure);
        return TourDetailDto.from(getEntityById(tourId));
    }

    @Override
    @Transactional
    public void deleteDeparture(Long tourId, Long departureId) {
        TourDeparture departure = departureRepository.findByIdAndTourId(departureId, tourId)
            .orElseThrow(() -> new ResourceNotFoundException("TourDeparture", departureId));
        if (departure.getBookedSlots() > 0) {
            departure.setStatus(com.ptit.tour.domain.tour.enums.DepartureStatus.CANCELLED);
            departureRepository.save(departure);
            return;
        }
        departureRepository.delete(departure);
    }

    @Override
    public Tour getEntityById(Long id) {
        return tourRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tour", id));
    }

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
                            .itineraryDay(day).activityTime(a.activityTime()).title(a.title())
                            .description(a.description()).sortOrder(a.sortOrder()).build()
                    ));
                }
                return day;
            }).toList();
            tour.getItineraryDays().addAll(days);
        }
    }

    private void validateDeparture(SaveTourDepartureRequest request) {
        if (request.bookedSlots() > request.availableSlots()) {
            throw new BusinessException("Số chỗ đã đặt không được lớn hơn số chỗ mở bán");
        }
    }
}
