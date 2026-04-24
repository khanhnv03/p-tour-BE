package com.ptit.tour.domain.blog.entity;

import com.ptit.tour.domain.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blog_block_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogBlockImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_block_id", nullable = false)
    private BlogBlock blogBlock;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
