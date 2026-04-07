package br.univille.mindflow.controller;

import br.univille.mindflow.dto.NoteDTO;
import br.univille.mindflow.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notas", description = "Anotação Estruturada — fases SECI: Externalização e Combinação")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista notas. Filtra por focusProfileId quando informado.")
    public List<NoteDTO> list(@RequestParam(required = false) Long focusProfileId) {
        return focusProfileId != null ? service.listByFocusProfile(focusProfileId) : service.listAll();
    }

    @GetMapping("/recent")
    @Operation(summary = "Top 5 notas mais recentemente atualizadas (para o dashboard)")
    public List<NoteDTO> recent() { return service.listRecent(); }

    @GetMapping("/{id}")
    public NoteDTO get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/{id}/related")
    @Operation(summary = "Conceitos relacionados — notas que compartilham tags com esta")
    public List<NoteDTO> related(@PathVariable Long id) { return service.related(id); }

    @PostMapping
    public ResponseEntity<NoteDTO> create(@Valid @RequestBody NoteDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public NoteDTO update(@PathVariable Long id, @Valid @RequestBody NoteDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
