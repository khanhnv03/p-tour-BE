package com.ptit.tour.domain.booking.dto;

public record TicketPdfDownload(
    String fileName,
    byte[] content
) {}
