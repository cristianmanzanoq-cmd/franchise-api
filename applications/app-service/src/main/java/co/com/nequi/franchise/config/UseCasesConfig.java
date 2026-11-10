package co.com.nequi.franchise.config;

import co.com.nequi.franchise.model.branch.gateways.BranchRepository;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.product.gateways.ProductRepository;
import co.com.nequi.franchise.usecase.branch.BranchUseCase;
import co.com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import co.com.nequi.franchise.usecase.product.ProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires framework-agnostic use cases as Spring beans. The domain layer
 * stays free of Spring annotations; only this configuration knows Spring.
 */
@Configuration
public class UseCasesConfig {

    @Bean
    public FranchiseUseCase franchiseUseCase(FranchiseRepository franchiseRepository) {
        return new FranchiseUseCase(franchiseRepository);
    }

    @Bean
    public BranchUseCase branchUseCase(BranchRepository branchRepository,
                                       FranchiseUseCase franchiseUseCase) {
        return new BranchUseCase(branchRepository, franchiseUseCase);
    }

    @Bean
    public ProductUseCase productUseCase(ProductRepository productRepository,
                                         BranchRepository branchRepository,
                                         BranchUseCase branchUseCase,
                                         FranchiseUseCase franchiseUseCase) {
        return new ProductUseCase(productRepository, branchRepository, branchUseCase, franchiseUseCase);
    }
}
