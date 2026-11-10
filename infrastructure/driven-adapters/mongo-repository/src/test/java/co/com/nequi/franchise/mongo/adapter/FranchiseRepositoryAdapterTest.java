package co.com.nequi.franchise.mongo.adapter;

import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.mongo.document.FranchiseDocument;
import co.com.nequi.franchise.mongo.repository.FranchiseDataRepository;
import co.com.nequi.franchise.mongo.resilience.MongoResilience;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseRepositoryAdapterTest {

    @Mock
    private FranchiseDataRepository dataRepository;

    private FranchiseRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MongoResilience resilience = new MongoResilience(
                CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults());
        adapter = new FranchiseRepositoryAdapter(dataRepository, resilience);
    }

    @Test
    void shouldMapFranchiseToDocumentAndBackOnSave() {
        when(dataRepository.save(any(FranchiseDocument.class)))
                .thenReturn(Mono.just(FranchiseDocument.builder().id("f1").name("Nequi Stores").build()));

        StepVerifier.create(adapter.save(Franchise.builder().name("Nequi Stores").build()))
                .assertNext(franchise -> {
                    Assertions.assertEquals("f1", franchise.getId());
                    Assertions.assertEquals("Nequi Stores", franchise.getName());
                })
                .verifyComplete();

        ArgumentCaptor<FranchiseDocument> captor = ArgumentCaptor.forClass(FranchiseDocument.class);
        verify(dataRepository).save(captor.capture());
        Assertions.assertEquals("Nequi Stores", captor.getValue().getName());
    }

    @Test
    void shouldFindFranchiseById() {
        when(dataRepository.findById("f1"))
                .thenReturn(Mono.just(FranchiseDocument.builder().id("f1").name("Nequi Stores").build()));

        StepVerifier.create(adapter.findById("f1"))
                .assertNext(franchise -> Assertions.assertEquals("Nequi Stores", franchise.getName()))
                .verifyComplete();
    }

    @Test
    void shouldCompleteEmptyWhenFranchiseDoesNotExist() {
        when(dataRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("missing"))
                .verifyComplete();
    }
}
