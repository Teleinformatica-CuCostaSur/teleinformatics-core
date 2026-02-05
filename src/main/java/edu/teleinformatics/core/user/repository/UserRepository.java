package edu.teleinformatics.core.user.repository;

import edu.teleinformatics.core.auth.dto.AuthUserProjection;
import edu.teleinformatics.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findAuthUserById(UUID id);
    Optional<User> findAuthUserByEmail(String email);
}
