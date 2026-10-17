package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.PetshopSettingsService;
import com.pawsclaws.petshop.web.dto.PetshopSettingsDto;
import com.pawsclaws.petshop.web.dto.PetshopSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/petshop")
public class PetshopController {

    private final PetshopSettingsService settingsService;

    public PetshopController(PetshopSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public PetshopSettingsDto get() {
        return settingsService.get();
    }

    @PutMapping
    public PetshopSettingsDto update(@Valid @RequestBody PetshopSettingsRequest request) {
        return settingsService.update(request);
    }
}
