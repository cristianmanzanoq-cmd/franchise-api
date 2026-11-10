package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.CreateFranchiseRequest;
import co.com.nequi.franchise.api.dto.CreateProductRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
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
}
