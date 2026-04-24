package com.ptit.tour.domain.booking.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.dto.CreateBookingRequest;
import com.ptit.tour.domain.booking.dto.TicketPdfDownload;
import com.ptit.tour.domain.booking.dto.TicketDto;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.enums.DealStatus;
import com.ptit.tour.domain.deal.enums.DisplayMode;
import com.ptit.tour.domain.deal.repository.DealRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourDepartureRepository;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import com.ptit.tour.util.BookingCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.text.Normalizer;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.05");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourDepartureRepository departureRepository;
    private final DealRepository dealRepository;
    private final TicketAccessTokenService ticketAccessTokenService;
    private final TicketPdfService ticketPdfService;

    @Override
    @Transactional
    public BookingDto create(Long userId, CreateBookingRequest req) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Tour tour = tourRepository.findById(req.tourId())
            .orElseThrow(() -> new ResourceNotFoundException("Tour", req.tourId()));
        if (tour.getStatus() != TourStatus.PUBLISHED) {
            throw new BusinessException("Tour này hiện không nhận đặt chỗ");
        }

        TourDeparture departure = departureRepository.findById(req.departureId())
            .orElseThrow(() -> new ResourceNotFoundException("Departure", req.departureId()));
        if (!departure.getTour().getId().equals(tour.getId())) {
            throw new BusinessException("Lịch khởi hành không thuộc tour này");
        }
        if (departure.getStatus() != DepartureStatus.OPEN || !departure.hasAvailableSlots()) {
            throw new BusinessException("Lịch khởi hành đã hết chỗ hoặc không còn nhận đặt");
        }
        if (req.guestCount() > tour.getMaxGuests()) {
            throw new BusinessException("Số lượng khách vượt quá giới hạn tối đa cho một booking (" + tour.getMaxGuests() + " khách)");
        }
        int remaining = departure.getAvailableSlots() - departure.getBookedSlots();
        if (req.guestCount() > remaining) {
            throw new BusinessException("Số lượng khách vượt quá chỗ còn lại (" + remaining + " chỗ)");
        }

        BigDecimal unitPrice = departure.effectivePrice(tour.getPricePerPerson());
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(req.guestCount()));
        BigDecimal taxAmount = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        // Resolve deal: promoCode (COPY_CODE) takes priority over dealId (AUTO_APPLY)
        Deal deal = null;
        if (req.promoCode() != null && !req.promoCode().isBlank()) {
            deal = dealRepository.findByPromoCode(req.promoCode().trim())
                .orElseThrow(() -> new BusinessException("Mã khuyến mãi không hợp lệ hoặc không tồn tại"));
            if (deal.getDisplayMode() != DisplayMode.COPY_CODE) {
                throw new BusinessException("Deal này không hỗ trợ nhập mã promo thủ công");
            }
            validateDeal(deal, subtotal);
        } else if (req.dealId() != null) {
            deal = dealRepository.findById(req.dealId())
                .orElseThrow(() -> new BusinessException("Deal không tồn tại"));
            if (deal.getDisplayMode() != DisplayMode.AUTO_APPLY) {
                throw new BusinessException("Deal này yêu cầu nhập mã promo để áp dụng");
            }
            validateDeal(deal, subtotal);
        }

        BigDecimal discountAmount = deal != null ? deal.computeDiscount(subtotal) : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount)
            .max(BigDecimal.ZERO);

        String bookingCode;
        do { bookingCode = BookingCodeGenerator.generate(); }
        while (bookingRepository.findByBookingCode(bookingCode).isPresent());

        String contactName = normalizeContactValue(req.contactName(), user.getFullName());
        String contactEmail = normalizeContactValue(req.contactEmail(), user.getEmail());
        String contactPhone = normalizeOptionalValue(req.contactPhone(), user.getPhone());

        Booking booking = Booking.builder()
            .bookingCode(bookingCode).user(user).tour(tour).departure(departure).deal(deal)
            .contactName(contactName).contactEmail(contactEmail).contactPhone(contactPhone)
            .guestCount(req.guestCount()).subtotal(subtotal).taxAmount(taxAmount)
            .discountAmount(discountAmount).totalAmount(totalAmount).notes(req.notes())
            .build();

        return BookingDto.from(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getById(Long id, Long requestingUserId) {
        Booking booking = getEntityById(id);
        if (!booking.getUser().getId().equals(requestingUserId)) {
            throw new BusinessException("Không có quyền xem booking này", HttpStatus.FORBIDDEN);
        }
        return BookingDto.from(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getByIdAdmin(Long id) {
        return BookingDto.from(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getMyBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(BookingDto::from);
    }

    @Override
    @Transactional
    public BookingDto cancel(Long id, Long requestingUserId) {
        Booking booking = getEntityById(id);
        if (!booking.getUser().getId().equals(requestingUserId)) {
            throw new BusinessException("Không có quyền huỷ booking này", HttpStatus.FORBIDDEN);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Chỉ có thể huỷ booking ở trạng thái chờ xác nhận");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return BookingDto.from(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicket(Long id, Long requestingUserId) {
        Booking booking = getEntityById(id);
        if (!booking.getUser().getId().equals(requestingUserId)) {
            throw new BusinessException("Không có quyền xem vé này", HttpStatus.FORBIDDEN);
        }
        assertTicketAvailable(booking);
        return buildTicketDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketPdfDownload getTicketPdf(Long id, Long requestingUserId, boolean adminAccess, String token) {
        Booking booking = getEntityById(id);
        assertTicketAccess(booking, requestingUserId, adminAccess, token);
        TicketDto ticket = buildTicketDto(booking);
        String fileName = "e-ticket-" + sanitizeFileName(booking.getBookingCode()) + ".pdf";
        return new TicketPdfDownload(fileName, ticketPdfService.generate(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> findAll(Long userId, BookingStatus status, Pageable pageable) {
        return bookingRepository.findAllFiltered(userId, status, pageable).map(BookingDto::from);
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = getEntityById(id);
        BookingStatus previousStatus = booking.getStatus();
        if (previousStatus == status) {
            return BookingDto.from(booking);
        }
        booking.setStatus(status);

        if (status == BookingStatus.CONFIRMED) {
            TourDeparture dep = booking.getDeparture();
            int remaining = dep.getAvailableSlots() - dep.getBookedSlots();
            if (booking.getGuestCount() > remaining) {
                throw new BusinessException("Lịch khởi hành không còn đủ chỗ", HttpStatus.CONFLICT);
            }
            dep.setBookedSlots(dep.getBookedSlots() + booking.getGuestCount());
            if (dep.getBookedSlots() >= dep.getAvailableSlots()) {
                dep.setStatus(DepartureStatus.FULL);
            }
            // Increment deal usage count on first confirmation
            if (booking.getDeal() != null) {
                Deal deal = booking.getDeal();
                deal.setUsageCount(deal.getUsageCount() + 1);
            }
        }
        if (status == BookingStatus.COMPLETED) {
            Tour t = booking.getTour();
            t.setBookingCount(t.getBookingCount() + 1);
        }
        if (status == BookingStatus.CANCELLED) {
            TourDeparture dep = booking.getDeparture();
            // Restore slots only if it was previously confirmed (slots were locked)
            if (previousStatus == BookingStatus.CONFIRMED || previousStatus == BookingStatus.COMPLETED) {
                int restored = Math.max(0, dep.getBookedSlots() - booking.getGuestCount());
                dep.setBookedSlots(restored);
                if (dep.getStatus() == DepartureStatus.FULL) {
                    dep.setStatus(DepartureStatus.OPEN);
                }
                // Decrement deal usage since this booking is being reversed
                if (booking.getDeal() != null) {
                    Deal deal = booking.getDeal();
                    deal.setUsageCount(Math.max(0, deal.getUsageCount() - 1));
                }
            }
        }
        return BookingDto.from(bookingRepository.save(booking));
    }

    @Override
    public Booking getEntityById(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    private void validateDeal(Deal deal, BigDecimal subtotal) {
        LocalDate today = LocalDate.now();
        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new BusinessException("Deal không còn hoạt động");
        }
        if (today.isBefore(deal.getValidFrom()) || today.isAfter(deal.getValidTo())) {
            throw new BusinessException("Deal đã hết hạn");
        }
        if (subtotal.compareTo(deal.getMinOrderValue()) < 0) {
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu " + deal.getMinOrderValue() + " VNĐ để áp dụng deal");
        }
        if (deal.getUsageLimit() != null && deal.getUsageCount() >= deal.getUsageLimit()) {
            throw new BusinessException("Deal đã hết lượt sử dụng", HttpStatus.GONE);
        }
    }

    private TicketDto buildTicketDto(Booking booking) {
        String token = ticketAccessTokenService.generate(booking);
        String downloadUrl = "/api/bookings/" + booking.getId() + "/ticket/pdf?token=" + token;
        String qrCodeData = "PTOUR-TICKET:" + booking.getBookingCode() + ":" + token;
        return TicketDto.from(booking, downloadUrl, qrCodeData);
    }

    private void assertTicketAccess(Booking booking, Long requestingUserId, boolean adminAccess, String token) {
        assertTicketAvailable(booking);
        if (adminAccess) {
            return;
        }
        if (requestingUserId != null && booking.getUser().getId().equals(requestingUserId)) {
            return;
        }
        if (token != null && ticketAccessTokenService.isValid(token, booking)) {
            return;
        }
        throw new BusinessException("Không có quyền xem vé này", HttpStatus.FORBIDDEN);
    }

    private void assertTicketAvailable(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("Vé chỉ khả dụng sau khi booking được xác nhận");
        }
    }

    private String normalizeContactValue(String requestedValue, String fallbackValue) {
        String value = normalizeOptionalValue(requestedValue, fallbackValue);
        if (value == null || value.isBlank()) {
            throw new BusinessException("Thiếu thông tin liên hệ bắt buộc");
        }
        return value;
    }

    private String normalizeOptionalValue(String requestedValue, String fallbackValue) {
        if (requestedValue != null && !requestedValue.isBlank()) {
            return requestedValue.trim();
        }
        if (fallbackValue != null && !fallbackValue.isBlank()) {
            return fallbackValue.trim();
        }
        return null;
    }

    private String sanitizeFileName(String input) {
        String ascii = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd')
            .replace('Đ', 'D');
        return ascii.replaceAll("[^A-Za-z0-9-]+", "-").replaceAll("-{2,}", "-");
    }
}
