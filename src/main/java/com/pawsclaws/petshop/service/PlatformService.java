package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.domain.Petshop;
import com.pawsclaws.petshop.domain.Role;
import com.pawsclaws.petshop.exception.ConflictException;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.ClientRepository;
import com.pawsclaws.petshop.repository.PetRepository;
import com.pawsclaws.petshop.repository.PetshopRepository;
import com.pawsclaws.petshop.repository.UserRepository;
import com.pawsclaws.petshop.web.dto.CreatePetshopRequest;
import com.pawsclaws.petshop.web.dto.PlatformPetshopDto;
import com.pawsclaws.petshop.web.dto.UserCreateRequest;
import com.pawsclaws.petshop.web.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Administração da plataforma (equipe Petly): cria petshops e contas.
 * Acesso restrito a PLATFORM_ADMIN (ver SecurityConfig).
 */
@Service
public class PlatformService {

    private final PetshopRepository petshopRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformService(PetshopRepository petshopRepository,
                           UserRepository userRepository,
                           ClientRepository clientRepository,
                           PetRepository petRepository,
                           AppointmentRepository appointmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.petshopRepository = petshopRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<PlatformPetshopDto> listPetshops() {
        return petshopRepository.findAllByPlatformFalseOrderByNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PlatformPetshopDto createPetshop(CreatePetshopRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.adminEmail())) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }

        Petshop petshop = new Petshop();
        petshop.setName(request.petshopName().trim());
        petshop.setSlug(uniqueSlug(request.petshopName()));
        petshop.setEmail(trimToNull(request.email()));
        petshop.setPhone(trimToNull(request.phone()));
        petshop.setAddress(trimToNull(request.address()));
        petshop.setPlatform(false);
        petshop.setActive(true);
        petshopRepository.save(petshop);

        AppUser admin = new AppUser();
        admin.setPetshop(petshop);
        admin.setName(request.adminName().trim());
        admin.setEmail(request.adminEmail().toLowerCase(Locale.ROOT));
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        return toDto(petshop);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers(Long petshopId) {
        requirePetshop(petshopId);
        return userRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .map(PlatformService::toUserDto)
                .toList();
    }

    @Transactional
    public UserDto createUser(Long petshopId, UserCreateRequest request) {
        Petshop petshop = requirePetshop(petshopId);
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }

        AppUser user = new AppUser();
        user.setPetshop(petshop);
        user.setName(request.name().trim());
        user.setEmail(request.email().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(parseRole(request.role()));
        user.setActive(true);
        userRepository.save(user);
        return toUserDto(user);
    }

    @Transactional
    public PlatformPetshopDto setActive(Long petshopId, boolean active) {
        Petshop petshop = requirePetshop(petshopId);
        petshop.setActive(active);
        petshopRepository.save(petshop);
        return toDto(petshop);
    }

    private Petshop requirePetshop(Long petshopId) {
        return petshopRepository.findById(petshopId)
                .filter(petshop -> !petshop.isPlatform())
                .orElseThrow(() -> new ResourceNotFoundException("Petshop não encontrado."));
    }

    private PlatformPetshopDto toDto(Petshop petshop) {
        Long id = petshop.getId();
        return new PlatformPetshopDto(
                id,
                petshop.getName(),
                petshop.getSlug(),
                petshop.getEmail(),
                petshop.getPhone(),
                petshop.getAddress(),
                petshop.isActive(),
                userRepository.countByPetshopId(id),
                clientRepository.countByPetshopId(id),
                petRepository.countByPetshopId(id),
                appointmentRepository.countByPetshopId(id));
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

    private static Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.STAFF;
        }
        try {
            Role parsed = Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
            return parsed == Role.PLATFORM_ADMIN ? Role.ADMIN : parsed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Perfil inválido. Use ADMIN ou STAFF.");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static UserDto toUserDto(AppUser user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt());
    }
}
