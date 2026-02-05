package edu.teleinformatics.core.user.repository;

import edu.teleinformatics.core.user.entity.Role;
import edu.teleinformatics.core.user.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    public Optional<Role> findByName(RoleEnum name);
}
