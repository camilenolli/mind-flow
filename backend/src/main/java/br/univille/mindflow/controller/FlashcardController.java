package br.univille.mindflow.controller;

import br.univille.mindflow.dto.FlashcardDTO;
import br.univille.mindflow.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@Tag(name = "Flashcards", description = "Revisão — fase SECI: Internalização (MVP: aleatório, futuro: SM-2)")
public class FlashcardController {

    private final FlashcardService service;

    public FlashcardController(FlashcardService service) { this.service = service; }

    @GetMapping
    public List<FlashcardDTO> list(@RequestParam(required = false) Long focusProfileId) {
        return focusProfileId != null ? service.listByFocusProfile(focusProfileId) : service.listAll();
    }

    @GetMapping("/draw")
    @Operation(summary = "Sorteia um flashcard aleatório (MVP). Filtra por perfil ativo se informado.")
    public ResponseEntity<FlashcardDTO> draw(@RequestParam(required = false) Long focusProfileId) {
        return service.drawRandom(focusProfileId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<FlashcardDTO> create(@Valid @RequestBody FlashcardDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public FlashcardDTO update(@PathVariable Long id, @Valid @RequestBody FlashcardDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
