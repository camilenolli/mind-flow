package br.univille.mindflow.repository;

import br.univille.mindflow.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByFocusProfileId(Long focusProfileId);
}
