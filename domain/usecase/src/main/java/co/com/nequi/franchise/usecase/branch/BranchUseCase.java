package co.com.nequi.franchise.usecase.branch;

import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.branch.gateways.BranchRepository;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.validation.DomainValidator;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BranchUseCase {

    private final BranchRepository branchRepository;
    private final FranchiseUseCase franchiseUseCase;

    public Mono<Branch> addBranch(String franchiseId, String name) {
        return DomainValidator.requireValidName(name)
                .flatMap(validName -> franchiseUseCase.findFranchise(franchiseId)
                        .map(franchise -> Branch.builder()
                                .franchiseId(franchise.getId())
                                .name(validName)
                                .build()))
                .flatMap(branchRepository::save);
    }

    public Mono<Branch> updateBranchName(String franchiseId, String branchId, String name) {
        return DomainValidator.requireValidName(name)
                .flatMap(validName -> findBranch(franchiseId, branchId)
                        .map(branch -> branch.toBuilder().name(validName).build()))
                .flatMap(branchRepository::save);
    }

    public Mono<Branch> findBranch(String franchiseId, String branchId) {
        return branchRepository.findById(branchId)
                .filter(branch -> branch.getFranchiseId().equals(franchiseId))
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.BRANCH_NOT_FOUND)));
    }
}
