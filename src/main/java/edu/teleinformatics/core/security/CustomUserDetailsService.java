package edu.teleinformatics.core.security;

import edu.teleinformatics.core.auth.dto.AuthUserProjection;
import edu.teleinformatics.core.auth.exception.AuthUserNotFoundException;
import edu.teleinformatics.core.user.entity.User;
import edu.teleinformatics.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws AuthUserNotFoundException {
        return userRepository
                .findAuthUserByEmail(email)
                .map(this::mapToCustomUserDetails)
                .orElseThrow(() -> {
                    log.debug("Could not find user with email: {}", email);
                    return new AuthUserNotFoundException("Invalid credentials");
                });
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) throws AuthUserNotFoundException {
        return userRepository
                .findAuthUserById(id)
                .map(this::mapToCustomUserDetails)
                .orElseThrow(() -> {
                    log.debug("Could not find user with id: {}", id);
                    return new AuthUserNotFoundException("Invalid credentials");
                });
    }

    private CustomUserDetails mapToCustomUserDetails(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRoles(),
                user.isEnabled()
        );
    }
}
