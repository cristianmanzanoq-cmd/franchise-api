package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductResponse(
        @Schema(example = "886f1a2b3c4d5e6f7a8b9c0f") String id,
        @Schema(example = "775f1a2b3c4d5e6f7a8b9c0e") String branchId,
        @Schema(example = "Coffee Bag 500g") String name,
        @Schema(example = "100") Integer stock) {
}
