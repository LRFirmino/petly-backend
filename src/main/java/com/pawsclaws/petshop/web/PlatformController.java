package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.PlatformService;
import com.pawsclaws.petshop.web.dto.CreatePetshopRequest;
import com.pawsclaws.petshop.web.dto.PlatformPetshopDto;
import com.pawsclaws.petshop.web.dto.UserCreateRequest;
import com.pawsclaws.petshop.web.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Área do administrador da plataforma (equipe Petly). Restrito a
 * ROLE_PLATFORM_ADMIN — ver SecurityConfig.
 */
@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/petshops")
    public List<PlatformPetshopDto> petshops() {
        return platformService.listPetshops();
    }

    @PostMapping("/petshops")
    public ResponseEntity<PlatformPetshopDto> createPetshop(@Valid @RequestBody CreatePetshopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformService.createPetshop(request));
    }

    @PutMapping("/petshops/{id}/active")
    public PlatformPetshopDto setActive(@PathVariable Long id, @RequestParam boolean active) {
        return platformService.setActive(id, active);
    }

    @GetMapping("/petshops/{id}/users")
    public List<UserDto> users(@PathVariable Long id) {
        return platformService.listUsers(id);
    }

    @PostMapping("/petshops/{id}/users")
    public ResponseEntity<UserDto> createUser(@PathVariable Long id,
                                              @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformService.createUser(id, request));
    }
}
