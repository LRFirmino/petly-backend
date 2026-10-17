package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.UserAdminService;
import com.pawsclaws.petshop.web.dto.UserCreateRequest;
import com.pawsclaws.petshop.web.dto.UserDto;
import com.pawsclaws.petshop.web.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestão de usuários do petshop. Acesso restrito a administradores
 * (ROLE_ADMIN) — ver SecurityConfig.
 */
@RestController
@RequestMapping("/api/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public List<UserDto> list() {
        return userAdminService.list();
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.create(request));
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userAdminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
