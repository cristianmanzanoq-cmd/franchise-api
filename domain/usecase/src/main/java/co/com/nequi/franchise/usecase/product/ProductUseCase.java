package co.com.nequi.franchise.usecase.product;

import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.model.product.TopStockProduct;
import co.com.nequi.franchise.model.product.gateways.ProductRepository;
import co.com.nequi.franchise.model.branch.gateways.BranchRepository;
import co.com.nequi.franchise.model.validation.DomainValidator;
import co.com.nequi.franchise.usecase.branch.BranchUseCase;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProductUseCase {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final BranchUseCase branchUseCase;
    private final FranchiseUseCase franchiseUseCase;

    public Mono<Product> addProduct(String franchiseId, String branchId, String name, Integer stock) {
        return DomainValidator.requireValidName(name)
                .zipWith(DomainValidator.requireValidStock(stock))
                .flatMap(nameAndStock -> branchUseCase.findBranch(franchiseId, branchId)
                        .map(branch -> Product.builder()
                                .branchId(branch.getId())
                                .name(nameAndStock.getT1())
                                .stock(nameAndStock.getT2())
                                .build()))
                .flatMap(productRepository::save);
    }

    public Mono<Void> removeProduct(String franchiseId, String branchId, String productId) {
        return findProduct(franchiseId, branchId, productId)
                .flatMap(product -> productRepository.deleteById(product.getId()));
    }

    public Mono<Product> updateStock(String franchiseId, String branchId, String productId, Integer stock) {
        return DomainValidator.requireValidStock(stock)
                .flatMap(validStock -> findProduct(franchiseId, branchId, productId)
                        .map(product -> product.toBuilder().stock(validStock).build()))
                .flatMap(productRepository::save);
    }

    public Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String name) {
        return DomainValidator.requireValidName(name)
                .flatMap(validName -> findProduct(franchiseId, branchId, productId)
                        .map(product -> product.toBuilder().name(validName).build()))
                .flatMap(productRepository::save);
    }

    public Flux<TopStockProduct> findTopStockProductsByFranchise(String franchiseId) {
        return franchiseUseCase.findFranchise(franchiseId)
                .flatMapMany(franchise -> branchRepository.findByFranchiseId(franchise.getId()))
                .flatMap(branch -> productRepository.findTopStockByBranchId(branch.getId())
                        .map(product -> TopStockProduct.builder()
                                .branch(branch)
                                .product(product)
                                .build()));
    }

    private Mono<Product> findProduct(String franchiseId, String branchId, String productId) {
        return branchUseCase.findBranch(franchiseId, branchId)
                .flatMap(branch -> productRepository.findById(productId)
                        .filter(product -> product.getBranchId().equals(branch.getId())))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.PRODUCT_NOT_FOUND)));
    }
}
