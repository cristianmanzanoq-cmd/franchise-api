package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.CreateFranchiseRequest;
import co.com.nequi.franchise.api.dto.CreateProductRequest;
import co.com.nequi.franchise.api.dto.UpdateNameRequest;
import co.com.nequi.franchise.api.dto.UpdateStockRequest;
import co.com.nequi.franchise.api.error.GlobalErrorFilter;
import co.com.nequi.franchise.api.handler.BranchHandler;
import co.com.nequi.franchise.api.handler.FranchiseHandler;
import co.com.nequi.franchise.api.handler.ProductHandler;
import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.model.product.TopStockProduct;
import co.com.nequi.franchise.usecase.branch.BranchUseCase;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import co.com.nequi.franchise.usecase.product.ProductUseCase;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    @Mock
    private FranchiseUseCase franchiseUseCase;

    @Mock
    private BranchUseCase branchUseCase;

    @Mock
    private ProductUseCase productUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToRouterFunction(
                        new RouterRest().routerFunction(
                                new FranchiseHandler(franchiseUseCase),
                                new BranchHandler(branchUseCase),
                                new ProductHandler(productUseCase),
                                new GlobalErrorFilter()))
                .build();
    }

    @Test
    void shouldCreateFranchiseAndReturn201() {
        when(franchiseUseCase.createFranchise("Nequi Stores"))
                .thenReturn(Mono.just(Franchise.builder().id("f1").name("Nequi Stores").build()));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .bodyValue(new CreateFranchiseRequest("Nequi Stores"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.name").isEqualTo("Nequi Stores");
    }

    @Test
    void shouldReturn404WhenFranchiseNotFound() {
        when(franchiseUseCase.updateFranchiseName(anyString(), anyString()))
                .thenReturn(Mono.error(new DomainException(ErrorCode.FRANCHISE_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/franchises/missing/name")
                .bodyValue(new CreateFranchiseRequest("New Name"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("FRANCHISE_NOT_FOUND");
    }

    @Test
    void shouldReturn400WhenValidationFails() {
        when(franchiseUseCase.createFranchise(anyString()))
                .thenReturn(Mono.error(new DomainException(ErrorCode.INVALID_NAME)));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .bodyValue(new CreateFranchiseRequest(" "))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_NAME");
    }

    @Test
    void shouldAddBranchAndReturn201() {
        when(branchUseCase.addBranch("f1", "Medellin Branch"))
                .thenReturn(Mono.just(Branch.builder()
                        .id("b1").franchiseId("f1").name("Medellin Branch").build()));

        webTestClient.post()
                .uri("/api/v1/franchises/f1/branches")
                .bodyValue(new CreateFranchiseRequest("Medellin Branch"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("b1")
                .jsonPath("$.franchiseId").isEqualTo("f1");
    }

    @Test
    void shouldAddProductAndReturn201() {
        when(productUseCase.addProduct("f1", "b1", "Coffee", 10))
                .thenReturn(Mono.just(Product.builder()
                        .id("p1").branchId("b1").name("Coffee").stock(10).build()));

        webTestClient.post()
                .uri("/api/v1/franchises/f1/branches/b1/products")
                .bodyValue(new CreateProductRequest("Coffee", 10))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(10);
    }

    @Test
    void shouldRemoveProductAndReturn204() {
        when(productUseCase.removeProduct("f1", "b1", "p1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/franchises/f1/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldReturnTopStockProductsPerBranch() {
        Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Medellin Branch").build();
        Product product = Product.builder().id("p1").branchId("b1").name("Coffee").stock(99).build();
        when(productUseCase.findTopStockProductsByFranchise("f1"))
                .thenReturn(Flux.just(TopStockProduct.builder().branch(branch).product(product).build()));

        webTestClient.get()
                .uri("/api/v1/franchises/f1/products/top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchId").isEqualTo("b1")
                .jsonPath("$[0].product.stock").isEqualTo(99);
    }

    @Test
    void shouldReturn500OnUnexpectedError() {
        when(franchiseUseCase.createFranchise(anyString()))
                .thenReturn(Mono.error(new IllegalStateException("boom")));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .bodyValue(new CreateFranchiseRequest("Nequi Stores"))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void shouldUpdateFranchiseName() {
        when(franchiseUseCase.updateFranchiseName("f1", "Nequi Stores Colombia"))
                .thenReturn(Mono.just(Franchise.builder().id("f1").name("Nequi Stores Colombia").build()));

        webTestClient.patch()
                .uri("/api/v1/franchises/f1/name")
                .bodyValue(new UpdateNameRequest("Nequi Stores Colombia"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nequi Stores Colombia");
    }

    @Test
    void shouldUpdateBranchName() {
        when(branchUseCase.updateBranchName("f1", "b1", "Medellin Downtown"))
                .thenReturn(Mono.just(Branch.builder()
                        .id("b1").franchiseId("f1").name("Medellin Downtown").build()));

        webTestClient.patch()
                .uri("/api/v1/franchises/f1/branches/b1/name")
                .bodyValue(new UpdateNameRequest("Medellin Downtown"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Medellin Downtown");
    }

    @Test
    void shouldReturn404WhenBranchDoesNotBelongToFranchise() {
        when(branchUseCase.updateBranchName("f1", "other", "Name"))
                .thenReturn(Mono.error(new DomainException(ErrorCode.BRANCH_NOT_FOUND)));

        webTestClient.patch()
                .uri("/api/v1/franchises/f1/branches/other/name")
                .bodyValue(new UpdateNameRequest("Name"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("BRANCH_NOT_FOUND");
    }

    @Test
    void shouldUpdateProductStock() {
        when(productUseCase.updateStock("f1", "b1", "p1", 42))
                .thenReturn(Mono.just(Product.builder()
                        .id("p1").branchId("b1").name("Coffee").stock(42).build()));

        webTestClient.patch()
                .uri("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                .bodyValue(new UpdateStockRequest(42))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(42);
    }

    @Test
    void shouldReturn400WhenStockIsNegative() {
        when(productUseCase.updateStock("f1", "b1", "p1", -1))
                .thenReturn(Mono.error(new DomainException(ErrorCode.INVALID_STOCK)));

        webTestClient.patch()
                .uri("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                .bodyValue(new UpdateStockRequest(-1))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_STOCK");
    }

    @Test
    void shouldUpdateProductName() {
        when(productUseCase.updateProductName("f1", "b1", "p1", "Coffee 1kg"))
                .thenReturn(Mono.just(Product.builder()
                        .id("p1").branchId("b1").name("Coffee 1kg").stock(10).build()));

        webTestClient.patch()
                .uri("/api/v1/franchises/f1/branches/b1/products/p1/name")
                .bodyValue(new UpdateNameRequest("Coffee 1kg"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Coffee 1kg");
    }

    @Test
    void shouldReturn404WhenProductBelongsToAnotherBranch() {
        when(productUseCase.removeProduct("f1", "b1", "p9"))
                .thenReturn(Mono.error(new DomainException(ErrorCode.PRODUCT_NOT_FOUND)));

        webTestClient.delete()
                .uri("/api/v1/franchises/f1/branches/b1/products/p9")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() {
        webTestClient.post()
                .uri("/api/v1/franchises/f1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST_BODY");

        verifyNoInteractions(branchUseCase);
    }

    @Test
    void shouldReturn400WhenBodyIsMalformed() {
        webTestClient.patch()
                .uri("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ not valid json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST_BODY");

        verifyNoInteractions(productUseCase);
    }

    @Test
    void shouldReturn404OnTopStockWhenFranchiseDoesNotExist() {
        when(productUseCase.findTopStockProductsByFranchise("missing"))
                .thenReturn(Flux.error(new DomainException(ErrorCode.FRANCHISE_NOT_FOUND)));

        webTestClient.get()
                .uri("/api/v1/franchises/missing/products/top-stock")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("FRANCHISE_NOT_FOUND");
    }

    @Test
    void shouldReturn504WhenDatabaseTimesOut() {
        when(productUseCase.findTopStockProductsByFranchise("f1"))
                .thenReturn(Flux.error(new TimeoutException("mongo too slow")));

        webTestClient.get()
                .uri("/api/v1/franchises/f1/products/top-stock")
                .exchange()
                .expectStatus().isEqualTo(504)
                .expectBody()
                .jsonPath("$.code").isEqualTo("TIMEOUT");
    }

    @Test
    void shouldReturn503WhenCircuitIsOpen() {
        when(franchiseUseCase.createFranchise("Nequi Stores"))
                .thenReturn(Mono.error(CallNotPermittedException
                        .createCallNotPermittedException(CircuitBreaker.ofDefaults("mongoRepository"))));

        webTestClient.post()
                .uri("/api/v1/franchises")
                .bodyValue(new CreateFranchiseRequest("Nequi Stores"))
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CIRCUIT_OPEN");
    }
}
