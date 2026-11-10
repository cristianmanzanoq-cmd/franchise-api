package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BranchResponse(
        @Schema(example = "775f1a2b3c4d5e6f7a8b9c0e") String id,
        @Schema(example = "664f1a2b3c4d5e6f7a8b9c0d") String franchiseId,
        @Schema(example = "Medellin Branch") String name) {
}
