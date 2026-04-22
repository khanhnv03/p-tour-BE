package com.ptit.tour.domain.tour.entity;

import com.ptit.tour.domain.destination.entity.Destination;
import com.ptit.tour.domain.shared.BaseEntity;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours", indexes = {
    @Index(name = "idx_tours_destination", columnList = "destination_id"),
    @Index(name = "idx_tours_status", columnList = "status"),
    @Index(name = "idx_tours_price", columnList = "price_per_person")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(unique = true, length = 500)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "duration_nights", nullable = false)
    private int durationNights;

    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TourDifficulty difficulty;

    @Column(name = "price_per_person", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerPerson;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TourStatus status = TourStatus.DRAFT;

    @Column(nullable = false, precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    @Column(name = "booking_count", nullable = false)
    @Builder.Default
    private int bookingCount = 0;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<TourGalleryImage> galleryImages = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<TourHighlight> highlights = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<TourInclusion> inclusions = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<ItineraryDay> itineraryDays = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("departureDate ASC")
    @Builder.Default
    private List<TourDeparture> departures = new ArrayList<>();
}
