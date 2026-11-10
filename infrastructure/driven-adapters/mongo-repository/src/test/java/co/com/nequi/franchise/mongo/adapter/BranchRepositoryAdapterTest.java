package co.com.nequi.franchise.mongo.adapter;

import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.mongo.document.BranchDocument;
import co.com.nequi.franchise.mongo.repository.BranchDataRepository;
import co.com.nequi.franchise.mongo.resilience.MongoResilience;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchRepositoryAdapterTest {

    @Mock
    private BranchDataRepository dataRepository;

    private BranchRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MongoResilience resilience = new MongoResilience(
                CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults());
        adapter = new BranchRepositoryAdapter(dataRepository, resilience);
    }

    @Test
    void shouldSaveBranchKeepingFranchiseReference() {
        when(dataRepository.save(any(BranchDocument.class)))
                .thenReturn(Mono.just(BranchDocument.builder()
                        .id("b1").franchiseId("f1").name("Medellin Branch").build()));

        StepVerifier.create(adapter.save(Branch.builder().franchiseId("f1").name("Medellin Branch").build()))
                .assertNext(branch -> {
                    Assertions.assertEquals("b1", branch.getId());
                    Assertions.assertEquals("f1", branch.getFranchiseId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindBranchById() {
        when(dataRepository.findById("b1"))
                .thenReturn(Mono.just(BranchDocument.builder()
                        .id("b1").franchiseId("f1").name("Medellin Branch").build()));

        StepVerifier.create(adapter.findById("b1"))
                .assertNext(branch -> Assertions.assertEquals("Medellin Branch", branch.getName()))
                .verifyComplete();
    }

    @Test
    void shouldStreamAllBranchesOfFranchise() {
        when(dataRepository.findByFranchiseId("f1"))
                .thenReturn(Flux.just(
                        BranchDocument.builder().id("b1").franchiseId("f1").name("Medellin Branch").build(),
                        BranchDocument.builder().id("b2").franchiseId("f1").name("Bogota Branch").build()));

        StepVerifier.create(adapter.findByFranchiseId("f1"))
                .assertNext(branch -> Assertions.assertEquals("b1", branch.getId()))
                .assertNext(branch -> Assertions.assertEquals("b2", branch.getId()))
                .verifyComplete();
    }
}
