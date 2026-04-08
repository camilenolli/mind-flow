package br.univille.mindflow.repository;

import br.univille.mindflow.model.FocusProfile;
import br.univille.mindflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FocusProfileRepository extends JpaRepository<FocusProfile, Long> {
    List<FocusProfile> findByUser(User user);
    Optional<FocusProfile> findByIdAndUser(Long id, User user);
    long countByUser(User user);
}
