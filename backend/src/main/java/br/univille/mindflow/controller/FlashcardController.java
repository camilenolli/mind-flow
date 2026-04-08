package br.univille.mindflow.controller;

import br.univille.mindflow.dto.FlashcardDTO;
import br.univille.mindflow.security.UserPrincipal;
import br.univille.mindflow.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@Tag(name = "Flashcards", description = "Revisão — fase SECI: Internalização (MVP: aleatório, futuro: SM-2)")
public class FlashcardController {

    private final FlashcardService service;

    public FlashcardController(FlashcardService service) { this.service = service; }

    @GetMapping
    public List<FlashcardDTO> list(@AuthenticationPrincipal UserPrincipal me,
                                   @RequestParam(required = false) Long focusProfileId) {
        return focusProfileId != null
                ? service.listByFocusProfile(me.getUser(), focusProfileId)
                : service.listAll(me.getUser());
    }

    @GetMapping("/draw")
    @Operation(summary = "Sorteia um flashcard aleatório (MVP). Filtra por perfil ativo se informado.")
    public ResponseEntity<FlashcardDTO> draw(@AuthenticationPrincipal UserPrincipal me,
                                             @RequestParam(required = false) Long focusProfileId) {
        return service.drawRandom(me.getUser(), focusProfileId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<FlashcardDTO> create(@AuthenticationPrincipal UserPrincipal me,
                                               @Valid @RequestBody FlashcardDTO dto) {
        return ResponseEntity.ok(service.create(me.getUser(), dto));
    }

    @PutMapping("/{id}")
    public FlashcardDTO update(@AuthenticationPrincipal UserPrincipal me,
                               @PathVariable Long id, @Valid @RequestBody FlashcardDTO dto) {
        return service.update(me.getUser(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        service.delete(me.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
