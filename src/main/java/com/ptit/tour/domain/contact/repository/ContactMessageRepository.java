package com.ptit.tour.domain.contact.repository;

import com.ptit.tour.domain.contact.entity.ContactMessage;
import com.ptit.tour.domain.contact.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    long countByStatus(ContactStatus status);

    @Query("""
        SELECT c FROM ContactMessage c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:keyword IS NULL
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.subject) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<ContactMessage> searchAdmin(@Param("status") ContactStatus status,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);
}
