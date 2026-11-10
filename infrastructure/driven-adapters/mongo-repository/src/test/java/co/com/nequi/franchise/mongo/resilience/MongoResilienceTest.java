package co.com.nequi.franchise.mongo.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

class MongoResilienceTest {

    private static final Duration TIMEOUT = Duration.ofMillis(100);

    private MongoResilience resilience(int retryAttempts, CircuitBreakerConfig circuitBreakerConfig) {
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(retryAttempts)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(IllegalStateException.class)
                .build());
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.of(TimeLimiterConfig.custom()
                .timeoutDuration(TIMEOUT)
                .build());
        return new MongoResilience(
                CircuitBreakerRegistry.of(circuitBreakerConfig), retryRegistry, timeLimiterRegistry);
    }

    private MongoResilience resilience(int retryAttempts) {
        return resilience(retryAttempts, CircuitBreakerConfig.ofDefaults());
    }

    @Test
    void shouldRetryTransientFailureAndSucceed() {
        AtomicInteger attempts = new AtomicInteger();

        StepVerifier.create(resilience(3).protect(() -> Mono.fromCallable(attempts::incrementAndGet)
                        .filter(attempt -> attempt > 1)
                        .map(attempt -> "ok")
                        .switchIfEmpty(Mono.error(new IllegalStateException("transient")))))
                .expectNext("ok")
                .verifyComplete();

        Assertions.assertEquals(2, attempts.get());
    }

    @Test
    void shouldFailWithTimeoutWithoutRetrying() {
        AtomicInteger attempts = new AtomicInteger();

        StepVerifier.create(resilience(3).protect(() -> Mono.fromCallable(attempts::incrementAndGet)
                        .then(Mono.never())))
                .expectError(TimeoutException.class)
                .verify(Duration.ofSeconds(5));

        Assertions.assertEquals(1, attempts.get());
    }

    @Test
    void shouldOpenCircuitAfterRepeatedFailures() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build();
        MongoResilience resilience = resilience(1, circuitBreakerConfig);
        AtomicInteger calls = new AtomicInteger();

        StepVerifier.create(resilience.protect(() -> Mono.fromCallable(calls::incrementAndGet)
                        .then(Mono.error(new IllegalStateException("database down")))))
                .expectError(IllegalStateException.class)
                .verify();
        StepVerifier.create(resilience.protect(() -> Mono.fromCallable(calls::incrementAndGet)
                        .then(Mono.error(new IllegalStateException("database down")))))
                .expectError(IllegalStateException.class)
                .verify();

        StepVerifier.create(resilience.protect(() -> Mono.fromCallable(calls::incrementAndGet)))
                .expectError(CallNotPermittedException.class)
                .verify();

        Assertions.assertEquals(2, calls.get());
    }

    @Test
    void shouldProtectFluxOperations() {
        StepVerifier.create(resilience(3).protectMany(() -> Flux.just("b1", "b2")))
                .expectNext("b1", "b2")
                .verifyComplete();
    }
}
