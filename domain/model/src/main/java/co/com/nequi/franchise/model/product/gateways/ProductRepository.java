package co.com.nequi.franchise.model.product.gateways;

import co.com.nequi.franchise.model.product.Product;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Mono<Void> deleteById(String id);

    Mono<Product> findTopStockByBranchId(String branchId);
}
