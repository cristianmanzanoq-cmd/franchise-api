package co.com.nequi.franchise.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_NAME("Name must not be null or blank", ErrorType.VALIDATION),
    INVALID_STOCK("Stock must be a non-negative number", ErrorType.VALIDATION),
    INVALID_REQUEST_BODY("Request body is missing or malformed", ErrorType.VALIDATION),
    FRANCHISE_NOT_FOUND("Franchise not found", ErrorType.NOT_FOUND),
    BRANCH_NOT_FOUND("Branch not found for the given franchise", ErrorType.NOT_FOUND),
    PRODUCT_NOT_FOUND("Product not found for the given branch", ErrorType.NOT_FOUND);

    private final String message;
    private final ErrorType type;
}
