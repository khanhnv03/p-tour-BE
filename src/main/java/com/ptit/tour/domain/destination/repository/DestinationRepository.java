package com.ptit.tour.domain.destination.repository;

import com.ptit.tour.domain.destination.entity.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    Optional<Destination> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Destination> findByFeaturedTrueOrderByTourCountDesc();
    Page<Destination> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
