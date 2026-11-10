package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateFranchiseRequest(
        @Schema(description = "Franchise name", example = "Nequi Stores") String name) {
}
