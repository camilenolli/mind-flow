# ADR 0002 — Relação N:N entre Nota e Tag para "conceitos relacionados"

**Status:** Aceito
**Data:** 2026-04-06
**Decisores:** Equipe MindFlow

## Contexto

O pilar de Anotação Estruturada exige conectar automaticamente conceitos relacionados. Avaliamos três caminhos: (a) referência direta nota↔nota, (b) tags como N:N, (c) IA / embeddings.

## Decisão

Adotar **N:N nota↔tag** com tabela associativa `note_tag`. Duas notas são consideradas relacionadas se compartilham ao menos uma tag; a ordenação por número de tags em comum vira o ranking de "conceitos relacionados".

## Consequências

**Positivas**
- Modelagem trivial em JPA (`@ManyToMany`) e em SQL.
- Custo computacional baixo — basta uma query com `JOIN` + `GROUP BY`.
- Mantém o usuário no controle (tagueamento explícito = Externalização do SECI).

**Negativas**
- Qualidade das conexões depende da disciplina do usuário em taguear.
- Não captura proximidade semântica entre tags distintas (ex.: "DNS" vs "Resolução de Nomes"). Fica para sprint futura com IA, fora do MVP.
