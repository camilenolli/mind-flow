package br.univille.mindflow.controller;

import br.univille.mindflow.model.Tag;
import br.univille.mindflow.repository.TagRepository;
import br.univille.mindflow.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagRepository repo;
    public TagController(TagRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Tag> list(@AuthenticationPrincipal UserPrincipal me) {
        return repo.findByUser(me.getUser());
    }
}
