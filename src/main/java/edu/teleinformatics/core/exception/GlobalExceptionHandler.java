package edu.teleinformatics.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.teleinformatics.core.auth.exception.UserAlreadyExistsException;
import edu.teleinformatics.core.db.user.exception.RoleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application. It will handle all exceptions thrown by the controllers and return a standardized error response.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * Standardized error response for the API. It contains the following fields:
     *
     * @param message   Detailed message about the error
     * @param errorCode A custom error code for the specific error
     * @param hash      The stack trace of the exception, which can be included for debugging purposes. This field is optional and can be omitted in production environments to avoid exposing sensitive information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApiErrorResponse(
            String message,
            String errorCode,
            String hash
    ) {
        public ApiErrorResponse(String message, String errorCode) {
            this(message, errorCode, null);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Extract error messages from the validation errors and join them into a single string. Each error message will be in the format "field: error message". For example, if the "email" field is invalid, the error message will be "email: must not be blank".
        String errorMessage = ex.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("; "));

        logException(ex, request, errorMessage, ErrorHandler.INVALID_INPUT.getCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(errorMessage, ErrorHandler.INVALID_INPUT.getCode()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logException(ex, request, ex.getMessage(), ErrorHandler.AUTH_FAILED.getCode());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(ex.getMessage(), ErrorHandler.AUTH_FAILED.getCode()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        logException(ex, request, ex.getMessage(), ErrorHandler.NOT_FOUND_GENERIC.getCode());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(ex.getMessage(), ErrorHandler.NOT_FOUND_GENERIC.getCode()));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFoundException(RoleNotFoundException ex, HttpServletRequest request) {
        logException(ex, request, ErrorHandler.ROLE_NOT_FOUND.getDefaultMessage(), ErrorHandler.ROLE_NOT_FOUND.getCode());

        return ResponseEntity.status(ErrorHandler.ROLE_NOT_FOUND.getHttpStatus()).body(new ApiErrorResponse(ex.getMessage(), ErrorHandler.ROLE_NOT_FOUND.getCode()));
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, HttpServletRequest request) {
        logException(ex, request, ErrorHandler.USER_ALREADY_EXISTS.getDefaultMessage(), ErrorHandler.USER_ALREADY_EXISTS.getCode());

        return ResponseEntity.status(ErrorHandler.USER_ALREADY_EXISTS.getHttpStatus()).body(new ApiErrorResponse(ErrorHandler.USER_ALREADY_EXISTS.getDefaultMessage(), ErrorHandler.USER_ALREADY_EXISTS.getCode()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
        logException(ex, request, ErrorHandler.USER_NOT_FOUND.getDefaultMessage(), ErrorHandler.USER_NOT_FOUND.getCode());

        return ResponseEntity.status(ErrorHandler.USER_NOT_FOUND.getHttpStatus()).body(new ApiErrorResponse(ErrorHandler.USER_NOT_FOUND.getDefaultMessage(), ErrorHandler.USER_NOT_FOUND.getCode()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        logException(ex, request, ErrorHandler.INVALID_CREDENTIALS.getDefaultMessage(), ErrorHandler.INVALID_CREDENTIALS.getCode());

        return ResponseEntity.status(ErrorHandler.INVALID_CREDENTIALS.getHttpStatus()).body(new ApiErrorResponse(ErrorHandler.INVALID_CREDENTIALS.getDefaultMessage(), ErrorHandler.INVALID_CREDENTIALS.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        // Convert stack trace into a string.
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        // Convert stack trace string into base64
        String base64 = Base64.getEncoder().encodeToString(stackTrace.getBytes());

        logException(ex, request, ErrorHandler.INTERNAL_ERROR.getDefaultMessage(), ErrorHandler.INTERNAL_ERROR.getCode());

        return ResponseEntity.status(ErrorHandler.INTERNAL_ERROR.getHttpStatus()).body(new ApiErrorResponse(ErrorHandler.INTERNAL_ERROR.getDefaultMessage(), ErrorHandler.INTERNAL_ERROR.getCode(), base64));
    }

    /**
     * Logs the exception with relevant information such as the request path, IP address, user agent, and stack trace. This method is called from each exception handler to ensure consistent logging of all exceptions.
     *
     * @param ex           The exception to be logged
     * @param request      The HTTP request that caused the exception
     * @param errorMessage A detailed message about the error, which will be included in the log
     * @param errorCode    A custom error code for the specific error, which will be included in the log
     */
    private void logException(Exception ex, HttpServletRequest request, String errorMessage, String errorCode) {
        log.error("""
                        An exception occurred while processing the request:
                        ErrorCode: {}
                        Message: {}
                        Exception: {}
                        Request: {}
                        IP: {}
                        User-Agent: {}
                        Stacktrace: {}
                        """,
                errorCode,
                errorMessage,
                ex.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                ex
        );
    }
}
