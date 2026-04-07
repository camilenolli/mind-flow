package br.univille.mindflow.repository;

import br.univille.mindflow.model.FocusProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusProfileRepository extends JpaRepository<FocusProfile, Long> {
}
