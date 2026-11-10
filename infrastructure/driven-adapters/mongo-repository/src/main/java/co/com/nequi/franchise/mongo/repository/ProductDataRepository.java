package co.com.nequi.franchise.mongo.repository;

import co.com.nequi.franchise.mongo.document.ProductDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductDataRepository extends ReactiveMongoRepository<ProductDocument, String> {

    Mono<ProductDocument> findFirstByBranchIdOrderByStockDesc(String branchId);
}
