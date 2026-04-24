package com.ptit.tour.domain.tour.repository;

import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TourDepartureRepository extends JpaRepository<TourDeparture, Long> {

    List<TourDeparture> findByTourIdAndStatusAndDepartureDateGreaterThanEqualOrderByDepartureDateAsc(
        Long tourId, DepartureStatus status, LocalDate from);

    List<TourDeparture> findByTourIdOrderByDepartureDateAsc(Long tourId);

    Optional<TourDeparture> findByIdAndTourId(Long id, Long tourId);
}
