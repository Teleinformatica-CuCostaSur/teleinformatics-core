package edu.teleinformatics.core.auth.controller;

import edu.teleinformatics.core.auth.dto.AuthComplete;
import edu.teleinformatics.core.auth.dto.CreateUser;
import edu.teleinformatics.core.auth.dto.LoginUser;
import edu.teleinformatics.core.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthService authService; // Injecting the AuthService

    @PostMapping("/register") // Endpoint for user registration
    @Operation(summary = "Register a new user", description = "Create a new user account in the system") // Swagger documentation
    public ResponseEntity<AuthComplete> createUser(@Valid @RequestBody CreateUser createUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(createUser));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticate a user and return an authentication token") // Swagger documentation
    public ResponseEntity<AuthComplete> loginUser(@Valid @RequestBody LoginUser loginUser) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginUser));
    }
}
