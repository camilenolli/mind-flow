# MindFlow

Plataforma web de **PKM (Personal Knowledge Management)** voltada para estudantes e profissionais de TI. Projeto acadêmico da disciplina **Sistemas de Informação e Gestão do Conhecimento** — Univille.

> **Equipe:** Camile · Guilherme · Maria Fernanda · Raul

## Problema

Informações importantes se perdem entre anotações dispersas, arquivos soltos e ferramentas diferentes. O MindFlow centraliza conteúdos e conexões entre ideias, facilitando o acesso e a reutilização do conhecimento pessoal.

## Pilares (mapeados ao modelo SECI)

| Pilar | O que faz | Fase SECI |
|---|---|---|
| **Anotação Estruturada** | Notas com tags. Relação N:N entre nota↔tag conecta automaticamente conceitos que compartilham tags ("conceitos relacionados"). | Externalização (tácito → explícito) e Combinação (explícito → explícito) |
| **Flashcards** | Revisão do conhecimento registrado. MVP usa sorteio aleatório; sprints futuras adotam SM-2 (repetição espaçada). | Internalização (explícito → tácito) |
| **Modo Foco** | Perfis por contexto e horário (ex.: "Trabalho" 8h–18h, "Faculdade" 19h–22h). Filtra notas/flashcards do contexto ativo. | Suporte transversal |

## Stack

- **Backend:** Java 17 + Spring Boot 3 (REST), Spring Data JPA, Spring Mail, Spring Validation, springdoc-openapi (Swagger UI)
- **Frontend:** HTML + CSS + JavaScript puros, consumindo a API via `fetch`
- **Banco:** PostgreSQL (perfil `prod`), H2 em memória (perfil `dev`, default)
- **Documentação da API:** Swagger UI em `http://localhost:8080/swagger-ui.html`

## Estrutura do repositório

```
mind_flow/
├── backend/                 # Spring Boot
│   ├── pom.xml
│   └── src/main/java/br/univille/mindflow/
│       ├── MindFlowApplication.java
│       ├── config/          # CORS, OpenAPI, DataSeeder
│       ├── controller/      # REST controllers + ExceptionHandler
│       ├── dto/
│       ├── model/           # Note, Tag, Flashcard, FocusProfile
│       ├── repository/
│       └── service/         # NoteService, FlashcardService, FocusProfileService
├── frontend/                # HTML/CSS/JS puro · mobile-first · glassmorphism
│   ├── index.html           # Dashboard (stats + ação rápida + tags)
│   ├── notes.html           # Anotação Estruturada
│   ├── flashcards.html      # Revisão com flip 3D
│   ├── focus.html           # Perfis de contexto/horário
│   ├── css/style.css        # Design system completo
│   └── js/
│       ├── layout.js        # Header, bottom nav mobile, ícones SVG, toasts, modais
│       ├── api.js           # Cliente fetch + estado de perfil ativo (override manual)
│       ├── home.js  notes.js  flashcards.js  focus.js
├── db/schema.sql            # Schema PostgreSQL de referência
├── docs/adr/                # Architecture Decision Records
└── README.md
```

## Interface

A UI é mobile-first, com:

- **Dashboard** com stats animados, ação rápida (criar nota em 1 passo) e nuvem de tags
- **Glassmorphism**: painéis translúcidos sobre fundo com gradientes e grade
- **Bottom nav** no mobile (Início · Notas · Flashcards · Foco), nav superior no desktop
- **Toasts** não-bloqueantes em vez de `alert()`
- **Modais** de confirmação em vez de `confirm()`
- **Flashcards com flip 3D** na revisão
- **Busca client-side** instantânea nas notas (título, conteúdo, tags)
- **Override manual de perfil**: na tela de Foco, "Forçar este" fixa um perfil ignorando a janela horária; persiste em `localStorage`
- **Pílula de perfil ativo** no header com pulse animado quando há contexto em vigor
- **Animações**: fade-up nos cards, hover suave, contagem animada de stats
- Tipografia **Inter** via Google Fonts
- Ícones SVG inline (sem dependências externas)

## Como rodar (single-command)

O **frontend é servido pelo próprio Spring Boot** — basta um comando:

```bash
cd backend
mvn spring-boot:run
```

Abra **<http://localhost:8080/>** no navegador. Pronto: API + UI no mesmo lugar, sem CORS, sem servidor estático extra. O perfil `dev` já vem com dados de exemplo (DNS, DNSSEC, SECI, dois perfis de foco e dois flashcards).

