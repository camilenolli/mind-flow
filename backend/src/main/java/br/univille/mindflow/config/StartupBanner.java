package br.univille.mindflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupBanner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);

    @Value("${server.port:8080}") private int port;
    @Value("${spring.profiles.active:default}") private String profile;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String base = "http://localhost:" + port;
        log.info("");
        log.info("==========================================================");
        log.info("  MindFlow rodando — perfil: {}", profile);
        log.info("");
        log.info("  >>> Abra no navegador: {}", base);
        log.info("");
        log.info("  API base : {}/api", base);
        log.info("  Swagger  : {}/swagger-ui.html", base);
        if ("dev".equals(profile)) {
            log.info("  H2 console: {}/h2-console  (JDBC: jdbc:h2:mem:mindflow)", base);
        }
        log.info("==========================================================");
        log.info("");
    }
}
