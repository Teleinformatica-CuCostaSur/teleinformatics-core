package edu.teleinformatics.core.auth.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthUserNotFoundException extends UsernameNotFoundException {
    public AuthUserNotFoundException(String message) {
        super(message);
    }
}
