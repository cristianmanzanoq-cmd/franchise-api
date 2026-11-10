package co.com.nequi.franchise.usecase.product;

import co.com.nequi.franchise.model.branch.Branch;
import co.com.nequi.franchise.model.branch.gateways.BranchRepository;
import co.com.nequi.franchise.model.exception.DomainException;
import co.com.nequi.franchise.model.exception.ErrorCode;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.product.Product;
import co.com.nequi.franchise.model.product.gateways.ProductRepository;
import co.com.nequi.franchise.usecase.branch.BranchUseCase;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    private ProductUseCase productUseCase;

    private final Franchise franchise = Franchise.builder().id("f1").name("Nequi Stores").build();
    private final Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Medellin Branch").build();
    private final Product product = Product.builder().id("p1").branchId("b1").name("Coffee").stock(10).build();

    @BeforeEach
    void setUp() {
        FranchiseUseCase franchiseUseCase = new FranchiseUseCase(franchiseRepository);
        BranchUseCase branchUseCase = new BranchUseCase(branchRepository, franchiseUseCase);
        productUseCase = new ProductUseCase(productRepository, branchRepository, branchUseCase, franchiseUseCase);
    }

    @Test
    void shouldAddProductToExistingBranch() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product toSave = invocation.getArgument(0);
                    return Mono.just(toSave.toBuilder().id("p1").build());
                });

        StepVerifier.create(productUseCase.addProduct("f1", "b1", "Coffee", 10))
                .assertNext(created -> {
                    Assertions.assertEquals("p1", created.getId());
                    Assertions.assertEquals("b1", created.getBranchId());
                    Assertions.assertEquals(10, created.getStock());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailToAddProductWhenStockIsNegative() {
        StepVerifier.create(productUseCase.addProduct("f1", "b1", "Coffee", -5))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.INVALID_STOCK)
                .verify();
    }

    @Test
    void shouldRemoveExistingProduct() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findById("p1")).thenReturn(Mono.just(product));
        when(productRepository.deleteById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(productUseCase.removeProduct("f1", "b1", "p1"))
                .verifyComplete();
    }

    @Test
    void shouldFailToRemoveProductWhenItDoesNotExist() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(productUseCase.removeProduct("f1", "b1", "missing"))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.PRODUCT_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldFailToRemoveProductWhenItBelongsToAnotherBranch() {
        Product foreignProduct = product.toBuilder().branchId("otherBranch").build();
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findById("p1")).thenReturn(Mono.just(foreignProduct));

        StepVerifier.create(productUseCase.removeProduct("f1", "b1", "p1"))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.PRODUCT_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldUpdateProductStock() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findById("p1")).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(productUseCase.updateStock("f1", "b1", "p1", 99))
                .assertNext(updated -> Assertions.assertEquals(99, updated.getStock()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateProductName() {
        when(branchRepository.findById("b1")).thenReturn(Mono.just(branch));
        when(productRepository.findById("p1")).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(productUseCase.updateProductName("f1", "b1", "p1", "Premium Coffee"))
                .assertNext(updated -> Assertions.assertEquals("Premium Coffee", updated.getName()))
                .verifyComplete();
    }

    @Test
    void shouldFindTopStockProductPerBranchOfFranchise() {
        Branch secondBranch = Branch.builder().id("b2").franchiseId("f1").name("Bogota Branch").build();
        Product topProductBranchOne = product.toBuilder().stock(50).build();
        Product topProductBranchTwo = Product.builder()
                .id("p2").branchId("b2").name("Tea").stock(70).build();

        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId("f1")).thenReturn(Flux.just(branch, secondBranch));
        when(productRepository.findTopStockByBranchId("b1")).thenReturn(Mono.just(topProductBranchOne));
        when(productRepository.findTopStockByBranchId("b2")).thenReturn(Mono.just(topProductBranchTwo));

        StepVerifier.create(productUseCase.findTopStockProductsByFranchise("f1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldSkipBranchesWithoutProductsInTopStockQuery() {
        Branch emptyBranch = Branch.builder().id("b2").franchiseId("f1").name("Bogota Branch").build();

        when(franchiseRepository.findById("f1")).thenReturn(Mono.just(franchise));
        when(branchRepository.findByFranchiseId("f1")).thenReturn(Flux.just(branch, emptyBranch));
        when(productRepository.findTopStockByBranchId("b1")).thenReturn(Mono.just(product));
        when(productRepository.findTopStockByBranchId("b2")).thenReturn(Mono.empty());

        StepVerifier.create(productUseCase.findTopStockProductsByFranchise("f1"))
                .assertNext(topStockProduct ->
                        Assertions.assertEquals("p1", topStockProduct.getProduct().getId()))
                .verifyComplete();
    }

    @Test
    void shouldFailTopStockQueryWhenFranchiseDoesNotExist() {
        when(franchiseRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(productUseCase.findTopStockProductsByFranchise("missing"))
                .expectErrorMatches(error -> error instanceof DomainException domainException
                        && domainException.getErrorCode() == ErrorCode.FRANCHISE_NOT_FOUND)
                .verify();
    }
}
