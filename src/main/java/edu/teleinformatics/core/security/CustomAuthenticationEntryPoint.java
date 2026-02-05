package edu.teleinformatics.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.teleinformatics.core.exception.ExceptionBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json"); // Sets response content type to JSON
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Set HTTP status to 401 (Unauthorized)

        // Use GlobalExceptionBuilder to format the error response
        Map<String, Object> data =
                ExceptionBuilder.build(
                        HttpServletResponse.SC_UNAUTHORIZED, // HTTP status code for unauthorized error
                        "AUTH.UNAUTHORIZED", // Error code
                        "Unauthorized", //Error title
                        authException.getMessage(), // Error message from the exception
                        request.getRequestURI(), // URI that was accessed
                        HttpMethod.valueOf(request.getMethod())); // HTTP method used (GET, POST, etc.)

        // Write the error response as a JSON object
        new ObjectMapper().writeValue(response.getOutputStream(), data);
    }
}