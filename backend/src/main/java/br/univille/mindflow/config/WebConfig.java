package br.univille.mindflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS aberto para o frontend estático servido em outro host/porta durante o dev.
 * Usa allowedOriginPatterns (mais permissivo) em vez de allowedOrigins("*"),
 * que no Spring Boot 3 pode falhar em alguns navegadores quando combinado com
 * preflight + headers customizados.
 *
 * Em produção restringir aos domínios autorizados.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
