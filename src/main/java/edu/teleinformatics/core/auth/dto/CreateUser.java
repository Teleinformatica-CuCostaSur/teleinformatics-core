package edu.teleinformatics.core.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.teleinformatics.core.config.TrimStringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUser(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must be at most 100 characters long")
        @JsonDeserialize(using = TrimStringDeserializer.class)
        @Schema(description = "A valid email format", example = "user@alumnos.udg.mx")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
        @Pattern(regexp = "^\\S+(?:\\s+\\S+)*$", message = "Password cannot contain leading or trailing spaces")
        @Schema(description = "Password with between 6 and 255 characters long", example = "password123")
        String password) {
}
