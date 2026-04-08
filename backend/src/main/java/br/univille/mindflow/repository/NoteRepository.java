package br.univille.mindflow.repository;

import br.univille.mindflow.model.Note;
import br.univille.mindflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserOrderByUpdatedAtDesc(User user);

    List<Note> findByUserAndFocusProfileId(User user, Long focusProfileId);

    List<Note> findTop5ByUserOrderByUpdatedAtDesc(User user);

    Optional<Note> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    /**
     * Conceitos relacionados — notas do mesmo usuário que compartilham
     * ao menos uma tag com a nota dada, excluindo a própria.
     */
    @Query("""
            SELECT n FROM Note n JOIN n.tags t
            WHERE t IN (SELECT t2 FROM Note n2 JOIN n2.tags t2 WHERE n2.id = :noteId)
              AND n.id <> :noteId
              AND n.user = :user
            GROUP BY n
            ORDER BY COUNT(t) DESC
            """)
    List<Note> findRelatedByTags(@Param("noteId") Long noteId, @Param("user") User user);
}
