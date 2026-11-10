package co.com.nequi.franchise.mongo.adapter;

import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.model.product.gateways.ProductRepository;
import co.com.nequi.franchise.mongo.mapper.MongoMapper;
import co.com.nequi.franchise.mongo.repository.ProductDataRepository;
import co.com.nequi.franchise.mongo.resilience.MongoResilience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductDataRepository dataRepository;
    private final MongoResilience resilience;

    @Override
    public Mono<Product> save(Product product) {
        return resilience.protect(() -> dataRepository.save(MongoMapper.toDocument(product)))
                .map(MongoMapper::toDomain);
    }

    @Override
    public Mono<Product> findById(String id) {
        return resilience.protect(() -> dataRepository.findById(id))
                .map(MongoMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return resilience.protect(() -> dataRepository.deleteById(id));
    }

    @Override
    public Mono<Product> findTopStockByBranchId(String branchId) {
        return resilience.protect(() -> dataRepository.findFirstByBranchIdOrderByStockDesc(branchId))
                .map(MongoMapper::toDomain);
    }
}
