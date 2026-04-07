package br.univille.mindflow.controller;

import br.univille.mindflow.repository.FlashcardRepository;
import br.univille.mindflow.repository.FocusProfileRepository;
import br.univille.mindflow.repository.NoteRepository;
import br.univille.mindflow.repository.TagRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Estatísticas", description = "Contadores agregados para o dashboard")
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
    public Map<String, Long> overview() {
        return Map.of(
            "notes", notes.count(),
            "flashcards", cards.count(),
            "tags", tags.count(),
            "profiles", profiles.count()
        );
    }
}
