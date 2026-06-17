package br.univille.mindflow.controller;

import br.univille.mindflow.security.UserPrincipal;
import br.univille.mindflow.service.TagSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class TagSuggestionController {

    private final TagSuggestionService service;

    public TagSuggestionController(TagSuggestionService service) {
        this.service = service;
    }

    @PostMapping("/suggest-tags")
    public ResponseEntity<?> suggestTags(
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal UserPrincipal me
    ) {
        if (!service.isConfigured()) {
            return ResponseEntity.status(503).body(
                Map.of("error", "Sugestão de tags não disponível: ANTHROPIC_API_KEY não configurada.")
            );
        }
        try {
            List<String> tags = service.suggest(
                body.getOrDefault("title", ""),
                body.getOrDefault("content", "")
            );
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(
                Map.of("error", "Falha ao consultar IA: " + e.getMessage())
            );
        }
    }
}
