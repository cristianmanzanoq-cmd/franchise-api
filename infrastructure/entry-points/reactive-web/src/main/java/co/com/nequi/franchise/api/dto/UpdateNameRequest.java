package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNameRequest(
        @Schema(description = "New name", example = "Renamed entity") String name) {
}
