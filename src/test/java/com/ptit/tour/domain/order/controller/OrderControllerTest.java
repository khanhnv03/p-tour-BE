package com.ptit.tour.domain.order.controller;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.order.dto.OrderDto;
import com.ptit.tour.domain.order.enums.PaymentMethod;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Environment environment;

    @Test
    void confirmShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = OrderController.class.getMethod("confirm", Long.class, String.class, String.class);

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    void mockPayShouldRejectOutsideDevProfile() {
        OrderController controller = new OrderController(orderService, environment);
        UserPrincipal principal = buildPrincipal(7L);

        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        assertThrows(BusinessException.class, () -> controller.mockPay(15L, principal, null));
        verifyNoInteractions(orderService);
    }

    @Test
    void mockPayShouldConfirmOwnedOrderInDevProfile() {
        OrderController controller = new OrderController(orderService, environment);
        UserPrincipal principal = buildPrincipal(7L);
        OrderDto dto = buildOrderDto();

        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(orderService.getById(15L, 7L)).thenReturn(dto);
        when(orderService.confirmPayment(eq(15L), any(String.class), eq("mock-pay-15"))).thenReturn(dto);

        ApiResponse<OrderDto> response = controller.mockPay(15L, principal, "mock-pay-15");

        assertThat(response.getData()).isEqualTo(dto);
        verify(orderService).getById(15L, 7L);

        ArgumentCaptor<String> refCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderService).confirmPayment(eq(15L), refCaptor.capture(), eq("mock-pay-15"));
        assertThat(refCaptor.getValue()).isEqualTo("MOCK-mock-pay-15");
    }

    private UserPrincipal buildPrincipal(Long userId) {
        var user = com.ptit.tour.domain.user.entity.User.builder()
            .email("alex@ptittour.com")
            .passwordHash("$2a$10$examplehash")
            .fullName("Alex PTIT")
            .role(com.ptit.tour.domain.user.enums.UserRole.CUSTOMER)
            .status(com.ptit.tour.domain.user.enums.UserStatus.ACTIVE)
            .build();
        user.setId(userId);
        return UserPrincipal.of(user);
    }

    private OrderDto buildOrderDto() {
        return new OrderDto(
            15L,
            "ORD-20260001",
            31L,
            "BK-1934",
            7L,
            "Alex PTIT",
            "Bình minh trên đỉnh Langbiang",
            "https://example.com/langbiang.jpg",
            LocalDate.of(2026, 5, 20),
            2,
            BigDecimal.valueOf(7_329_000L),
            PaymentMethod.VNPAY,
            PaymentStatus.PAID,
            "MOCK-123456",
            null,
            Instant.parse("2026-04-24T00:00:00Z"),
            null,
            Instant.parse("2026-04-24T00:00:00Z")
        );
    }
}
