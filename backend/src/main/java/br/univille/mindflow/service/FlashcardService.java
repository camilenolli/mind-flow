package br.univille.mindflow.service;

import br.univille.mindflow.dto.FlashcardDTO;
import br.univille.mindflow.model.Flashcard;
import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.model.Note;
import br.univille.mindflow.model.User;
import br.univille.mindflow.repository.FlashcardRepository;
import br.univille.mindflow.repository.FocusProfileRepository;
import br.univille.mindflow.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * MVP: revisão por seleção aleatória. Sprint futura: SM-2 (repetição espaçada).
 * Todos os métodos são scoped por usuário.
 */
@Service
@Transactional
public class FlashcardService {

    private final FlashcardRepository repo;
    private final NoteRepository noteRepo;
    private final FocusProfileRepository profileRepo;
    private final Random random = new Random();

    public FlashcardService(FlashcardRepository repo, NoteRepository noteRepo, FocusProfileRepository profileRepo) {
        this.repo = repo;
        this.noteRepo = noteRepo;
        this.profileRepo = profileRepo;
    }

    public List<FlashcardDTO> listAll(User user) {
        return repo.findByUser(user).stream().map(this::toDTO).toList();
    }

    public List<FlashcardDTO> listByFocusProfile(User user, Long profileId) {
        return repo.findByUserAndFocusProfileId(user, profileId).stream().map(this::toDTO).toList();
    }

    public Optional<FlashcardDTO> drawRandom(User user, Long focusProfileId) {
        List<Flashcard> pool = (focusProfileId != null)
                ? repo.findByUserAndFocusProfileId(user, focusProfileId)
                : repo.findByUser(user);
        if (pool.isEmpty()) return Optional.empty();
        return Optional.of(toDTO(pool.get(random.nextInt(pool.size()))));
    }

    public FlashcardDTO create(User user, FlashcardDTO dto) {
        Flashcard f = new Flashcard();
        f.setUser(user);
        applyDTO(user, f, dto);
        return toDTO(repo.save(f));
    }

    public FlashcardDTO update(User user, Long id, FlashcardDTO dto) {
        Flashcard f = repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard não encontrado: " + id));
        applyDTO(user, f, dto);
        return toDTO(repo.save(f));
    }

    public void delete(User user, Long id) {
        Flashcard f = repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard não encontrado: " + id));
        repo.delete(f);
    }

    private void applyDTO(User user, Flashcard f, FlashcardDTO dto) {
        f.setQuestion(dto.getQuestion());
        f.setAnswer(dto.getAnswer());
        if (dto.getNoteId() != null) {
            Note n = noteRepo.findByIdAndUser(dto.getNoteId(), user)
                    .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada"));
            f.setNote(n);
        } else {
            f.setNote(null);
        }
        if (dto.getFocusProfileId() != null) {
            FocusProfile fp = profileRepo.findByIdAndUser(dto.getFocusProfileId(), user)
                    .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado"));
            f.setFocusProfile(fp);
        } else {
            f.setFocusProfile(null);
        }
    }

    private FlashcardDTO toDTO(Flashcard f) {
        FlashcardDTO d = new FlashcardDTO();
        d.setId(f.getId());
        d.setQuestion(f.getQuestion());
        d.setAnswer(f.getAnswer());
        d.setNoteId(f.getNote() != null ? f.getNote().getId() : null);
        d.setFocusProfileId(f.getFocusProfile() != null ? f.getFocusProfile().getId() : null);
        return d;
    }
}
