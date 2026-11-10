package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateStockRequest(
        @Schema(description = "New stock value", example = "42") Integer stock) {
}
