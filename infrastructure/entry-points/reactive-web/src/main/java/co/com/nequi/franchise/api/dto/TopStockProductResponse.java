package co.com.nequi.franchise.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TopStockProductResponse(
        @Schema(example = "775f1a2b3c4d5e6f7a8b9c0e") String branchId,
        @Schema(example = "Medellin Branch") String branchName,
        ProductResponse product) {
}
