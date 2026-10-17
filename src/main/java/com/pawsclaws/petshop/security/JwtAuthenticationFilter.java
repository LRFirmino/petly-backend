package com.pawsclaws.petshop.security;

import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Reads the {@code Authorization: Bearer <token>} header, validates the JWT and
 * populates both the {@link SecurityContextHolder} and the {@link TenantContext}.
 *
 * <p>The tenant id used for the request always comes from the verified token /
 * database user, never from the request body or query string.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            extractToken(request)
                    .flatMap(this::authenticateFromToken)
                    .ifPresent(principal -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal, null, principal.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        TenantContext.setPetshopId(principal.getPetshopId());
                    });

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isEmpty()) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    /**
     * Re-loads the user from the database so that deactivated accounts and
     * stale tenant assignments are rejected immediately. The JWT itself is
     * still verified (signature + expiration) before the subject is trusted.
     */
    private Optional<UserPrincipal> authenticateFromToken(String token) {
        try {
            Claims claims = jwtService.parse(token);
            Long userId = Long.valueOf(claims.getSubject());

            return userRepository.findById(userId)
                    .filter(AppUser::isActive)
                    .map(UserPrincipal::from);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
