<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-001 — Adotar Monolito Modular em vez de Microsserviços

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge)

**Data**: 2026-06-10
**Status**: Aceita
**Decisores**: Par 2 (Enterprise Architect + Software Architect), com revisão do Par 1 (Product Owner)

## Contexto

Estamos modernizando o SIFAP (29 anos, Natural/Adabas) para Java 21 + Spring Boot 3.3 + PostgreSQL 16. A arqueologia (Estágio 1) revelou dois fatos decisivos:

1. **Zero acoplamento por código:** nenhum dos 15 programas usa `CALLNAT` ou `INCLUDE`. A integração é toda **pelos dados** (4 DDMs Adabas compartilhados). Ver [`../01-arqueologia/dependency-map.md`](../01-arqueologia/dependency-map.md).
2. **Quatro domínios coesos por DDM:** Beneficiary, Social Program, Payment e Audit, formalizados em [`bounded-contexts.md`](bounded-contexts.md).

Restrições:

- **Time pequeno** (5 pares) e **janela curta** — não há tempo para service mesh, contratos versionados e observabilidade distribuída.
- **Acoplamento forte via Auditoria:** quase toda operação (inclusão, alteração, conciliação) gera evento de auditoria. Comunicação cross-service síncrona adicionaria latência sem benefício.
- **Carga batch noturna:** geração de ciclo processa milhões de beneficiários — exige processo previsível, não funções efêmeras.
- O contexto **Payment é hub de dados** (8 dos 15 programas tocam o DDM PAGAMENTO); separá-lo prematuramente multiplicaria chamadas de rede.

A decisão precisa equilibrar **clareza de domínio** (4 bounded contexts visíveis) com **simplicidade de operação** (1 deployable).

## Opções Consideradas

### Opção 1: Monolito Modular (package-by-feature)

- **Descrição**: 1 aplicação Spring Boot, 4 módulos (`beneficiary`, `socialprogram`, `payment`, `audit`), cada um com `domain/`, `application/`, `infrastructure/`. Fronteiras verificadas por ArchUnit no CI. Banco único PostgreSQL com schema por módulo.
- **Vantagens**: 1 pipeline e 1 deploy; latência inter-módulo zero (in-process); preserva os bounded contexts; refatoração futura para microsserviços é módulo a módulo.
- **Desvantagens**: escala "tudo ou nada"; um bug grave pode afetar o processo inteiro; não acomoda stack heterogênea por módulo.

### Opção 2: Microsserviços desde o dia 1

- **Descrição**: 4 serviços independentes, um por bounded context, comunicação via REST/mensageria.
- **Vantagens**: deploy e escala independentes por domínio.
- **Desvantagens**: não cabe na janela do workshop (service mesh, contratos OpenAPI, observabilidade distribuída, CI/CD por serviço); time sem skill operacional para 4 serviços; latência síncrona Payment↔Beneficiary↔Audit quebraria a meta p95 < 200 ms; transação de geração de ciclo viraria saga distribuída.

### Opção 3: Monolito tradicional (package-by-layer)

- **Descrição**: 1 aplicação organizada por camada técnica (`controllers/`, `services/`, `repositories/`), sem fronteiras de módulo.
- **Vantagens**: mais simples de começar.
- **Desvantagens**: dissolve os 4 bounded contexts — reproduz exatamente o anti-padrão do legado (regras espalhadas e duplicadas); refatoração futura para serviços fica proibitiva.

## Decisão

**Decidimos adotar um Monolito Modular (package-by-feature)** em Java 21 + Spring Boot 3.3, com 4 módulos alinhados aos bounded contexts:

```
src/main/java/br/gov/sifap/
├── beneficiary/      ├── domain/  ├── application/  └── infrastructure/
├── socialprogram/    ├── domain/  ├── application/  └── infrastructure/
├── payment/          ├── domain/  ├── application/  └── infrastructure/
└── audit/            ├── domain/  ├── application/  └── infrastructure/
```

Regras de fronteira:

1. Nenhum módulo importa `infrastructure` de outro módulo.
2. Comunicação inter-módulo só via interfaces declaradas em `domain/` (ex.: `BeneficiaryQuery`, `ProgramParameters`, `AuditRecorder`).
3. ArchUnit no CI valida as fronteiras a cada PR.
4. Banco único com schema por módulo (`beneficiary`, `social_program`, `payment`, `audit`); JOIN só dentro do próprio schema.
5. A **fórmula de cálculo** vive **apenas** no módulo `payment` — elimina a duplicação BATCHPGT × CALCBENF/CALCDSCT do legado.

## Justificativa

O legado já é, na prática, "4 domínios que conversam por dados". O monolito modular materializa essa realidade com fronteiras explícitas, sem pagar o custo operacional de microsserviços que o time e a janela não comportam. Como não há acoplamento por código no legado, não herdamos cadeias de chamadas que justifiquem split. ArchUnit garante que a clareza de domínio não degrade para um "monolito espaguete", mantendo aberta a porta de extração futura (começando por `audit`, o de menor acoplamento de escrita).

## Consequências

### Positivas

- 1 deployable = 1 pipeline CI/CD, 1 imagem Docker, 1 ambiente.
- Latência inter-módulo zero (chamadas in-process) — favorece a meta p95 < 200 ms.
- Bounded contexts rastreáveis 1:1 com os DDMs do legado.
- Fonte única de verdade para o cálculo (resolve dívida técnica de duplicação).

### Negativas

- Escala horizontal é "tudo ou nada" — **mitigação**: módulos isolados permitem extrair `payment` ou `audit` depois, se necessário.
- Falha grave afeta o processo todo — **mitigação**: testes de fronteira (ArchUnit) + cobertura ≥ 70% no módulo `payment`.
- Deploy atualiza tudo junto — **mitigação**: pipeline rápido e feature flags para mudanças de risco.

### Riscos

- Relaxar as regras do ArchUnit reintroduz acoplamento — **contingência**: o teste de fronteira é gate de CI obrigatório; quebra de fronteira reprova o PR.

## Como saber que esta decisão envelheceu mal

Revisitar este ADR quando 2+ sinais coexistirem:

- 🚨 Deploy de uma feature de um módulo causa rollback de outro (acoplamento real).
- 🚨 Tempo de build > 10 min.
- 🚨 `payment` precisa escalar 5× mais que os demais (assimetria justifica split).
- 🚨 Time cresce > 15 pessoas com duas equipes pisando no mesmo módulo.

## Referências

- [`bounded-contexts.md`](bounded-contexts.md) — os 4 contextos
- [`../01-arqueologia/dependency-map.md`](../01-arqueologia/dependency-map.md) — evidência de zero `CALLNAT`
- [`../08-exemplos/ADR-001-monolito-modular-exemplo.md`](../08-exemplos/ADR-001-monolito-modular-exemplo.md) — ADR de referência
- *Building Evolutionary Architectures* (Ford, Parsons, Kua, 2017), cap. 4
- Regras relacionadas: BR-001…BR-018 (Payment), BR-019…BR-035 (Beneficiary)
