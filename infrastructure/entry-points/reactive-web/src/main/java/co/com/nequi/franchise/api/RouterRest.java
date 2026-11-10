package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.BranchResponse;
import co.com.nequi.franchise.api.dto.CreateBranchRequest;
import co.com.nequi.franchise.api.dto.CreateFranchiseRequest;
import co.com.nequi.franchise.api.dto.CreateProductRequest;
import co.com.nequi.franchise.api.dto.FranchiseResponse;
import co.com.nequi.franchise.api.dto.ProductResponse;
import co.com.nequi.franchise.api.dto.TopStockProductResponse;
import co.com.nequi.franchise.api.dto.UpdateNameRequest;
import co.com.nequi.franchise.api.dto.UpdateStockRequest;
import co.com.nequi.franchise.api.error.GlobalErrorFilter;
import co.com.nequi.franchise.api.handler.BranchHandler;
import co.com.nequi.franchise.api.handler.FranchiseHandler;
import co.com.nequi.franchise.api.handler.ProductHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterRest {

    private static final String FRANCHISES = "/api/v1/franchises";
    private static final String FRANCHISE = FRANCHISES + "/{franchiseId}";
    private static final String BRANCHES = FRANCHISE + "/branches";
    private static final String BRANCH = BRANCHES + "/{branchId}";
    private static final String PRODUCTS = BRANCH + "/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";

    @Bean
    @RouterOperations({
            @RouterOperation(path = FRANCHISES, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
                    operation = @Operation(operationId = "createFranchise",
                            summary = "Create a new franchise",
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = CreateFranchiseRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request")})),
            @RouterOperation(path = FRANCHISE + "/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateFranchiseName",
                    operation = @Operation(operationId = "updateFranchiseName",
                            summary = "Update the name of a franchise",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateNameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise updated",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found")})),
            @RouterOperation(path = BRANCHES, method = RequestMethod.POST,
                    beanClass = BranchHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch",
                            summary = "Add a branch to a franchise",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = CreateBranchRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch created",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found")})),
            @RouterOperation(path = BRANCH + "/name", method = RequestMethod.PATCH,
                    beanClass = BranchHandler.class, beanMethod = "updateBranchName",
                    operation = @Operation(operationId = "updateBranchName",
                            summary = "Update the name of a branch",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateNameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch updated",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found")})),
            @RouterOperation(path = PRODUCTS, method = RequestMethod.POST,
                    beanClass = ProductHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct",
                            summary = "Add a product to a branch",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = CreateProductRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product created",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found")})),
            @RouterOperation(path = PRODUCT, method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class, beanMethod = "removeProduct",
                    operation = @Operation(operationId = "removeProduct",
                            summary = "Remove a product from a branch",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)},
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Product deleted"),
                                    @ApiResponse(responseCode = "404", description = "Product not found")})),
            @RouterOperation(path = PRODUCT + "/stock", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateStock",
                    operation = @Operation(operationId = "updateStock",
                            summary = "Update the stock of a product",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateStockRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found")})),
            @RouterOperation(path = PRODUCT + "/name", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateProductName",
                    operation = @Operation(operationId = "updateProductName",
                            summary = "Update the name of a product",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateNameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product renamed",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found")})),
            @RouterOperation(path = FRANCHISE + "/products/top-stock", method = RequestMethod.GET,
                    beanClass = ProductHandler.class, beanMethod = "findTopStockProducts",
                    operation = @Operation(operationId = "findTopStockProducts",
                            summary = "Get the highest-stock product per branch of a franchise",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top products per branch",
                                            content = @Content(schema = @Schema(implementation = TopStockProductResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found")}))
    })
    public RouterFunction<ServerResponse> routerFunction(FranchiseHandler franchiseHandler,
                                                         BranchHandler branchHandler,
                                                         ProductHandler productHandler,
                                                         GlobalErrorFilter globalErrorFilter) {
        return RouterFunctions.route()
                .POST(FRANCHISES, franchiseHandler::createFranchise)
                .PATCH(FRANCHISE + "/name", franchiseHandler::updateFranchiseName)
                .POST(BRANCHES, branchHandler::addBranch)
                .PATCH(BRANCH + "/name", branchHandler::updateBranchName)
                .POST(PRODUCTS, productHandler::addProduct)
                .DELETE(PRODUCT, productHandler::removeProduct)
                .PATCH(PRODUCT + "/stock", productHandler::updateStock)
                .PATCH(PRODUCT + "/name", productHandler::updateProductName)
                .GET(FRANCHISE + "/products/top-stock", productHandler::findTopStockProducts)
                .filter(globalErrorFilter)
                .build();
    }
}
