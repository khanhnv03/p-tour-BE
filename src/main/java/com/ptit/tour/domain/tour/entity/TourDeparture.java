package com.ptit.tour.domain.tour.entity;

import com.ptit.tour.domain.shared.BaseEntity;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_departures", indexes = {
    @Index(name = "idx_departures_date", columnList = "departure_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDeparture extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "available_slots", nullable = false)
    private int availableSlots;

    @Column(name = "booked_slots", nullable = false)
    @Builder.Default
    private int bookedSlots = 0;

    @Column(name = "price_override", precision = 15, scale = 2)
    private BigDecimal priceOverride;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DepartureStatus status = DepartureStatus.OPEN;

    public BigDecimal effectivePrice(BigDecimal tourBasePrice) {
        return priceOverride != null ? priceOverride : tourBasePrice;
    }

    public boolean hasAvailableSlots() {
        return bookedSlots < availableSlots;
    }
}
