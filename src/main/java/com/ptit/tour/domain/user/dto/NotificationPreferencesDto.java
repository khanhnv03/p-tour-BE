package com.ptit.tour.domain.user.dto;

import com.ptit.tour.domain.user.entity.UserNotificationPreferences;

public record NotificationPreferencesDto(
        boolean bookingAlerts,
        boolean editorialComments,
        boolean systemStatus
) {
    public static NotificationPreferencesDto from(UserNotificationPreferences prefs) {
        return new NotificationPreferencesDto(
                prefs.isBookingAlerts(),
                prefs.isEditorialComments(),
                prefs.isSystemStatus()
        );
    }
}
