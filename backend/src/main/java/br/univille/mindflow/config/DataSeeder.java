package br.univille.mindflow.config;

import br.univille.mindflow.dto.FlashcardDTO;
import br.univille.mindflow.dto.NoteDTO;
import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.model.User;
import br.univille.mindflow.repository.FocusProfileRepository;
import br.univille.mindflow.repository.NoteRepository;
import br.univille.mindflow.repository.UserRepository;
import br.univille.mindflow.service.FlashcardService;
import br.univille.mindflow.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Popula o banco com um usuário demo + dados de exemplo no perfil dev (H2).
 *
 * Credenciais:    demo@mindflow.com / demo123
 *
 * Serve dois propósitos:
 *  1) Permite que o professor / avaliador entre no sistema sem precisar
 *     criar conta — basta clicar no botão "Entrar como demo" da tela de login.
 *  2) Garante que após cada cold start do Render (que reseta o H2),
 *     a conta demo é restaurada automaticamente.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public static final String DEMO_EMAIL = "demo@mindflow.com";
    public static final String DEMO_PASSWORD = "demo123";

    private final UserRepository userRepo;
    private final NoteRepository noteRepo;
    private final FocusProfileRepository profileRepo;
    private final NoteService noteService;
    private final FlashcardService flashcardService;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository userRepo,
                      NoteRepository noteRepo,
                      FocusProfileRepository profileRepo,
                      NoteService noteService,
                      FlashcardService flashcardService,
                      PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.noteRepo = noteRepo;
        this.profileRepo = profileRepo;
        this.noteService = noteService;
        this.flashcardService = flashcardService;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.existsByEmailIgnoreCase(DEMO_EMAIL)) {
            log.info("[DataSeeder] Usuário demo já existe — pulando seed");
            return;
        }

        User demo = new User();
        demo.setEmail(DEMO_EMAIL);
        demo.setName("Usuário Demo");
        demo.setPasswordHash(encoder.encode(DEMO_PASSWORD));
        demo = userRepo.save(demo);

        log.info("[DataSeeder] Usuário demo criado: {} (senha: {})", DEMO_EMAIL, DEMO_PASSWORD);

        // Perfis de foco do demo
        FocusProfile trabalho = new FocusProfile();
        trabalho.setName("Trabalho");
        trabalho.setStartTime(LocalTime.of(8, 0));
        trabalho.setEndTime(LocalTime.of(18, 0));
        trabalho.setUser(demo);
        trabalho = profileRepo.save(trabalho);

        FocusProfile faculdade = new FocusProfile();
        faculdade.setName("Faculdade");
        faculdade.setStartTime(LocalTime.of(19, 0));
        faculdade.setEndTime(LocalTime.of(22, 0));
        faculdade.setUser(demo);
        faculdade = profileRepo.save(faculdade);

        // Notas do demo (passa pelo NoteService que cria tags per-user automaticamente)
        NoteDTO n1 = new NoteDTO();
        n1.setTitle("DNS — resolução de nomes");
        n1.setContent("DNS traduz nomes de domínio em endereços IP. Hierárquico, distribuído e cacheado.");
        n1.setTags(setOf("DNS", "Redes"));
        n1.setFocusProfileId(trabalho.getId());
        NoteDTO saved1 = noteService.create(demo, n1);

        NoteDTO n2 = new NoteDTO();
        n2.setTitle("DNSSEC");
        n2.setContent("Extensão de segurança do DNS. Assina respostas para evitar cache poisoning.");
        n2.setTags(setOf("DNS", "Segurança", "Redes"));
        n2.setFocusProfileId(trabalho.getId());
        noteService.create(demo, n2);

        NoteDTO n3 = new NoteDTO();
        n3.setTitle("Modelo SECI de Nonaka");
        n3.setContent("Socialização, Externalização, Combinação, Internalização — espiral do conhecimento.");
        n3.setTags(setOf("SECI"));
        n3.setFocusProfileId(faculdade.getId());
        NoteDTO saved3 = noteService.create(demo, n3);

        // Flashcards do demo
        FlashcardDTO f1 = new FlashcardDTO();
        f1.setQuestion("O que o DNS faz?");
        f1.setAnswer("Traduz nomes de domínio em endereços IP.");
        f1.setNoteId(saved1.getId());
        f1.setFocusProfileId(trabalho.getId());
        flashcardService.create(demo, f1);

        FlashcardDTO f2 = new FlashcardDTO();
        f2.setQuestion("Quais as fases do SECI?");
        f2.setAnswer("Socialização, Externalização, Combinação, Internalização.");
        f2.setNoteId(saved3.getId());
        f2.setFocusProfileId(faculdade.getId());
        flashcardService.create(demo, f2);

        log.info("[DataSeeder] Seed concluído: {} notas, {} perfis, {} flashcards",
                noteRepo.count(), profileRepo.count(), 2);
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(java.util.Arrays.asList(values));
    }
}
