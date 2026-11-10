package co.com.nequi.franchise.model.validation;

import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import reactor.core.publisher.Mono;


public final class DomainValidator {

    private DomainValidator() {
    }

    public static Mono<String> requireValidName(String name) {
        return Mono.justOrEmpty(name)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_NAME)));
    }

    public static Mono<Integer> requireValidStock(Integer stock) {
        return Mono.justOrEmpty(stock)
                .filter(value -> value >= 0)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_STOCK)));
    }
}
