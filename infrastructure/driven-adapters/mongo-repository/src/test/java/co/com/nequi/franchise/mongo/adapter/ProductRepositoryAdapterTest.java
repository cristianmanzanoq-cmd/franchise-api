package co.com.nequi.franchise.mongo.adapter;

import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.mongo.document.ProductDocument;
import co.com.nequi.franchise.mongo.repository.ProductDataRepository;
import co.com.nequi.franchise.mongo.resilience.MongoResilience;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

    @Mock
    private ProductDataRepository dataRepository;

    private ProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(10))
                .build());
        MongoResilience resilience = new MongoResilience(
                CircuitBreakerRegistry.ofDefaults(), retryRegistry, TimeLimiterRegistry.ofDefaults());
        adapter = new ProductRepositoryAdapter(dataRepository, resilience);
    }

    @Test
    void shouldSaveProductAndMapToDomain() {
        ProductDocument savedDocument = ProductDocument.builder()
                .id("p1").branchId("b1").name("Coffee").stock(10).build();
        when(dataRepository.save(any(ProductDocument.class))).thenReturn(Mono.just(savedDocument));

        Product toSave = Product.builder().branchId("b1").name("Coffee").stock(10).build();

        StepVerifier.create(adapter.save(toSave))
                .assertNext(product -> {
                    Assertions.assertEquals("p1", product.getId());
                    Assertions.assertEquals("b1", product.getBranchId());
                    Assertions.assertEquals(10, product.getStock());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindTopStockProductByBranch() {
        ProductDocument document = ProductDocument.builder()
                .id("p9").branchId("b1").name("Tea").stock(70).build();
        when(dataRepository.findFirstByBranchIdOrderByStockDesc("b1")).thenReturn(Mono.just(document));

        StepVerifier.create(adapter.findTopStockByBranchId("b1"))
                .assertNext(product -> Assertions.assertEquals(70, product.getStock()))
                .verifyComplete();
    }

    @Test
    void shouldRetryFailedOperationBeforePropagatingError() {
        when(dataRepository.findById("p1"))
                .thenReturn(Mono.error(new RuntimeException("transient failure")))
                .thenReturn(Mono.just(ProductDocument.builder()
                        .id("p1").branchId("b1").name("Coffee").stock(10).build()));

        StepVerifier.create(adapter.findById("p1"))
                .assertNext(product -> Assertions.assertEquals("p1", product.getId()))
                .verifyComplete();
    }

    @Test
    void shouldDeleteProductById() {
        when(dataRepository.deleteById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById("p1"))
                .verifyComplete();
    }
}
