package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.BranchResponse;
import co.com.nequi.franchise.api.dto.CreateBranchRequest;
import co.com.nequi.franchise.api.dto.CreateFranchiseRequest;
import co.com.nequi.franchise.api.dto.CreateProductRequest;
import co.com.nequi.franchise.api.dto.ErrorResponse;
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
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    private static final String FRANCHISE_EXAMPLE = "{\"id\":\"664f1a2b3c4d5e6f7a8b9c0d\",\"name\":\"Nequi Stores\"}";
    private static final String BRANCH_EXAMPLE = "{\"id\":\"775f1a2b3c4d5e6f7a8b9c0e\",\"franchiseId\":\"664f1a2b3c4d5e6f7a8b9c0d\",\"name\":\"Medellin Branch\"}";
    private static final String PRODUCT_EXAMPLE = "{\"id\":\"886f1a2b3c4d5e6f7a8b9c0f\",\"branchId\":\"775f1a2b3c4d5e6f7a8b9c0e\",\"name\":\"Coffee Bag 500g\",\"stock\":42}";
    private static final String TOP_STOCK_EXAMPLE = "[{\"branchId\":\"775f1a2b3c4d5e6f7a8b9c0e\",\"branchName\":\"Medellin Branch\",\"product\":" + PRODUCT_EXAMPLE + "}]";

    private static final String CREATE_FRANCHISE_REQUEST_EXAMPLE = "{\"name\":\"Nequi Stores\"}";
    private static final String UPDATE_FRANCHISE_REQUEST_EXAMPLE = "{\"name\":\"Nequi Stores Colombia\"}";
    private static final String CREATE_BRANCH_REQUEST_EXAMPLE = "{\"name\":\"Medellin Branch\"}";
    private static final String UPDATE_BRANCH_REQUEST_EXAMPLE = "{\"name\":\"Medellin Downtown Branch\"}";
    private static final String CREATE_PRODUCT_REQUEST_EXAMPLE = "{\"name\":\"Coffee Bag 500g\",\"stock\":100}";
    private static final String UPDATE_STOCK_REQUEST_EXAMPLE = "{\"stock\":42}";
    private static final String UPDATE_PRODUCT_REQUEST_EXAMPLE = "{\"name\":\"Coffee Bag 1kg\"}";

    private static final String ERROR_TIMESTAMP = "\"timestamp\":\"2026-11-10T15:00:00Z\"}";
    private static final String FRANCHISE_NOT_FOUND_EXAMPLE = "{\"code\":\"FRANCHISE_NOT_FOUND\",\"message\":\"Franchise not found\"," + ERROR_TIMESTAMP;
    private static final String BRANCH_NOT_FOUND_EXAMPLE = "{\"code\":\"BRANCH_NOT_FOUND\",\"message\":\"Branch not found for the given franchise\"," + ERROR_TIMESTAMP;
    private static final String PRODUCT_NOT_FOUND_EXAMPLE = "{\"code\":\"PRODUCT_NOT_FOUND\",\"message\":\"Product not found for the given branch\"," + ERROR_TIMESTAMP;
    private static final String INVALID_NAME_EXAMPLE = "{\"code\":\"INVALID_NAME\",\"message\":\"Name must not be null or blank\"," + ERROR_TIMESTAMP;
    private static final String INVALID_STOCK_EXAMPLE = "{\"code\":\"INVALID_STOCK\",\"message\":\"Stock must be a non-negative number\"," + ERROR_TIMESTAMP;
    private static final String INTERNAL_ERROR_EXAMPLE = "{\"code\":\"INTERNAL_ERROR\",\"message\":\"An unexpected error occurred\"," + ERROR_TIMESTAMP;
    private static final String CIRCUIT_OPEN_EXAMPLE = "{\"code\":\"CIRCUIT_OPEN\",\"message\":\"Service is temporarily unavailable, please retry later\"," + ERROR_TIMESTAMP;
    private static final String TIMEOUT_EXAMPLE = "{\"code\":\"TIMEOUT\",\"message\":\"The downstream operation timed out\"," + ERROR_TIMESTAMP;

    @Bean
    @RouterOperations({
            @RouterOperation(path = FRANCHISES, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
                    operation = @Operation(operationId = "createFranchise",
                            summary = "Create a new franchise",
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = CreateFranchiseRequest.class),
                                    examples = @ExampleObject(value = CREATE_FRANCHISE_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class),
                                                    examples = @ExampleObject(value = FRANCHISE_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Blank name or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_NAME_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = FRANCHISE + "/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateFranchiseName",
                    operation = @Operation(operationId = "updateFranchiseName",
                            summary = "Update the name of a franchise",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateNameRequest.class),
                                    examples = @ExampleObject(value = UPDATE_FRANCHISE_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise updated",
                                            content = @Content(schema = @Schema(implementation = FranchiseResponse.class),
                                                    examples = @ExampleObject(value = FRANCHISE_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Blank name or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_NAME_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = FRANCHISE_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = BRANCHES, method = RequestMethod.POST,
                    beanClass = BranchHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch",
                            summary = "Add a branch to a franchise",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = CreateBranchRequest.class),
                                    examples = @ExampleObject(value = CREATE_BRANCH_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Branch created",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class),
                                                    examples = @ExampleObject(value = BRANCH_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Blank name or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_NAME_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = FRANCHISE_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = BRANCH + "/name", method = RequestMethod.PATCH,
                    beanClass = BranchHandler.class, beanMethod = "updateBranchName",
                    operation = @Operation(operationId = "updateBranchName",
                            summary = "Update the name of a branch",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateNameRequest.class),
                                    examples = @ExampleObject(value = UPDATE_BRANCH_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch updated",
                                            content = @Content(schema = @Schema(implementation = BranchResponse.class),
                                                    examples = @ExampleObject(value = BRANCH_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Blank name or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_NAME_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = BRANCH_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = PRODUCTS, method = RequestMethod.POST,
                    beanClass = ProductHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct",
                            summary = "Add a product to a branch",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = CreateProductRequest.class),
                                    examples = @ExampleObject(value = CREATE_PRODUCT_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Product created",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class),
                                                    examples = @ExampleObject(value = PRODUCT_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Blank name, negative stock or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_STOCK_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = BRANCH_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
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
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = PRODUCT_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = PRODUCT + "/stock", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateStock",
                    operation = @Operation(operationId = "updateStock",
                            summary = "Update the stock of a product",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateStockRequest.class),
                                    examples = @ExampleObject(value = UPDATE_STOCK_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class),
                                                    examples = @ExampleObject(value = PRODUCT_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Negative stock or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_STOCK_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = PRODUCT_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = PRODUCT + "/name", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateProductName",
                    operation = @Operation(operationId = "updateProductName",
                            summary = "Update the name of a product",
                            parameters = {
                                    @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "branchId", in = ParameterIn.PATH, required = true),
                                    @Parameter(name = "productId", in = ParameterIn.PATH, required = true)},
                            requestBody = @RequestBody(content = @Content(
                                    schema = @Schema(implementation = UpdateNameRequest.class),
                                    examples = @ExampleObject(value = UPDATE_PRODUCT_REQUEST_EXAMPLE))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product renamed",
                                            content = @Content(schema = @Schema(implementation = ProductResponse.class),
                                                    examples = @ExampleObject(value = PRODUCT_EXAMPLE))),
                                    @ApiResponse(responseCode = "400", description = "Blank name or malformed body",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INVALID_NAME_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Product not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = PRODUCT_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))})),
            @RouterOperation(path = FRANCHISE + "/products/top-stock", method = RequestMethod.GET,
                    beanClass = ProductHandler.class, beanMethod = "findTopStockProducts",
                    operation = @Operation(operationId = "findTopStockProducts",
                            summary = "Get the highest-stock product per branch of a franchise",
                            parameters = @Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top products per branch",
                                            content = @Content(schema = @Schema(implementation = TopStockProductResponse.class),
                                                    examples = @ExampleObject(value = TOP_STOCK_EXAMPLE))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = FRANCHISE_NOT_FOUND_EXAMPLE))),
                                    @ApiResponse(responseCode = "500", description = "Unexpected error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = INTERNAL_ERROR_EXAMPLE))),
                                    @ApiResponse(responseCode = "503", description = "Circuit breaker open",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = CIRCUIT_OPEN_EXAMPLE))),
                                    @ApiResponse(responseCode = "504", description = "Database operation timed out",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                                    examples = @ExampleObject(value = TIMEOUT_EXAMPLE)))}))
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
