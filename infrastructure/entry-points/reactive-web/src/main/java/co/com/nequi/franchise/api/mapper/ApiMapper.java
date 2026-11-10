package co.com.nequi.franchise.api.mapper;

import co.com.nequi.franchise.api.dto.BranchResponse;
import co.com.nequi.franchise.api.dto.FranchiseResponse;
import co.com.nequi.franchise.api.dto.ProductResponse;
import co.com.nequi.franchise.api.dto.TopStockProductResponse;
import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.model.product.TopStockProduct;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(franchise.getId(), franchise.getName());
    }

    public static BranchResponse toResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getFranchiseId(), branch.getName());
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getBranchId(),
                product.getName(), product.getStock());
    }

    public static TopStockProductResponse toResponse(TopStockProduct topStockProduct) {
        return new TopStockProductResponse(
                topStockProduct.getBranch().getId(),
                topStockProduct.getBranch().getName(),
                toResponse(topStockProduct.getProduct()));
    }
}
