package com.example.visitsubsystem.web;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.VisitStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Presentation-only helpers, invoked from Thymeleaf via {@code @beanName}.
 */
public final class DisplayFormats {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT).withZone(ZoneId.systemDefault());

    public DisplayFormats() {
    }

    public static String dateTime(Instant instant) {
        return instant == null ? "—" : DATE_TIME.format(instant);
    }

    public static String statusLabel(VisitStatus status) {
        return switch (status) {
            case PLANNED -> "Planned";
            case CHECKED_IN -> "Checked in";
            case CHECKED_OUT -> "Checked out";
            case REPORTED -> "Reported";
        };
    }

    public static String reasonLabel(AbnormalReason reason) {
        return switch (reason) {
            case SHORT_DURATION -> "Less than 5 minutes at the hospital";
            case FAR_FROM_HOSPITAL -> "More than 500 m from the hospital";
        };
    }

    public static String duration(Long seconds) {
        if (seconds == null) {
            return "—";
        }
        long minutes = seconds / 60;
        long remaining = seconds % 60;
        return minutes + " min " + remaining + " s";
    }
}
