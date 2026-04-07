package br.univille.mindflow.repository;

import br.univille.mindflow.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByFocusProfileId(Long focusProfileId);

    List<Note> findTop5ByOrderByUpdatedAtDesc();

    /**
     * Notas relacionadas: compartilham ao menos uma tag com a nota dada,
     * excluindo a própria. Ordenadas pelo número de tags em comum (desc).
     */
    @Query("""
            SELECT n FROM Note n JOIN n.tags t
            WHERE t IN (SELECT t2 FROM Note n2 JOIN n2.tags t2 WHERE n2.id = :noteId)
              AND n.id <> :noteId
            GROUP BY n
            ORDER BY COUNT(t) DESC
            """)
    List<Note> findRelatedByTags(@Param("noteId") Long noteId);
}
