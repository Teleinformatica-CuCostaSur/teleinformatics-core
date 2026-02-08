package edu.teleinformatics.core.auth.dto;

import edu.teleinformatics.core.db.user.entity.Role;

import java.util.Set;
import java.util.UUID;

public record AuthUserProjection(UUID id, String email, String password, Set<Role> roles, boolean enabled) {
}
