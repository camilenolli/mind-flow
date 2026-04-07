package br.univille.mindflow.service;

import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.repository.FocusProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FocusProfileService {

    private final FocusProfileRepository repo;

    public FocusProfileService(FocusProfileRepository repo) { this.repo = repo; }

    public List<FocusProfile> listAll() { return repo.findAll(); }

    public FocusProfile save(FocusProfile p) { return repo.save(p); }

    public void delete(Long id) { repo.deleteById(id); }

    /** Retorna o perfil cuja janela contém o horário atual (primeiro encontrado). */
    public Optional<FocusProfile> activeNow() {
        LocalTime now = LocalTime.now();
        return repo.findAll().stream().filter(p -> p.isActiveAt(now)).findFirst();
    }
}
