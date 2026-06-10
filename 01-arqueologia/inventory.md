# Inventário Legado — [Nome da Equipe]

**Data:** 2026-06-10
**Caminho escaneado:** `01-arqueologia/legado-sifap/`
**Nota:** Esta é a primeira passada — a ser revisada conforme a equipe lê arquivos individuais.

## Estrutura de Pastas

```
01-arqueologia/legado-sifap/
├── README.md
├── COMO-LER-NATURAL.md
├── natural-programs/
│   ├── README.md
│   ├── BATCHCON.NSN
│   ├── BATCHPGT.NSN
│   ├── BATCHREL.NSN
│   ├── CADBENEF.NSN
│   ├── CADDEPEND.NSN
│   ├── CADPROG.NSN
│   ├── CALCBENF.NSN
│   ├── CALCCORR.NSN
│   ├── CALCDSCT.NSN
│   ├── CONSBENF.NSN
│   ├── RELAUDIT.NSN
│   ├── RELPGT.NSN
│   ├── VALBENEF.NSN
│   ├── VALDOCS.NSN
│   └── VALELEG.NSN
├── adabas-ddms/
│   ├── README.md
│   ├── AUDITORIA.ddm
│   ├── BENEFICIARIO.ddm
│   ├── PAGAMENTO.ddm
│   └── PROGRAMA-SOCIAL.ddm
├── legacy-docs/
│   ├── README.md
│   ├── ARQUITETURA-ORIGINAL-1997.md
│   ├── ARQUITETURA-ORIGINAL-1997.docx
│   ├── MANUAL-TECNICO-SIFAP-2008.md
│   ├── MANUAL-TECNICO-SIFAP-2008.docx
│   ├── REGRAS-NEGOCIO-2012.md
│   └── REGRAS-NEGOCIO-2012.docx
└── demo/
    ├── README.md
    └── sifap-terminal-demo-script.md
```

**Total de diretórios:** 5 (legado-sifap/, natural-programs/, adabas-ddms/, legacy-docs/, demo/)

## Contagem de Arquivos por Tipo

| Extensão | Contagem | Finalidade provável |
| -------- | -------- | ------------------- |
| `.NSN`   | 15       | Programa-fonte Natural (lógica de negócio) |
| `.ddm`   | 4        | Data Definition Module — schema Adabas |
| `.md`    | 8        | Documentação (READMEs, guia de leitura, docs legados convertidos) |
| `.docx`  | 3        | Documentação legada em formato original (Word) |
| `.html`  | 1        | Demo terminal interativa |

**Total de arquivos:** 31

## Padrões de Convenção de Nomes

| Prefixo    | Contagem | Hipótese |
| ---------- | -------- | -------- |
| `BATCH`    | 3        | Programas batch — processamento em lote (folha de pagamento, relatórios, conciliação). Executados por scheduler JES2. |
| `CAD`      | 3        | Programas de cadastro — CRUD de entidades (beneficiário, dependente, programa social). Online/interativos via terminal 3270. |
| `CALC`     | 3        | Programas de cálculo — lógica financeira (benefício, correção, desconto). Núcleo de regras de negócio do sistema. |
| `VAL`      | 3        | Programas de validação — verificação de dados cadastrais, elegibilidade e documentação. |
| `CONS`     | 1        | Consulta online — tela de busca de beneficiários. Prefixo com ocorrência única. |
| `REL`      | 2        | Relatórios — geração de relatórios de pagamento e auditoria. |

## Itens Incomuns (Top 3)

| # | Caminho do Arquivo | O Que o Torna Incomum | Investigação Sugerida |
| - | ------------------ | --------------------- | --------------------- |
| 1 | `legacy-docs/REGRAS-NEGOCIO-2012.md` | Documento de regras de negócio marcado como **INCOMPLETO** (levantamento interrompido em ago/2012 por aposentadoria de analistas-chave). Contém várias seções `[PENDENTE]` e `[A COMPLETAR]`. | Cruzar as regras RN-001 a RN-023 documentadas neste arquivo com o código-fonte dos programas `.NSN` para identificar lacunas e validar precisão. |
| 2 | `natural-programs/CONSBENF.NSN` | Prefixo `CONS` ocorre apenas uma vez entre os 15 programas. Todos os outros prefixos têm 2-3 ocorrências. Programa isolado pode indicar padrão de nome diferente ou programa originalmente parte de outro módulo. | Verificar se CONSBENF tem dependências com outros programas e se existe lógica de negócio relevante ou se é apenas uma tela de consulta passthrough. |
| 3 | `adabas-ddms/AUDITORIA.ddm` | DDM adicionado em 2005 (não constava no projeto original de 1997 nem no Manual Técnico de 2008). Os programas AUDCONSUL e AUDRELAT previstos originalmente foram substituídos por RELAUDIT. | Verificar quais programas efetivamente leem/escrevem neste DDM e se há campos não utilizados remanescentes do design original. |

## Ordem de Leitura Proposta

> **Hipótese** — esta ordem será revisada quando a equipe começar a rastrear dependências via CALLNAT.

1. **DDMs primeiro** (entender os dados antes do código):
   - `BENEFICIARIO.ddm` — entidade central, ~4.2M registros
   - `PROGRAMA-SOCIAL.ddm` — parametrização, contém campos PE/MU
   - `PAGAMENTO.ddm` — transações financeiras, ~180M registros
   - `AUDITORIA.ddm` — trilha de auditoria, adicionado em 2005

2. **Entry points batch** (revelam o fluxo de negócio inteiro):
   - `BATCHPGT.NSN` — orquestra o ciclo mensal de pagamento, chama CALCBENF, CALCCORR, CALCDSCT e VALELEG
   - `BATCHCON.NSN` — conciliação financeira com SIAFI
   - `BATCHREL.NSN` — geração de relatórios gerenciais

3. **Programas de cálculo** (núcleo financeiro, maior risco de erro na migração):
   - `CALCBENF.NSN` — fórmula base de benefício
   - `CALCCORR.NSN` — correções e reajustes anuais
   - `CALCDSCT.NSN` — descontos e deduções (regra dos 30%)

4. **Programas de validação** (viram testes no sistema novo):
   - `VALBENEF.NSN`, `VALELEG.NSN`, `VALDOCS.NSN`

5. **Cadastros e consultas** (entidades e interface do usuário):
   - `CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN`
   - `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`
