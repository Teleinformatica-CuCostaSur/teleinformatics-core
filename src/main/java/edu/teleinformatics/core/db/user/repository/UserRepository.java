package edu.teleinformatics.core.db.user.repository;

import edu.teleinformatics.core.db.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(@NotNull @NotBlank String email);

    Optional<User> findByEmail(@NotNull @NotBlank String email);

    Optional<User> findAuthUserById(@NotNull @NotBlank UUID id);
    Optional<User> findAuthUserByEmail(@NotNull @NotBlank String email);
}
