package br.univille.mindflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class TagSuggestionService {

    private final String apiKey;
    private final RestClient restClient;

    public TagSuggestionService(@Value("${anthropic.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<String> suggest(String title, String content) {
        if (!isConfigured()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY não configurada no servidor.");
        }

        String truncated = content != null && content.length() > 600
                ? content.substring(0, 600) + "…"
                : (content != null ? content : "");

        String prompt = String.format(
            "Você é um assistente de gestão do conhecimento. Analise o título e conteúdo " +
            "da nota abaixo e sugira de 5 a 7 tags curtas e relevantes no mesmo idioma do conteúdo. " +
            "Retorne APENAS as tags separadas por vírgula, sem explicações, numeração ou pontuação extra.\n\n" +
            "Título: %s\nConteúdo: %s",
            title != null ? title : "", truncated
        );

        var requestBody = Map.of(
            "model", "claude-haiku-4-5-20251001",
            "max_tokens", 120,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
            .uri("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(Map.class);

        if (response == null) return List.of();

        @SuppressWarnings("unchecked")
        var blocks = (List<Map<String, Object>>) response.get("content");
        if (blocks == null || blocks.isEmpty()) return List.of();

        String text = (String) blocks.get(0).get("text");
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.split(","))
            .map(s -> s.trim().replaceAll("[.!?]$", ""))
            .filter(s -> !s.isEmpty())
            .limit(8)
            .toList();
    }
}
