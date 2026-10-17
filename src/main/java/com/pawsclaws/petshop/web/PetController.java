package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.PetService;
import com.pawsclaws.petshop.web.dto.PetDto;
import com.pawsclaws.petshop.web.dto.PetRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public List<PetDto> list(@RequestParam(required = false) Long clientId) {
        return clientId == null ? petService.findAll() : petService.findByClient(clientId);
    }

    @GetMapping("/{id}")
    public PetDto get(@PathVariable Long id) {
        return petService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PetDto> create(@Valid @RequestBody PetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.create(request));
    }

    @PutMapping("/{id}")
    public PetDto update(@PathVariable Long id, @Valid @RequestBody PetRequest request) {
        return petService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
