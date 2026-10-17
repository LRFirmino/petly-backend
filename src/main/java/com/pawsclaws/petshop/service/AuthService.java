package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.auth.dto.AuthResponse;
import com.pawsclaws.petshop.auth.dto.LoginRequest;
import com.pawsclaws.petshop.auth.dto.PetshopSummary;
import com.pawsclaws.petshop.auth.dto.RegisterRequest;
import com.pawsclaws.petshop.auth.dto.UserSummary;
import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.domain.Petshop;
import com.pawsclaws.petshop.domain.Role;
import com.pawsclaws.petshop.exception.ConflictException;
import com.pawsclaws.petshop.repository.PetshopRepository;
import com.pawsclaws.petshop.repository.UserRepository;
import com.pawsclaws.petshop.security.JwtService;
import com.pawsclaws.petshop.security.SecurityUtils;
import com.pawsclaws.petshop.security.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PetshopRepository petshopRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PetshopRepository petshopRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.petshopRepository = petshopRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(AppUser::isActive)
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        return issueToken(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }

        Petshop petshop = new Petshop();
        petshop.setName(request.petshopName().trim());
        petshop.setSlug(uniqueSlug(request.petshopName()));
        petshop.setEmail(request.email().toLowerCase(Locale.ROOT));
        petshop.setActive(true);
        petshopRepository.save(petshop);

        AppUser user = new AppUser();
        user.setPetshop(petshop);
        user.setName(request.name().trim());
        user.setEmail(request.email().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ADMIN);
        user.setActive(true);
        userRepository.save(user);

        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public UserSummary currentUserSummary() {
        Long userId = SecurityUtils.currentUserId();
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado."));
        return toSummary(user);
    }

    private AuthResponse issueToken(AppUser user) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        return AuthResponse.of(token, jwtService.getExpirationMs(), toSummary(user));
    }

    private UserSummary toSummary(AppUser user) {
        Petshop petshop = user.getPetshop();
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                new PetshopSummary(petshop.getId(), petshop.getName(), petshop.getSlug()));
    }

    private String uniqueSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) {
            base = "petshop";
        }
        String candidate = base;
        while (petshopRepository.existsBySlug(candidate)) {
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return candidate;
    }
}
