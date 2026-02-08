package edu.teleinformatics.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * An enum representing standardized error codes, default messages, and corresponding HTTP status codes for various error scenarios in the application.
 * This enum is used in conjunction with the BusinessException class to provide consistent error handling throughout the application.
 */
@Getter
@AllArgsConstructor
public enum ErrorHandler {
    // GENERIC
    INTERNAL_ERROR("GEN-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("GEN-002", "Invalid field values", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("GEN-003", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_GENERIC("GEN-004", "The requested resource was not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("GEN-005", "The requested HTTP method is not allowed for this endpoint", HttpStatus.METHOD_NOT_ALLOWED),

    // AUTH
    USER_ALREADY_EXISTS("AUTH-001", "The email address is already in use", HttpStatus.CONFLICT),
    AUTH_FAILED("AUTH-002", "Authentication failed", HttpStatus.UNAUTHORIZED),

    // DB / NOT FOUND
    USER_NOT_FOUND("DB-001", "The requested user does not exist", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND("DB-002", "The requested role was not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
}