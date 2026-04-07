package br.univille.mindflow.controller;

import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.service.FocusProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/focus-profiles")
@Tag(name = "Modo Foco", description = "Perfis de contexto e horário")
public class FocusProfileController {

    private final FocusProfileService service;

    public FocusProfileController(FocusProfileService service) { this.service = service; }

    @GetMapping
    public List<FocusProfile> list() { return service.listAll(); }

    @GetMapping("/active")
    @Operation(summary = "Retorna o perfil ativo no horário atual, ou 204 se nenhum")
    public ResponseEntity<FocusProfile> active() {
        return service.activeNow().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public FocusProfile create(@Valid @RequestBody FocusProfile p) {
        p.setId(null);
        return service.save(p);
    }

    @PutMapping("/{id}")
    public FocusProfile update(@PathVariable Long id, @Valid @RequestBody FocusProfile p) {
        p.setId(id);
        return service.save(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
