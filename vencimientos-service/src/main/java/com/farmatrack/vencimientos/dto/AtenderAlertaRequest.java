package com.farmatrack.vencimientos.dto;

import jakarta.validation.constraints.NotNull;

public record AtenderAlertaRequest(@NotNull Boolean atendida) {
}
