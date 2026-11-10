package co.com.nequi.franchise.api.handler;

import co.com.nequi.franchise.api.dto.CreateProductRequest;
import co.com.nequi.franchise.api.dto.TopStockProductResponse;
import co.com.nequi.franchise.api.dto.UpdateNameRequest;
import co.com.nequi.franchise.api.dto.UpdateStockRequest;
import co.com.nequi.franchise.api.mapper.ApiMapper;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.usecase.product.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        return request.bodyToMono(CreateProductRequest.class)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_REQUEST_BODY)))
                .flatMap(body -> productUseCase.addProduct(
                        request.pathVariable("franchiseId"),
                        request.pathVariable("branchId"),
                        body.name(),
                        body.stock()))
                .map(ApiMapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        return productUseCase.removeProduct(
                        request.pathVariable("franchiseId"),
                        request.pathVariable("branchId"),
                        request.pathVariable("productId"))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        return request.bodyToMono(UpdateStockRequest.class)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_REQUEST_BODY)))
                .flatMap(body -> productUseCase.updateStock(
                        request.pathVariable("franchiseId"),
                        request.pathVariable("branchId"),
                        request.pathVariable("productId"),
                        body.stock()))
                .map(ApiMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        return request.bodyToMono(UpdateNameRequest.class)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_REQUEST_BODY)))
                .flatMap(body -> productUseCase.updateProductName(
                        request.pathVariable("franchiseId"),
                        request.pathVariable("branchId"),
                        request.pathVariable("productId"),
                        body.name()))
                .map(ApiMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> findTopStockProducts(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productUseCase.findTopStockProductsByFranchise(
                                request.pathVariable("franchiseId"))
                        .map(ApiMapper::toResponse), TopStockProductResponse.class);
    }
}
