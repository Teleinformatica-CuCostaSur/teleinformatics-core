package edu.teleinformatics.core.db.user.exception;

public class RoleNotFoundException extends RuntimeException {
  public RoleNotFoundException(String message) {
    super(message);
  }
}
