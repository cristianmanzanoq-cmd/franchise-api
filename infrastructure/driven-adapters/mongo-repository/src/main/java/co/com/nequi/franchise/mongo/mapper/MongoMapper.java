package co.com.nequi.franchise.mongo.mapper;

import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.mongo.document.BranchDocument;
import co.com.nequi.franchise.mongo.document.FranchiseDocument;
import co.com.nequi.franchise.mongo.document.ProductDocument;

public final class MongoMapper {

    private MongoMapper() {
    }

    public static FranchiseDocument toDocument(Franchise franchise) {
        return FranchiseDocument.builder()
                .id(franchise.getId())
                .name(franchise.getName())
                .build();
    }

    public static Franchise toDomain(FranchiseDocument document) {
        return Franchise.builder()
                .id(document.getId())
                .name(document.getName())
                .build();
    }

    public static BranchDocument toDocument(Branch branch) {
        return BranchDocument.builder()
                .id(branch.getId())
                .franchiseId(branch.getFranchiseId())
                .name(branch.getName())
                .build();
    }

    public static Branch toDomain(BranchDocument document) {
        return Branch.builder()
                .id(document.getId())
                .franchiseId(document.getFranchiseId())
                .name(document.getName())
                .build();
    }

    public static ProductDocument toDocument(Product product) {
        return ProductDocument.builder()
                .id(product.getId())
                .branchId(product.getBranchId())
                .name(product.getName())
                .stock(product.getStock())
                .build();
    }

    public static Product toDomain(ProductDocument document) {
        return Product.builder()
                .id(document.getId())
                .branchId(document.getBranchId())
                .name(document.getName())
                .stock(document.getStock())
                .build();
    }
}
