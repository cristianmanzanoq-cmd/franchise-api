package co.com.nequi.franchise.api.handler;

import co.com.nequi.franchise.api.dto.CreateFranchiseRequest;
import co.com.nequi.franchise.api.dto.UpdateNameRequest;
import co.com.nequi.franchise.api.mapper.ApiMapper;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_REQUEST_BODY)))
                .flatMap(body -> franchiseUseCase.createFranchise(body.name()))
                .map(ApiMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        return request.bodyToMono(UpdateNameRequest.class)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_REQUEST_BODY)))
                .flatMap(body -> franchiseUseCase.updateFranchiseName(
                        request.pathVariable("franchiseId"), body.name()))
                .map(ApiMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
