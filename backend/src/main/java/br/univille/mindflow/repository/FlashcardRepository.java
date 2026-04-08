package br.univille.mindflow.repository;

import br.univille.mindflow.model.Flashcard;
import br.univille.mindflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByUser(User user);
    List<Flashcard> findByUserAndFocusProfileId(User user, Long focusProfileId);
    Optional<Flashcard> findByIdAndUser(Long id, User user);
    long countByUser(User user);
}
