package com.ptit.tour.domain.tour.entity;

import com.ptit.tour.domain.shared.BaseEntity;
import com.ptit.tour.domain.tour.enums.InclusionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_inclusions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourInclusion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InclusionType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
