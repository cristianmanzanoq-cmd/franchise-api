package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ErrorResponse(
        @Schema(example = "FRANCHISE_NOT_FOUND") String code,
        @Schema(example = "Franchise not found") String message,
        Instant timestamp) {
}
