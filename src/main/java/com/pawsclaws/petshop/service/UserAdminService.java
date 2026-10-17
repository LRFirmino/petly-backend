package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.AppUser;
import com.pawsclaws.petshop.domain.Role;
import com.pawsclaws.petshop.exception.ConflictException;
import com.pawsclaws.petshop.exception.ForbiddenException;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.UserRepository;
import com.pawsclaws.petshop.security.SecurityUtils;
import com.pawsclaws.petshop.web.dto.UserCreateRequest;
import com.pawsclaws.petshop.web.dto.UserDto;
import com.pawsclaws.petshop.web.dto.UserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Gestão de contas de usuário do petshop, restrita a administradores
 * (ver SecurityConfig: /api/users/** exige ROLE_ADMIN).
 */
@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantService tenantService;

    public UserAdminService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            TenantService tenantService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        Long petshopId = tenantService.currentPetshopId();
        return userRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .map(UserAdminService::toDto)
                .toList();
    }

    @Transactional
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }

        AppUser user = new AppUser();
        user.setPetshop(tenantService.currentPetshopReference());
        user.setName(request.name().trim());
        user.setEmail(request.email().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(parseRole(request.role()));
        user.setActive(true);
        userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest request) {
        AppUser user = requireUser(id);
        boolean isSelf = user.getId().equals(SecurityUtils.currentUserId());

        if (isSelf && request.active() != null && !request.active()) {
            throw new ForbiddenException("Você não pode desativar a própria conta.");
        }

        user.setName(request.name().trim());
        if (request.role() != null && !request.role().isBlank()) {
            Role role = parseRole(request.role());
            if (isSelf && role != Role.ADMIN) {
                throw new ForbiddenException("Você não pode remover o seu próprio acesso de administrador.");
            }
            user.setRole(role);
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = requireUser(id);
        if (user.getId().equals(SecurityUtils.currentUserId())) {
            throw new ForbiddenException("Você não pode excluir a própria conta.");
        }
        userRepository.delete(user);
    }

    private AppUser requireUser(Long id) {
        return userRepository.findByIdAndPetshopId(id, tenantService.currentPetshopId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private static Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.STAFF;
        }
        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Perfil inválido. Use ADMIN ou STAFF.");
        }
    }

    private static UserDto toDto(AppUser user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt());
    }
}
