package co.com.nequi.franchise.usecase.branch;

import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.branch.gateways.BranchRepository;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchUseCaseTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    private BranchUseCase branchUseCase;

    @BeforeEach
    void setUp() {
        branchUseCase = new BranchUseCase(branchRepository, new FranchiseUseCase(franchiseRepository));
    }

    @Test
    void shouldAddBranchToExistingFranchise() {
        Franchise franchise = Franchise.builder().id("f1").name("Nequi Stores").build();
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> {
                    Branch toSave = invocation.getArgument(0);
                    return Mono.just(toSave.toBuilder().id("b1").build());
                });

        StepVerifier.create(branchUseCase.addBranch("f1", "Medellin Branch"))
                .assertNext(branch -> {
                    Assertions.assertEquals("b1", branch.getId());
                    Assertions.assertEquals("f1", branch.getFranchiseId());
                    Assertions.assertEquals("Medellin Branch", branch.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailToAddBranchWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(branchUseCase.addBranch("missing", "Medellin Branch"))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.FRANCHISE_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldUpdateBranchName() {
        Branch existing = Branch.builder().id("b1").franchiseId("f1").name("Old Branch").build();
        when(branchRepository.findById("b1")).thenReturn(Mono.just(existing));
        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(branchUseCase.updateBranchName("f1", "b1", "New Branch"))
                .assertNext(branch -> Assertions.assertEquals("New Branch", branch.getName()))
                .verifyComplete();
    }

    @Test
    void shouldFailToUpdateBranchNameWhenBranchBelongsToAnotherFranchise() {
        Branch existing = Branch.builder().id("b1").franchiseId("other").name("Old Branch").build();
        when(branchRepository.findById("b1")).thenReturn(Mono.just(existing));

        StepVerifier.create(branchUseCase.updateBranchName("f1", "b1", "New Branch"))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.BRANCH_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldFailToAddBranchWhenNameIsInvalid() {
        StepVerifier.create(branchUseCase.addBranch("f1", ""))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.INVALID_NAME)
                .verify();
    }
}
