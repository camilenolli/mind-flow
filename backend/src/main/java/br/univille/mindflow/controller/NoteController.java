package br.univille.mindflow.controller;

import br.univille.mindflow.dto.NoteDTO;
import br.univille.mindflow.security.UserPrincipal;
import br.univille.mindflow.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notas", description = "Anotação Estruturada — fases SECI: Externalização e Combinação")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista notas do usuário. Filtra por focusProfileId quando informado.")
    public List<NoteDTO> list(@AuthenticationPrincipal UserPrincipal me,
                              @RequestParam(required = false) Long focusProfileId) {
        return focusProfileId != null
                ? service.listByFocusProfile(me.getUser(), focusProfileId)
                : service.listAll(me.getUser());
    }

    @GetMapping("/recent")
    @Operation(summary = "Top 5 notas mais recentemente atualizadas")
    public List<NoteDTO> recent(@AuthenticationPrincipal UserPrincipal me) {
        return service.listRecent(me.getUser());
    }

    @GetMapping("/{id}")
    public NoteDTO get(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return service.get(me.getUser(), id);
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Conceitos relacionados — notas que compartilham tags com esta")
    public List<NoteDTO> related(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        return service.related(me.getUser(), id);
    }

    @PostMapping
    public ResponseEntity<NoteDTO> create(@AuthenticationPrincipal UserPrincipal me,
                                          @Valid @RequestBody NoteDTO dto) {
        return ResponseEntity.ok(service.create(me.getUser(), dto));
    }

    @PutMapping("/{id}")
    public NoteDTO update(@AuthenticationPrincipal UserPrincipal me,
                          @PathVariable Long id, @Valid @RequestBody NoteDTO dto) {
        return service.update(me.getUser(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long id) {
        service.delete(me.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
