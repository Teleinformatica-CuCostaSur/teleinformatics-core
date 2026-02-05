package edu.teleinformatics.core.auth.dto;

import java.util.UUID;

public record AuthComplete(UUID id, String jwt) {
}
