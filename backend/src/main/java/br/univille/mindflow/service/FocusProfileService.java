package br.univille.mindflow.service;

import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.model.User;
import br.univille.mindflow.repository.FocusProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FocusProfileService {

    private final FocusProfileRepository repo;

    public FocusProfileService(FocusProfileRepository repo) { this.repo = repo; }

    public List<FocusProfile> listAll(User user) {
        return repo.findByUser(user);
    }

    public FocusProfile create(User user, FocusProfile p) {
        p.setId(null);
        p.setUser(user);
        return repo.save(p);
    }

    public FocusProfile update(User user, Long id, FocusProfile p) {
        FocusProfile existing = repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado"));
        existing.setName(p.getName());
        existing.setStartTime(p.getStartTime());
        existing.setEndTime(p.getEndTime());
        return repo.save(existing);
    }

    public void delete(User user, Long id) {
        FocusProfile existing = repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado"));
        repo.delete(existing);
    }
}
