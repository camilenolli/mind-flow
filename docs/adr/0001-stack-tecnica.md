# ADR 0001 — Stack técnica do MindFlow

**Status:** Aceito
**Data:** 2026-04-06
**Decisores:** Equipe MindFlow (Camile, Guilherme, Maria Fernanda, Raul)

## Contexto

Precisamos escolher uma stack acessível para a equipe (com base de Java na Univille), que permita entregar o MVP dentro do semestre da disciplina de SI e Gestão do Conhecimento, com documentação automática de API e banco relacional bem suportado.

## Decisão

- **Backend:** Java 17 + Spring Boot 3, expondo REST.
- **Persistência:** Spring Data JPA / Hibernate sobre **PostgreSQL** em produção; H2 em memória no perfil `dev` para reduzir atrito de setup.
- **Documentação de API:** springdoc-openapi (Swagger UI).
- **Frontend:** HTML + CSS + JavaScript puros consumindo a API via `fetch`. Sem framework SPA no MVP.

## Consequências

**Positivas**
- Reuso de conhecimento prévio da equipe em Java/Spring.
- JPA reduz boilerplate de SQL e modela naturalmente o N:N nota↔tag.
- Swagger entrega documentação executável sem custo extra.
- Frontend "puro" mantém o foco em fundamentos web e simplifica o deploy.

**Negativas / trade-offs**
- Sem framework no frontend, escalabilidade da UI é limitada — aceitável no MVP.
- H2 em dev pode mascarar diferenças de SQL com PostgreSQL — mitigamos rodando o perfil `prod` antes de cada entrega.
