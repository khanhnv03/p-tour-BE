package com.ptit.tour.domain.contact.entity;

import com.ptit.tour.domain.contact.enums.ContactStatus;
import com.ptit.tour.domain.shared.BaseEntity;
import com.ptit.tour.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "contact_messages", indexes = {
    @Index(name = "idx_contact_status", columnList = "status"),
    @Index(name = "idx_contact_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContactStatus status = ContactStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
}
