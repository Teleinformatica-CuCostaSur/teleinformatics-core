package edu.teleinformatics.core.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {
    ROLE_STUDENT("University student"),
    ROLE_TEACHER("Academic staff / Professor"),
    ROLE_COORDINATOR("Program coordinator"),
    ROLE_ADMIN("System administrator");

    private final String description;
}
