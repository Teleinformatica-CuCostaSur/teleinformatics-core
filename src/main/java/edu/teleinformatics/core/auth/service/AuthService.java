package edu.teleinformatics.core.auth.service;

import edu.teleinformatics.core.auth.dto.AuthComplete;
import edu.teleinformatics.core.auth.dto.CreateUser;
import edu.teleinformatics.core.auth.dto.LoginUser;
import edu.teleinformatics.core.auth.exception.UserAlreadyExistsException;
import edu.teleinformatics.core.security.CustomUserDetails;
import edu.teleinformatics.core.security.jwt.JwtService;
import edu.teleinformatics.core.db.entity.Role;
import edu.teleinformatics.core.db.entity.RoleEnum;
import edu.teleinformatics.core.db.entity.User;
import edu.teleinformatics.core.db.exception.RoleNotFoundException;
import edu.teleinformatics.core.db.repository.RoleRepository;
import edu.teleinformatics.core.db.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthComplete createUser(CreateUser createUser) {
        String hashedPassword = passwordEncoder.encode(createUser.password());

        Role initialRole = roleRepository.findByName(RoleEnum.ROLE_STUDENT)
                .orElseThrow(() -> new RoleNotFoundException("Default role not found"));

        try {
            User user = userRepository.saveAndFlush(new User(createUser.email(), hashedPassword, initialRole));

            String jwt = jwtService.generateToken(user.getId(), user.getEmail(), List.of(initialRole.getName().name()));

            log.info("New user created. Id: {}", user.getId());

            return new AuthComplete(user.getId(), jwt);
        } catch (DataIntegrityViolationException e){
            throw new UserAlreadyExistsException("The email " + createUser.email() + " is already registered.");
        }
    }


    @Transactional(readOnly = true)
    public AuthComplete login(LoginUser loginUser) {
        var authToken = new UsernamePasswordAuthenticationToken(loginUser.email(), loginUser.password());
        CustomUserDetails userDetails = (CustomUserDetails) authenticationManager.authenticate(authToken).getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        String jwt = jwtService.generateToken(userDetails.getId(), userDetails.getUsername(), roles);

        log.info("User logged in. Id: {}", userDetails.getId());

        return new AuthComplete(userDetails.getId(), jwt);
    }
}
