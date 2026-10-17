package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.ClientService;
import com.pawsclaws.petshop.web.dto.ClientDto;
import com.pawsclaws.petshop.web.dto.ClientRequest;
import com.pawsclaws.petshop.web.dto.ClientSummary;
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
 * Clients of the authenticated petshop only. Ids belonging to another petshop
 * are treated as not found.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientDto> list() {
        return clientService.findAll();
    }

    @GetMapping("/summary")
    public ClientSummary summary() {
        return clientService.summary();
    }

    @GetMapping("/{id}")
    public ClientDto get(@PathVariable Long id) {
        return clientService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @PutMapping("/{id}")
    public ClientDto update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
