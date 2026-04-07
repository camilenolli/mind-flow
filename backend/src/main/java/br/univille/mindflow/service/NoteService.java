package br.univille.mindflow.service;

import br.univille.mindflow.dto.NoteDTO;
import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.model.Note;
import br.univille.mindflow.model.Tag;
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

    public List<NoteDTO> listAll() {
        return noteRepo.findAll().stream().map(this::toDTO).toList();
    }

    public List<NoteDTO> listRecent() {
        return noteRepo.findTop5ByOrderByUpdatedAtDesc().stream().map(this::toDTO).toList();
    }

    public List<NoteDTO> listByFocusProfile(Long profileId) {
        return noteRepo.findByFocusProfileId(profileId).stream().map(this::toDTO).toList();
    }

    public NoteDTO get(Long id) {
        return toDTO(noteRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Nota não encontrada: " + id)));
    }

    public List<NoteDTO> related(Long id) {
        return noteRepo.findRelatedByTags(id).stream().map(this::toDTO).toList();
    }

    public NoteDTO create(NoteDTO dto) {
        Note note = new Note();
        applyDTO(note, dto);
        return toDTO(noteRepo.save(note));
    }

    public NoteDTO update(Long id, NoteDTO dto) {
        Note note = noteRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Nota não encontrada: " + id));
        applyDTO(note, dto);
        return toDTO(noteRepo.save(note));
    }

    public void delete(Long id) { noteRepo.deleteById(id); }

    private void applyDTO(Note note, NoteDTO dto) {
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setTags(resolveTags(dto.getTags()));
        if (dto.getFocusProfileId() != null) {
            FocusProfile fp = profileRepo.findById(dto.getFocusProfileId())
                    .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado"));
            note.setFocusProfile(fp);
        } else {
            note.setFocusProfile(null);
        }
    }

    /** Resolve nomes de tag em entidades, criando as que não existem (case-insensitive). */
    private Set<Tag> resolveTags(Set<String> names) {
        if (names == null) return new HashSet<>();
        return names.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> tagRepo.findByNameIgnoreCase(name).orElseGet(() -> tagRepo.save(new Tag(name))))
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
