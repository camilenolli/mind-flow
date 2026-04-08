package br.univille.mindflow.controller;

import br.univille.mindflow.repository.FlashcardRepository;
import br.univille.mindflow.repository.FocusProfileRepository;
import br.univille.mindflow.repository.NoteRepository;
import br.univille.mindflow.repository.TagRepository;
import br.univille.mindflow.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Estatísticas", description = "Contadores agregados do usuário autenticado")
public class StatsController {

    private final NoteRepository notes;
    private final FlashcardRepository cards;
    private final TagRepository tags;
    private final FocusProfileRepository profiles;

    public StatsController(NoteRepository notes, FlashcardRepository cards,
                           TagRepository tags, FocusProfileRepository profiles) {
        this.notes = notes;
        this.cards = cards;
        this.tags = tags;
        this.profiles = profiles;
    }

    @GetMapping
    public Map<String, Long> overview(@AuthenticationPrincipal UserPrincipal me) {
        var u = me.getUser();
        return Map.of(
            "notes",      notes.countByUser(u),
            "flashcards", cards.countByUser(u),
            "tags",       tags.countByUser(u),
            "profiles",   profiles.countByUser(u)
        );
    }
}
