package edu.teleinformatics.core.auth.controller;

import edu.teleinformatics.core.auth.dto.AuthComplete;
import edu.teleinformatics.core.auth.dto.CreateUser;
import edu.teleinformatics.core.auth.dto.LoginUser;
import edu.teleinformatics.core.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthComplete> createUser(@RequestBody CreateUser createUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(createUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthComplete> loginUser(@RequestBody LoginUser loginUser) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginUser));
    }
}
