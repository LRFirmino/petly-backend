package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.CatalogService;
import com.pawsclaws.petshop.web.dto.ComboDto;
import com.pawsclaws.petshop.web.dto.ComboRequest;
import com.pawsclaws.petshop.web.dto.ServiceDto;
import com.pawsclaws.petshop.web.dto.ServiceRequest;
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

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // ---- Serviços -----------------------------------------------------

    @GetMapping("/services")
    public List<ServiceDto> services() {
        return catalogService.listServices();
    }

    @PostMapping("/services")
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createService(request));
    }

    @PutMapping("/services/{id}")
    public ServiceDto updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return catalogService.updateService(id, request);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        catalogService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Combos -------------------------------------------------------

    @GetMapping("/combos")
    public List<ComboDto> combos() {
        return catalogService.listCombos();
    }

    @PostMapping("/combos")
    public ResponseEntity<ComboDto> createCombo(@Valid @RequestBody ComboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCombo(request));
    }

    @PutMapping("/combos/{id}")
    public ComboDto updateCombo(@PathVariable Long id, @Valid @RequestBody ComboRequest request) {
        return catalogService.updateCombo(id, request);
    }

    @DeleteMapping("/combos/{id}")
    public ResponseEntity<Void> deleteCombo(@PathVariable Long id) {
        catalogService.deleteCombo(id);
        return ResponseEntity.noContent().build();
    }
}
