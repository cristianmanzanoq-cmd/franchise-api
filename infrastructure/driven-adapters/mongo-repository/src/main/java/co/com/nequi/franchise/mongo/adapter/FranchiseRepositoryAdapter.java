package co.com.nequi.franchise.mongo.adapter;

import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.mongo.mapper.MongoMapper;
import co.com.nequi.franchise.mongo.repository.FranchiseDataRepository;
import co.com.nequi.franchise.mongo.resilience.MongoResilience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private final FranchiseDataRepository dataRepository;
    private final MongoResilience resilience;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return resilience.protect(() -> dataRepository.save(MongoMapper.toDocument(franchise)))
                .map(MongoMapper::toDomain);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return resilience.protect(() -> dataRepository.findById(id))
                .map(MongoMapper::toDomain);
    }
}
