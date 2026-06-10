<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002 — Estratégia de Migração de Dados Adabas → PostgreSQL (Expand-Contract com backfill)

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge)

**Data**: 2026-06-10
**Status**: Aceita
**Decisores**: Par 2 (EA + SA) + Par 4 (DBA), com revisão do Par 1 (PO)

## Contexto

Os dados do SIFAP vivem em 4 DDMs Adabas com construtos que **não existem em PostgreSQL relacional puro**:

- **Grupos periódicos (PE)** — ex.: `DEPENDENTES` (até 5) em BENEFICIARIO; `DESCONTOS` com vigência em PAGAMENTO.
- **Campos multivalorados (MU)** — listas dentro de um registro.
- **Campos compactados (packed decimal)** e nomes abreviados anos 90 (`BN-NM-BENEF`, `PG-VL-BRUTO`).
- **Sem integridade referencial** no Adabas — toda validação era nos programas Natural.

Volumes (ref. 2018): ~4,2 mi beneficiários, ~180 mi registros de pagamento (histórico), ~25 mi de auditoria, crescimento de ~46 mi/ano em PAGAMENTO. **Não há política de expurgo.**

Restrições: a migração precisa ser **reversível** (rollback seguro), **verificável** (valores batem com o legado) e **sem perda de rastreabilidade** das regras (`source_legacy`).

## Opções Consideradas

### Opção 1: Big-bang (corte único)

- **Descrição**: congela o legado, exporta tudo, importa no PostgreSQL, vira a chave.
- **Vantagens**: modelo de dados limpo desde o início; sem período de dupla escrita.
- **Desvantagens**: janela de downtime enorme para 180 mi+ registros; rollback caro; alto risco de divergência descoberta tarde demais.

### Opção 2: Expand-Contract (expandir → migrar → contrair) com backfill incremental

- **Descrição**: cria o schema novo normalizado (expand); migra dados em lotes (backfill) com tabelas de mapeamento PE/MU → tabelas filhas; valida por amostragem e por totais; só então desativa o legado (contract). Tabelas de pagamento histórico migram por competência (mês a mês).
- **Vantagens**: reversível a cada etapa; downtime mínimo; permite testes de equivalência por lote; normaliza PE/MU em tabelas filhas (`beneficiary_dependent`, `payment_deduction`).
- **Desvantagens**: mais etapas; exige scripts de backfill e verificação; convivência temporária de dois modelos.

### Opção 3: Lift-and-shift (replicar o modelo Adabas no PostgreSQL)

- **Descrição**: cria colunas que imitam PE/MU (ex.: `dependente_1..5`) e mantém nomes abreviados.
- **Vantagens**: migração mecânica rápida.
- **Desvantagens**: carrega a dívida do legado (desnormalização, limites mágicos como 5 dependentes); impede usar integridade referencial e índices adequados; trava a evolução.

## Decisão

**Decidimos usar Expand-Contract com backfill incremental por lote (Opção 2).**

1. **Expand**: criar o schema PostgreSQL normalizado via migrations Flyway. PE → tabelas filhas (`beneficiary_dependent`, `payment_deduction`); MU → tabelas de relação; packed decimal → `NUMERIC`; nomes legíveis em inglês.
2. **Backfill**: migrar em lotes idempotentes. BENEFICIARIO e PROGRAMA-SOCIAL de uma vez; PAGAMENTO e AUDITORIA **por competência** (mês a mês), do mais recente para o mais antigo.
3. **Verify**: para cada lote, testes de equivalência (totais por programa/competência + amostragem de cálculo) comparando legado × novo, com tolerância de R$0,01 (mesma de BR-061).
4. **Contract**: só após verde em todos os lotes, desativar leitura do legado.
5. **Integridade**: aplicar FKs e constraints **depois** do backfill de cada lote, para não bloquear a carga.

Regras de transformação documentadas como requisitos `REQ-MIG-*` com `source_legacy` apontando para os DDMs.

## Justificativa

O volume e a criticidade (180 mi+ registros, ciclo mensal que não pode parar) tornam o big-bang inaceitável em risco. O expand-contract dá pontos de rollback e permite **provar equivalência** antes de cortar — essencial porque as regras de cálculo só existem no código (não nos docs). Migrar por competência casa com o padrão de uso (relatórios e conciliação são por mês). O lift-and-shift foi rejeitado por perpetuar a dívida (ex.: limite de 5 dependentes hardcoded, MYS-002) que a modernização quer eliminar.

## Consequências

### Positivas

- Rollback seguro por etapa e por lote.
- Equivalência verificável antes do corte (reduz risco de divergência regulatória).
- Modelo novo já normalizado e com integridade referencial.

### Negativas

- Convivência temporária de dois modelos — **mitigação**: janela de dupla leitura curta e por competência.
- Scripts de backfill e verificação adicionais — **mitigação**: gerados com testes de equivalência (Estágio 3) e reutilizáveis por lote.

### Riscos

- Sem política de expurgo, o histórico é gigante — **contingência**: migrar competências antigas em background, com baixa prioridade, após o corte das competências ativas.
- Campos sem validação no legado podem ter lixo (ex.: região inválida, BR-031) — **contingência**: relatório de exceções por lote; dados inválidos vão para tabela de quarentena, não bloqueiam a carga.

## Como saber que esta decisão envelheceu mal

- 🚨 Verificação de equivalência falha repetidamente em um mesmo tipo de cálculo (a transformação está errada — pausar e revisar a regra).
- 🚨 Backfill de uma competência leva mais que a janela noturna (rever lote/índices).

## Referências

- [`../01-arqueologia/legado-sifap/adabas-ddms/`](../01-arqueologia/legado-sifap/adabas-ddms/) — DDMs de origem
- [`../01-arqueologia/discovery-report.md`](../01-arqueologia/discovery-report.md) — volumes e dívida técnica
- Skill `safe-migration` (expand-contract, backfill) e `query-optimization`
- Regras relacionadas: BR-007/BR-032 (dependentes PE), BR-042 (descontos PE com vigência), BR-061 (tolerância R$0,01)
