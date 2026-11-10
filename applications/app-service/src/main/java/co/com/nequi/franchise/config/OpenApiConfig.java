package co.com.nequi.franchise.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchiseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Franchise Management API")
                        .description("Reactive API to manage franchises, branches and products. "
                                + "Built with Spring WebFlux, functional routing and hexagonal architecture.")
                        .version("1.0.0")
                        .contact(new Contact().name("Franchise API Team")));
    }
}
