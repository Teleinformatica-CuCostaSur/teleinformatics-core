package edu.teleinformatics.core.auth.exception;


import org.springframework.security.core.AuthenticationException;

public class JwtInvalidException extends AuthenticationException {
  public JwtInvalidException(String message) {
    super(message);
  }
  public JwtInvalidException(String message, Throwable cause) {
    super(message, cause);
  }
}
