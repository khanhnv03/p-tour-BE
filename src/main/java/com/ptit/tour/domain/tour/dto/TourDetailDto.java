package com.ptit.tour.domain.tour.dto;

import com.ptit.tour.domain.destination.dto.DestinationDto;
import com.ptit.tour.domain.tour.entity.*;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.enums.InclusionType;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TourDetailDto(
    Long id,
    String title,
    String slug,
    String description,
    String coverImageUrl,
    DestinationDto destination,
    int durationDays,
    int durationNights,
    int maxGuests,
    TourDifficulty difficulty,
    BigDecimal pricePerPerson,
    TourStatus status,
    BigDecimal rating,
    int reviewCount,
    int bookingCount,
    List<GalleryImageDto> galleryImages,
    List<HighlightDto> highlights,
    List<InclusionDto> inclusions,
    List<ItineraryDayDto> itineraryDays,
    List<DepartureDto> departures
) {
    public static TourDetailDto from(Tour t) {
        return new TourDetailDto(
            t.getId(), t.getTitle(), t.getSlug(), t.getDescription(), t.getCoverImageUrl(),
            DestinationDto.from(t.getDestination()),
            t.getDurationDays(), t.getDurationNights(), t.getMaxGuests(),
            t.getDifficulty(), t.getPricePerPerson(), t.getStatus(),
            t.getRating(), t.getReviewCount(), t.getBookingCount(),
            t.getGalleryImages().stream().map(GalleryImageDto::from).toList(),
            t.getHighlights().stream().map(HighlightDto::from).toList(),
            t.getInclusions().stream().map(InclusionDto::from).toList(),
            t.getItineraryDays().stream().map(ItineraryDayDto::from).toList(),
            t.getDepartures().stream().map(DepartureDto::from).toList()
        );
    }

    public record GalleryImageDto(Long id, String imageUrl, int sortOrder) {
        public static GalleryImageDto from(TourGalleryImage g) {
            return new GalleryImageDto(g.getId(), g.getImageUrl(), g.getSortOrder());
        }
    }

    public record HighlightDto(Long id, String icon, String label, int sortOrder) {
        public static HighlightDto from(TourHighlight h) {
            return new HighlightDto(h.getId(), h.getIcon(), h.getLabel(), h.getSortOrder());
        }
    }

    public record InclusionDto(Long id, InclusionType type, String description, int sortOrder) {
        public static InclusionDto from(TourInclusion i) {
            return new InclusionDto(i.getId(), i.getType(), i.getDescription(), i.getSortOrder());
        }
    }

    public record ItineraryDayDto(Long id, int dayNumber, String title, String summary,
                                   String coverImageUrl, List<ActivityDto> activities) {
        public static ItineraryDayDto from(ItineraryDay d) {
            return new ItineraryDayDto(d.getId(), d.getDayNumber(), d.getTitle(), d.getSummary(),
                d.getCoverImageUrl(),
                d.getActivities().stream().map(ActivityDto::from).toList());
        }
    }

    public record ActivityDto(Long id, LocalTime activityTime, String title, String description, int sortOrder) {
        public static ActivityDto from(ItineraryActivity a) {
            return new ActivityDto(a.getId(), a.getActivityTime(), a.getTitle(), a.getDescription(), a.getSortOrder());
        }
    }

    public record DepartureDto(Long id, LocalDate departureDate, int availableSlots,
                                int bookedSlots, BigDecimal priceOverride, DepartureStatus status) {
        public static DepartureDto from(TourDeparture d) {
            return new DepartureDto(d.getId(), d.getDepartureDate(), d.getAvailableSlots(),
                d.getBookedSlots(), d.getPriceOverride(), d.getStatus());
        }
    }
}
