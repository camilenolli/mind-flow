package br.univille.mindflow.repository;

import br.univille.mindflow.model.Tag;
import br.univille.mindflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByUserAndNameIgnoreCase(User user, String name);
    List<Tag> findByUser(User user);
    long countByUser(User user);
}
