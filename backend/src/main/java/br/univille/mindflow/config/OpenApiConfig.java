package br.univille.mindflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI mindFlowOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("MindFlow API")
                .version("0.1.0")
                .description("PKM acadêmico — Univille (SI e Gestão do Conhecimento). " +
                        "Equipe: Camile, Guilherme, Maria Fernanda e Raul.")
                .contact(new Contact().name("Equipe MindFlow")));
    }
}
