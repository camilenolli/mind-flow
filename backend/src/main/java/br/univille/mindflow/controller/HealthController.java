package br.univille.mindflow.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint público para healthcheck do Render. Não requer autenticação.
 * Substitui o uso de /api/stats (que agora é privado e scoped por usuário).
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Healthcheck público para load balancer / monitor")
public class HealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "mindflow");
    }
}
