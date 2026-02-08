package edu.teleinformatics.core.db.repository;

import edu.teleinformatics.core.db.entity.Role;
import edu.teleinformatics.core.db.entity.RoleEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    public Optional<Role> findByName(@NotNull RoleEnum name);
}
