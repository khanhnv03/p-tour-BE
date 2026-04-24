package com.ptit.tour.domain.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.destination.entity.Destination;
import com.ptit.tour.domain.destination.repository.DestinationRepository;
import com.ptit.tour.domain.order.repository.OrderRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourDepartureRepository;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class Phase3FlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourDepartureRepository departureRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User customer;
    private User admin;
    private Tour tour;
    private TourDeparture departure;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        bookingRepository.deleteAll();
        departureRepository.deleteAll();
        tourRepository.deleteAll();
        destinationRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
            .email("admin@test.local")
            .passwordHash("admin")
            .fullName("Admin Test")
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .build());

        customer = userRepository.save(User.builder()
            .email("alex@test.local")
            .passwordHash("customer")
            .fullName("Alex PTIT")
            .phone("+84 912 345 678")
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build());

        Destination destination = destinationRepository.save(Destination.builder()
            .name("Đà Lạt")
            .slug("da-lat-it")
            .description("Integration destination")
            .featured(true)
            .build());

        tour = tourRepository.save(Tour.builder()
            .destination(destination)
            .title("Bình minh trên đỉnh Langbiang")
            .slug("binh-minh-tren-dinh-langbiang-it")
            .description("Integration tour")
            .durationDays(3)
            .durationNights(2)
            .maxGuests(10)
            .difficulty(TourDifficulty.MEDIUM)
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .coverImageUrl("https://example.com/langbiang.jpg")
            .status(TourStatus.PUBLISHED)
            .rating(BigDecimal.valueOf(4.8))
            .reviewCount(12)
            .build());

        departure = departureRepository.save(TourDeparture.builder()
            .tour(tour)
            .departureDate(LocalDate.of(2026, 5, 20))
            .availableSlots(12)
            .bookedSlots(0)
            .status(DepartureStatus.OPEN)
            .build());
    }

    @Test
    @WithAnonymousUser
    void checkoutFlowShouldSupportMockPaymentTicketPdfAndAdminViews() throws Exception {
        String createBookingPayload = """
            {
              "tourId": %d,
              "departureId": %d,
              "guestCount": 2,
              "contactName": "Alex PTIT",
              "contactEmail": "alex.booking@test.local",
              "contactPhone": "+84 912 345 678",
              "notes": "Mang theo do leo nui"
            }
            """.formatted(tour.getId(), departure.getId());

        MvcResult bookingResult = mockMvc.perform(post("/bookings")
                .with(authenticationToken(customer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBookingPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.contactEmail").value("alex.booking@test.local"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andReturn();

        JsonNode bookingJson = objectMapper.readTree(bookingResult.getResponse().getContentAsString());
        long bookingId = bookingJson.path("data").path("id").asLong();
        String bookingCode = bookingJson.path("data").path("bookingCode").asText();

        String createOrderPayload = """
            {
              "bookingId": %d,
              "paymentMethod": "VNPAY"
            }
            """.formatted(bookingId);

        MvcResult orderResult = mockMvc.perform(post("/orders")
                .with(authenticationToken(customer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.bookingId").value(bookingId))
            .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"))
            .andReturn();

        JsonNode orderJson = objectMapper.readTree(orderResult.getResponse().getContentAsString());
        long orderId = orderJson.path("data").path("id").asLong();

        mockMvc.perform(post("/orders/{id}/mock-pay", orderId)
                .with(authenticationToken(customer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("PAID"));

        mockMvc.perform(get("/bookings/my")
                .with(authenticationToken(customer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].bookingCode").value(bookingCode))
            .andExpect(jsonPath("$.data.content[0].contactPhone").value("+84 912 345 678"));

        MvcResult ticketResult = mockMvc.perform(get("/bookings/{id}/ticket", bookingId)
                .with(authenticationToken(customer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.downloadUrl").exists())
            .andExpect(jsonPath("$.data.qrCodeData").exists())
            .andReturn();

        JsonNode ticketJson = objectMapper.readTree(ticketResult.getResponse().getContentAsString());
        String downloadUrl = ticketJson.path("data").path("downloadUrl").asText().replaceFirst("^/api", "");

        byte[] pdfBytes = mockMvc.perform(get(downloadUrl))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        assertThat(new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");

        mockMvc.perform(get("/admin/bookings/{id}", bookingId)
                .with(authenticationToken(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingCode").value(bookingCode))
            .andExpect(jsonPath("$.data.contactEmail").value("alex.booking@test.local"));

        mockMvc.perform(get("/admin/orders/{id}", orderId)
                .with(authenticationToken(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.bookingCode").value(bookingCode))
            .andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor authenticationToken(User user) {
        UserPrincipal principal = UserPrincipal.of(user);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
