package co.com.nequi.franchise.mongo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Decorates reactive publishers with the full resilience pipeline:
 * TimeLimiter (timeout) -> Retry (with backoff) -> CircuitBreaker.
 * Operations are wrapped in defer(...) so every retry re-executes the
 * underlying call instead of resubscribing to a stale publisher.
 * Instances are resolved from the registries configured in application.yaml.
 */
@Component
public class MongoResilience {

    private static final String INSTANCE_NAME = "mongoRepository";

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    public MongoResilience(CircuitBreakerRegistry circuitBreakerRegistry,
                           RetryRegistry retryRegistry,
                           TimeLimiterRegistry timeLimiterRegistry) {
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(INSTANCE_NAME);
        this.retry = retryRegistry.retry(INSTANCE_NAME);
        this.timeLimiter = timeLimiterRegistry.timeLimiter(INSTANCE_NAME);
    }

    public <T> Mono<T> protect(Supplier<Mono<T>> operation) {
        return Mono.defer(operation)
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }

    public <T> Flux<T> protectMany(Supplier<Flux<T>> operation) {
        return Flux.defer(operation)
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }
}
