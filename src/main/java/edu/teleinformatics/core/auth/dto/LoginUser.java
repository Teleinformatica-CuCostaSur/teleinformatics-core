package edu.teleinformatics.core.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.teleinformatics.core.config.TrimStringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUser(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        @JsonDeserialize(using = TrimStringDeserializer.class)
        @Schema(example = "user@alumnos.udg.mx")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(max = 255, message = "Password is too long")
        @Schema(example = "password123")
        String password) {
}
