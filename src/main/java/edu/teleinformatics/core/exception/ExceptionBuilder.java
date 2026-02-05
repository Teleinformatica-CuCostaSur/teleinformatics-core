package edu.teleinformatics.core.exception;

import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ExceptionBuilder {
    public static Map<String, Object> build(int status,
                                            String errorCode,
                                            String errorTitle,
                                            Object errorMessage,
                                            String path,
                                            HttpMethod method) {
        Map<String, Object> data = new HashMap<>();

        data.put("status", status); // Status code for the error
        data.put("error", errorCode); // Describing the error
        data.put("title", errorTitle); // Title describing the error
        data.put("message", errorMessage); // Detailed message about the error
        data.put("path", path); // Path of the request that caused the error
        data.put("method", method.toString()); // HTTP method used in the request
        data.put("timestamp", Instant.now().toString()); // Timestamp of the error

        return data;
    }

    private ExceptionBuilder() {
    }
}
