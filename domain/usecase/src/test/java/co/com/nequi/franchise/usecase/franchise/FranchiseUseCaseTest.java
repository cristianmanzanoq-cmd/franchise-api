package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    private FranchiseUseCase franchiseUseCase;

    @BeforeEach
    void setUp() {
        franchiseUseCase = new FranchiseUseCase(franchiseRepository);
    }

    @Test
    void shouldCreateFranchiseWhenNameIsValid() {
        Franchise saved = Franchise.builder().id("f1").name("Nequi Stores").build();
        when(franchiseRepository.save(any(Franchise.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(franchiseUseCase.createFranchise("Nequi Stores"))
                .assertNext(franchise -> {
                    org.junit.jupiter.api.Assertions.assertEquals("f1", franchise.getId());
                    org.junit.jupiter.api.Assertions.assertEquals("Nequi Stores", franchise.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailToCreateFranchiseWhenNameIsBlank() {
        StepVerifier.create(franchiseUseCase.createFranchise("   "))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.INVALID_NAME)
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @Test
    void shouldFailToCreateFranchiseWhenNameIsNull() {
        StepVerifier.create(franchiseUseCase.createFranchise(null))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.INVALID_NAME)
                .verify();
    }

    @Test
    void shouldUpdateFranchiseName() {
        Franchise existing = Franchise.builder().id("f1").name("Old Name").build();
        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(existing));
        when(franchiseRepository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(franchiseUseCase.updateFranchiseName("f1", "New Name"))
                .assertNext(franchise -> {
                    org.junit.jupiter.api.Assertions.assertEquals("f1", franchise.getId());
                    org.junit.jupiter.api.Assertions.assertEquals("New Name", franchise.getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailToUpdateNameWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.updateFranchiseName("missing", "New Name"))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.FRANCHISE_NOT_FOUND)
                .verify();
    }
}
