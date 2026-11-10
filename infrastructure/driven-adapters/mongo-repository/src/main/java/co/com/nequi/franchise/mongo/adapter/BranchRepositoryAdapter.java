package co.com.nequi.franchise.mongo.adapter;

import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.branch.gateways.BranchRepository;
import co.com.nequi.franchise.mongo.mapper.MongoMapper;
import co.com.nequi.franchise.mongo.repository.BranchDataRepository;
import co.com.nequi.franchise.mongo.resilience.MongoResilience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class BranchRepositoryAdapter implements BranchRepository {

    private final BranchDataRepository dataRepository;
    private final MongoResilience resilience;

    @Override
    public Mono<Branch> save(Branch branch) {
        return resilience.protect(() -> dataRepository.save(MongoMapper.toDocument(branch)))
                .map(MongoMapper::toDomain);
    }

    @Override
    public Mono<Branch> findById(String id) {
        return resilience.protect(() -> dataRepository.findById(id))
                .map(MongoMapper::toDomain);
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        return resilience.protectMany(() -> dataRepository.findByFranchiseId(franchiseId))
                .map(MongoMapper::toDomain);
    }
}
