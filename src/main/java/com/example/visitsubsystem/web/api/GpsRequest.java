package com.example.visitsubsystem.web.api;

import jakarta.validation.constraints.NotNull;

public record GpsRequest(@NotNull Double lat, @NotNull Double lng) {
}
