package com.ptit.tour.domain.wishlist.entity;

import com.ptit.tour.domain.shared.BaseEntity;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists",
    uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_user_tour", columnNames = {"user_id", "tour_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;
}
