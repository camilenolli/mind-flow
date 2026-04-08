package br.univille.mindflow.service;

import br.univille.mindflow.dto.NoteDTO;
import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.model.Note;
import br.univille.mindflow.model.Tag;
import br.univille.mindflow.model.User;
import br.univille.mindflow.repository.FocusProfileRepository;
import br.univille.mindflow.repository.NoteRepository;
import br.univille.mindflow.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepo;
    private final TagRepository tagRepo;
    private final FocusProfileRepository profileRepo;

    public NoteService(NoteRepository noteRepo, TagRepository tagRepo, FocusProfileRepository profileRepo) {
        this.noteRepo = noteRepo;
        this.tagRepo = tagRepo;
        this.profileRepo = profileRepo;
    }

    public List<NoteDTO> listAll(User user) {
        return noteRepo.findByUserOrderByUpdatedAtDesc(user).stream().map(this::toDTO).toList();
    }

    public List<NoteDTO> listRecent(User user) {
        return noteRepo.findTop5ByUserOrderByUpdatedAtDesc(user).stream().map(this::toDTO).toList();
    }

    public List<NoteDTO> listByFocusProfile(User user, Long profileId) {
        return noteRepo.findByUserAndFocusProfileId(user, profileId).stream().map(this::toDTO).toList();
    }

    public NoteDTO get(User user, Long id) {
        return toDTO(noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada: " + id)));
    }

    public List<NoteDTO> related(User user, Long id) {
        // Garante que a nota pertence ao usuário antes de buscar relacionados
        noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada: " + id));
        return noteRepo.findRelatedByTags(id, user).stream().map(this::toDTO).toList();
    }

    public NoteDTO create(User user, NoteDTO dto) {
        Note note = new Note();
        note.setUser(user);
        applyDTO(user, note, dto);
        return toDTO(noteRepo.save(note));
    }

    public NoteDTO update(User user, Long id, NoteDTO dto) {
        Note note = noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada: " + id));
        applyDTO(user, note, dto);
        return toDTO(noteRepo.save(note));
    }

    public void delete(User user, Long id) {
        Note note = noteRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada: " + id));
        noteRepo.delete(note);
    }

    private void applyDTO(User user, Note note, NoteDTO dto) {
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setTags(resolveTags(user, dto.getTags()));
        if (dto.getFocusProfileId() != null) {
            FocusProfile fp = profileRepo.findByIdAndUser(dto.getFocusProfileId(), user)
                    .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado"));
            note.setFocusProfile(fp);
        } else {
            note.setFocusProfile(null);
        }
    }

    /** Resolve nomes de tag em entidades, criando as que não existem (case-insensitive) — sempre per-user. */
    private Set<Tag> resolveTags(User user, Set<String> names) {
        if (names == null) return new HashSet<>();
        return names.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> tagRepo.findByUserAndNameIgnoreCase(user, name)
                        .orElseGet(() -> tagRepo.save(new Tag(name, user))))
                .collect(Collectors.toSet());
    }

    NoteDTO toDTO(Note n) {
        NoteDTO d = new NoteDTO();
        d.setId(n.getId());
        d.setTitle(n.getTitle());
        d.setContent(n.getContent());
        d.setTags(n.getTags().stream().map(Tag::getName).collect(Collectors.toCollection(HashSet::new)));
        d.setFocusProfileId(n.getFocusProfile() != null ? n.getFocusProfile().getId() : null);
        d.setCreatedAt(n.getCreatedAt());
        d.setUpdatedAt(n.getUpdatedAt());
        return d;
    }
}
