package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.validation.DomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> createFranchise(String name) {
        return DomainValidator.requireValidName(name)
                .map(validName -> Franchise.builder().name(validName).build())
                .flatMap(franchiseRepository::save);
    }

    public Mono<Franchise> updateFranchiseName(String franchiseId, String name) {
        return DomainValidator.requireValidName(name)
                .flatMap(validName -> findFranchise(franchiseId)
                        .map(franchise -> franchise.toBuilder().name(validName).build()))
                .flatMap(franchiseRepository::save);
    }

    public Mono<Franchise> findFranchise(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.FRANCHISE_NOT_FOUND)));
    }
}
