package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.TeamService;
import com.pawsclaws.petshop.web.dto.TeamMemberDto;
import com.pawsclaws.petshop.web.dto.TeamMemberRequest;
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
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<TeamMemberDto> list() {
        return teamService.list();
    }

    @PostMapping
    public ResponseEntity<TeamMemberDto> create(@Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
    }

    @PutMapping("/{id}")
    public TeamMemberDto update(@PathVariable Long id, @Valid @RequestBody TeamMemberRequest request) {
        return teamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
