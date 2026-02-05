package edu.teleinformatics.core.security.jwt;

import edu.teleinformatics.core.auth.exception.JwtExpiredException;
import edu.teleinformatics.core.auth.exception.JwtInvalidException;
import edu.teleinformatics.core.security.CustomAuthenticationEntryPoint;
import edu.teleinformatics.core.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            UUID id = UUID.fromString(jwtService.extractClaim(jwt, "sub", String.class));

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserById(id);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("User authenticated: {}", id);
            }
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();

            log.warn("JWT expired for user: {}", e.getClaims().getSubject());

            customAuthenticationEntryPoint.commence(request, response, new JwtExpiredException("Jwt is expired", e));
            return;
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();

            log.warn("Invalid JWT. Reason: {} - {}", e.getClass(), e.getMessage());

            customAuthenticationEntryPoint.commence(request, response, new JwtInvalidException("Invalid JWT", e));
            return;
        }

        filterChain.doFilter(request, response);
    }
}