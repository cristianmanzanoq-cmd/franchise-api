package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateProductRequest(
        @Schema(description = "Product name", example = "Coffee Bag 500g") String name,
        @Schema(description = "Initial stock", example = "100") Integer stock) {
}
