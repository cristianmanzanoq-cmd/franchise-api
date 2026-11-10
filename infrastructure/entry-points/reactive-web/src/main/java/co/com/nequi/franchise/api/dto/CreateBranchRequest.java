package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateBranchRequest(
        @Schema(description = "Branch name", example = "Medellin Branch") String name) {
}