- App      : <http://localhost:8080/>
- Swagger  : <http://localhost:8080/swagger-ui.html>
- H2 console: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:mindflow`)

> A pasta [`frontend/`](frontend/) também existe como cópia caso você queira servir o estático separadamente (ex.: `python -m http.server 5500`). Mas a forma recomendada é usar a versão dentro de [`backend/src/main/resources/static/`](backend/src/main/resources/static/).

### Perfil prod (PostgreSQL)

1. Crie o banco e o usuário:
   ```sql
   CREATE DATABASE mindflow;
   CREATE USER mindflow WITH PASSWORD 'mindflow';
   GRANT ALL PRIVILEGES ON DATABASE mindflow TO mindflow;
   ```
2. Rode com o perfil:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

### Diagnóstico — "Não foi possível contatar o backend"

Se o frontend mostrar esse toast, é uma das três coisas:

1. **Backend não está rodando.** Confira no terminal do `mvn spring-boot:run` se apareceu o banner `>>> Abra no navegador: http://localhost:8080`. Se não, há erro de compilação/Java — me manda o log.
2. **Java/Maven não instalados.** Verifique com `java -version` (precisa ser 17+) e `mvn -v`.
3. **Você abriu o HTML via duplo-clique.** A URL do navegador começa com `file:///`? Isso quebra o `fetch`. Use `http://localhost:8080/` (servido pelo Spring) em vez disso.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| `GET`  | `/api/stats` | Contadores agregados (notas, flashcards, tags, perfis) |
| `GET`  | `/api/notes` | Lista notas. `?focusProfileId=` filtra por perfil |
| `GET`  | `/api/notes/recent` | Top 5 notas mais recentes (dashboard) |
| `GET`  | `/api/notes/{id}/related` | Conceitos relacionados (compartilham tags) |
| `POST` | `/api/notes` | Cria nota (cria tags inexistentes automaticamente) |
| `PUT`  | `/api/notes/{id}` | Atualiza |
| `DELETE` | `/api/notes/{id}` | Remove |
| `GET`  | `/api/flashcards/draw` | Sorteia um flashcard aleatório |
| `POST` | `/api/flashcards` | Cria |
| `GET`  | `/api/focus-profiles/active` | Perfil ativo no horário atual |
| `GET/POST/PUT/DELETE` | `/api/focus-profiles[/{id}]` | CRUD de perfis |

## Deploy no Render (gratuito)

O repositório já contém [`Dockerfile`](Dockerfile) (multi-stage com `maven:3.9-eclipse-temurin-17` no build e `eclipse-temurin:17-jre` no runtime) e [`render.yaml`](render.yaml) declarativo.

Para subir um demo público:

1. Crie conta em <https://render.com> (sem cartão de crédito, login com GitHub)
2. **New +** → **Blueprint** → conecte o repositório `camilenolli/mind-flow`
3. Render lê o `render.yaml`, pré-preenche tudo, basta clicar **Apply**
4. Aguarde o primeiro build (~3 a 5 min). A URL pública aparece em verde no topo: `https://mindflow-XXXX.onrender.com`

A partir daí, todo `git push origin main` redeploya automaticamente.

**Sobre o free tier:** 512MB RAM, dorme após 15min sem tráfego. A primeira requisição depois de dormir leva ~30–60s para acordar (é o JVM subindo). Para a demo acadêmica, isso é aceitável — após acordar, a navegação é fluida. O `Dockerfile` já está otimizado para esse perfil de recursos (Serial GC, `TieredStopAtLevel=1`, `Xmx450m`).

**Persistência:** o deploy roda no perfil `dev` propositalmente (H2 em memória), então cada cold start repopula os dados de exemplo via `DataSeeder`. Para produção real com dados persistentes, troque para o perfil `prod` no `render.yaml` e provisione um Postgres externo (Neon, Supabase free tiers).

## Fora do escopo do MVP

Colaboração entre usuários, modo offline, app mobile nativo, integrações externas (Notion/Obsidian), exportação de dados, IA para conexão automática de conceitos, gamificação, histórico de versões.

## Organização da equipe

- **Divisão por camada técnica:** frontend / backend / banco
- **Raul** — integrador técnico
- **Camile** — atas das reuniões
- **Gestão:** GitHub Projects (Kanban)
- **Reuniões:** semanais, segundas-feiras
- **Decisões:** documentadas como ADRs em [`docs/adr/`](docs/adr/)
