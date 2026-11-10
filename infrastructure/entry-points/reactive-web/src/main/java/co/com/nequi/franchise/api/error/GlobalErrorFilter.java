package co.com.nequi.franchise.api.error;

import co.com.nequi.franchise.api.dto.ErrorResponse;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.exception.ErrorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class GlobalErrorFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final Map<ErrorType, HttpStatus> STATUS_BY_ERROR_TYPE = Map.of(
            ErrorType.VALIDATION, HttpStatus.BAD_REQUEST,
            ErrorType.NOT_FOUND, HttpStatus.NOT_FOUND);

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                .onErrorResume(DomainException.class, this::handleDomainException)
                .onErrorResume(ServerWebInputException.class, exception ->
                        respond(HttpStatus.BAD_REQUEST,
                                ErrorCode.INVALID_REQUEST_BODY.name(),
                                ErrorCode.INVALID_REQUEST_BODY.getMessage()))
                .onErrorResume(TimeoutException.class, exception ->
                        respond(HttpStatus.GATEWAY_TIMEOUT, "TIMEOUT",
                                "The downstream operation timed out"))
                .onErrorResume(CallNotPermittedException.class, exception ->
                        respond(HttpStatus.SERVICE_UNAVAILABLE, "CIRCUIT_OPEN",
                                "Service is temporarily unavailable, please retry later"))
                .onErrorResume(exception ->
                        respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                                "An unexpected error occurred"));
    }

    private Mono<ServerResponse> handleDomainException(DomainException exception) {
        return respond(
                STATUS_BY_ERROR_TYPE.getOrDefault(
                        exception.getErrorCode().getType(), HttpStatus.INTERNAL_SERVER_ERROR),
                exception.getErrorCode().name(),
                exception.getMessage());
    }

    private Mono<ServerResponse> respond(HttpStatus status, String code, String message) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ErrorResponse(code, message, Instant.now()));
    }
}
