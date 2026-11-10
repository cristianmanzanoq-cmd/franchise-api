package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FranchiseResponse(
        @Schema(example = "664f1a2b3c4d5e6f7a8b9c0d") String id,
        @Schema(example = "Nequi Stores") String name) {
}
