package com.example.visitsubsystem.web;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.VisitStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Presentation-only helpers, invoked from Thymeleaf via {@code @beanName}.
 *
 * <p>All timestamps are rendered in the configured app zone ({@code visit.time-zone}),
 * defaulting to UTC+8.</p>
 */
public final class DisplayFormats {

    private final DateTimeFormatter dateTime;

    public DisplayFormats(ZoneId zone) {
        this.dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT).withZone(zone);
    }

    public String dateTime(Instant instant) {
        return instant == null ? "—" : dateTime.format(instant);
    }

    public String statusLabel(VisitStatus status) {
        return switch (status) {
            case PLANNED -> "Planned";
            case CHECKED_IN -> "Checked in";
            case CHECKED_OUT -> "Checked out";
            case REPORTED -> "Reported";
        };
    }

    public String reasonLabel(AbnormalReason reason) {
        return switch (reason) {
            case SHORT_DURATION -> "Less than 5 minutes at the hospital";
            case FAR_FROM_HOSPITAL -> "More than 500 m from the hospital";
        };
    }

    public String duration(Long seconds) {
        if (seconds == null) {
            return "—";
        }
        long minutes = seconds / 60;
        long remaining = seconds % 60;
        return minutes + " min " + remaining + " s";
    }
}
