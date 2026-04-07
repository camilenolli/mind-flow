package br.univille.mindflow.config;

import br.univille.mindflow.dto.FlashcardDTO;
import br.univille.mindflow.dto.NoteDTO;
import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.repository.FocusProfileRepository;
import br.univille.mindflow.repository.NoteRepository;
import br.univille.mindflow.service.FlashcardService;
import br.univille.mindflow.service.NoteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Popula o banco com exemplos no perfil dev (H2).
 *
 * Usa NoteService/FlashcardService (já transacionais) para evitar o problema
 * de "detached entity" que ocorreria se chamássemos repositórios diretamente
 * a partir de um CommandLineRunner sem transação ativa.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final NoteRepository noteRepo;
    private final FocusProfileRepository profileRepo;
    private final NoteService noteService;
    private final FlashcardService flashcardService;

    public DataSeeder(NoteRepository noteRepo,
                      FocusProfileRepository profileRepo,
                      NoteService noteService,
                      FlashcardService flashcardService) {
        this.noteRepo = noteRepo;
        this.profileRepo = profileRepo;
        this.noteService = noteService;
        this.flashcardService = flashcardService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (noteRepo.count() > 0) return;

        FocusProfile trabalho = new FocusProfile();
        trabalho.setName("Trabalho");
        trabalho.setStartTime(LocalTime.of(8, 0));
        trabalho.setEndTime(LocalTime.of(18, 0));
        trabalho = profileRepo.save(trabalho);

        FocusProfile faculdade = new FocusProfile();
        faculdade.setName("Faculdade");
        faculdade.setStartTime(LocalTime.of(19, 0));
        faculdade.setEndTime(LocalTime.of(22, 0));
        faculdade = profileRepo.save(faculdade);

        NoteDTO n1 = new NoteDTO();
        n1.setTitle("DNS — resolução de nomes");
        n1.setContent("DNS traduz nomes de domínio em endereços IP. Hierárquico, distribuído e cacheado.");
        n1.setTags(setOf("DNS", "Redes"));
        n1.setFocusProfileId(trabalho.getId());
        NoteDTO saved1 = noteService.create(n1);

        NoteDTO n2 = new NoteDTO();
        n2.setTitle("DNSSEC");
        n2.setContent("Extensão de segurança do DNS. Assina respostas para evitar cache poisoning.");
        n2.setTags(setOf("DNS", "Segurança", "Redes"));
        n2.setFocusProfileId(trabalho.getId());
        noteService.create(n2);

        NoteDTO n3 = new NoteDTO();
        n3.setTitle("Modelo SECI de Nonaka");
        n3.setContent("Socialização, Externalização, Combinação, Internalização — espiral do conhecimento.");
        n3.setTags(setOf("SECI"));
        n3.setFocusProfileId(faculdade.getId());
        NoteDTO saved3 = noteService.create(n3);

        FlashcardDTO f1 = new FlashcardDTO();
        f1.setQuestion("O que o DNS faz?");
        f1.setAnswer("Traduz nomes de domínio em endereços IP.");
        f1.setNoteId(saved1.getId());
        f1.setFocusProfileId(trabalho.getId());
        flashcardService.create(f1);

        FlashcardDTO f2 = new FlashcardDTO();
        f2.setQuestion("Quais as fases do SECI?");
        f2.setAnswer("Socialização, Externalização, Combinação, Internalização.");
        f2.setNoteId(saved3.getId());
        f2.setFocusProfileId(faculdade.getId());
        flashcardService.create(f2);
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(java.util.Arrays.asList(values));
    }
}
