package br.univille.mindflow.controller;

import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.security.UserPrincipal;
import br.univille.mindflow.service.FocusProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/focus-profiles")
@Tag(name = "Modo Foco", description = "Perfis de contexto e horário")
public class FocusProfileController {

    private final FocusProfileService service;

    public FocusProfileController(FocusProfileService service) { this.service = service; }

    @GetMapping
    public List<FocusProfile> list(@AuthenticationPrincipal UserPrincipal me) {
        return service.listAll(me.getUser());
    }

    @PostMapping
    public FocusProfile create(@AuthenticationPrincipal UserPrincipal me,
                               @Valid @RequestBody FocusProfile p) {
        return service.create(me.getUser(), p);
    }

    @PutMapping("/{id}")
    public FocusProfile update(@AuthenticationPrincipal UserPrincipal me,
                               @PathVariable Long id, @Valid @RequestBody FocusProfile p) {
        return service.update(me.getUser(), id, p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        service.delete(me.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
