package com.ptit.tour.domain.tour.service;

import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.destination.entity.Destination;
import com.ptit.tour.domain.destination.service.DestinationService;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourServiceImplTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private DestinationService destinationService;

    @InjectMocks
    private TourServiceImpl tourService;

    @Test
    void searchShouldForwardPhase2FiltersAndMapTourSummary() {
        LocalDate departureDate = LocalDate.of(2026, 5, 10);
        PageRequest pageable = PageRequest.of(0, 9);
        Tour tour = buildPublishedTour(1L, "binh-minh-tren-dinh-langbiang", "Bình minh trên đỉnh Langbiang");
        Page<Tour> page = new PageImpl<>(List.of(tour), pageable, 1);

        when(tourRepository.search(
            "langbiang", 1L, departureDate, 4, 3,
            TourDifficulty.MEDIUM,
            BigDecimal.valueOf(2_000_000L), BigDecimal.valueOf(5_000_000L),
            BigDecimal.valueOf(4), pageable
        )).thenReturn(page);

        Page<TourSummaryDto> result = tourService.search(
            "langbiang", 1L, departureDate, 4, 3,
            TourDifficulty.MEDIUM,
            BigDecimal.valueOf(2_000_000L), BigDecimal.valueOf(5_000_000L),
            BigDecimal.valueOf(4), pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).destinationName()).isEqualTo("Đà Lạt");
        verify(tourRepository).search(
            "langbiang", 1L, departureDate, 4, 3,
            TourDifficulty.MEDIUM,
            BigDecimal.valueOf(2_000_000L), BigDecimal.valueOf(5_000_000L),
            BigDecimal.valueOf(4), pageable
        );
    }

    @Test
    void getByIdShouldRejectNonPublishedTour() {
        when(tourRepository.findByIdAndStatus(99L, TourStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourService.getById(99L));
    }

    @Test
    void getBySlugShouldReturnPublishedTourDetail() {
        Tour tour = buildPublishedTour(2L, "du-thuyen-ha-long-2n1d", "Du thuyền Hạ Long 2 ngày 1 đêm");
        when(tourRepository.findBySlugAndStatus("du-thuyen-ha-long-2n1d", TourStatus.PUBLISHED))
            .thenReturn(Optional.of(tour));

        TourDetailDto dto = tourService.getBySlug("du-thuyen-ha-long-2n1d");

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.slug()).isEqualTo("du-thuyen-ha-long-2n1d");
        assertThat(dto.destination().name()).isEqualTo("Đà Lạt");
    }

    private Tour buildPublishedTour(Long id, String slug, String title) {
        Destination destination = Destination.builder()
            .name("Đà Lạt")
            .slug("da-lat")
            .country("Việt Nam")
            .build();
        destination.setId(1L);

        Tour tour = Tour.builder()
            .destination(destination)
            .title(title)
            .slug(slug)
            .description("Mô tả")
            .durationDays(3)
            .durationNights(2)
            .maxGuests(10)
            .difficulty(TourDifficulty.MEDIUM)
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .coverImageUrl("https://example.com/tour.jpg")
            .status(TourStatus.PUBLISHED)
            .rating(BigDecimal.valueOf(4.8))
            .reviewCount(12)
            .bookingCount(20)
            .build();
        tour.setId(id);
        return tour;
    }
}
