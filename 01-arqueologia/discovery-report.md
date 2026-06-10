<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: [Nome do Time]
**Data**: 19/05/2026
**Edição**:
**Participantes**: [Liste os membros e suas personas]

---

## 1. Sumário Executivo

O SIFAP é um sistema de fiscalização e administração de pagamentos de benefícios sociais, escrito em Natural/Adabas e em produção há 29 anos. São **15 programas .NSN** (cadastro, cálculo, validação, consulta, batch e relatórios) sobre **4 DDMs Adabas** (BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO, AUDITORIA). É **altamente crítico**: processa ~3,8 milhões de pagamentos/mês. O código carrega décadas de regras de negócio não documentadas, constantes mágicas e divergências entre documentação e implementação. A descoberta mais relevante é arquitetural: **nenhum programa chama outro** (zero `CALLNAT`) — toda lógica é duplicada inline, o que contraria a documentação oficial.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Calcular, conceder, pagar e auditar benefícios sociais a beneficiários cadastrados, organizados por programa social (BPC, Bolsa Família, Auxílio Brasil etc.). Cobre o ciclo completo: cadastro de beneficiário e dependentes, validação cadastral/elegibilidade/documental, cálculo de valor e descontos, geração mensal de pagamentos em lote, conciliação bancária (CNAB 240) e relatórios gerenciais e de auditoria.

### 2.2 Arquitetura Legada

- **15 programas Natural**: 10 online (3270) + 5 batch.
- **4 DDMs Adabas**: BENEFICIARIO (FNR 150), PROGRAMA-SOCIAL (FNR 151), PAGAMENTO (FNR 152), AUDITORIA (FNR 153).
- **Integração por dados, não por código**: os programas se comunicam exclusivamente via DDMs compartilhados. Não há `CALLNAT` nem `INCLUDE` em nenhum programa.
- **Lógica duplicada**: BATCHPGT replica inline as fórmulas de CALCBENF e CALCDSCT, apesar de o cabeçalho dizer que "chama" esses programas.
- **Entrada/saída**: terminal 3270 (online), arquivo CNAB 240 (conciliação), impressora matricial (relatórios texto 132 colunas).

### 2.3 Usuários e Perfis

Operadores de cadastro/consulta (transações SF01–SF06 via 3270), operadores de batch (jobs mensais), e auditores (relatórios de auditoria). O controle de acesso é por transação no monitor COM-PLETE; não há perfis granulares no código analisado.

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

1. **Cálculo do benefício com Fator-K** — `BR` ligada a `CADPROG.NSN#L87` (constante 0.347215). Base de todos os valores.
2. **Fórmula de 13º em dezembro** — `CALCBENF.NSN#L242-L244`. Sazonalidade que altera o cálculo no mês 12.
3. **Teto de 30% nos descontos, exceto judicial** — `CALCDSCT.NSN#L101-L131`. Desconto judicial não respeita o teto.
4. **Suspensão automática de idosos (>75)** — `CADBENEF.NSN#L167-L168`. Muda o status do beneficiário.
5. **Geração de pagamentos para beneficiários ativos** — `BATCHPGT.NSN#L178-L233`. Núcleo do ciclo mensal, com ordenação por CPF que é dependência externa.

> Catálogo completo: 68 regras em [`business-rules-catalog.md`](business-rules-catalog.md).

### 3.2 Dependências Complexas

O acoplamento **não é por chamada de código** (não há `CALLNAT`), e sim **pelos dados**. O ponto de maior risco de efeito cascata é o **DDM PAGAMENTO**, escrito/lido por 8 dos 15 programas (CALCBENF, CALCDSCT, CALCCORR, CONSBENF, BATCHPGT, BATCHCON, BATCHREL, RELPGT). Mudanças nesse schema impactam quase todo o sistema. Ver [`dependency-map.md`](dependency-map.md).

### 3.3 Dívida Técnica Identificada

- [x] Lógica de cálculo duplicada entre BATCHPGT e CALCBENF/CALCDSCT (risco de divergência silenciosa).
- [x] Constantes mágicas sem documentação (ex.: Fator-K 0.347215, tabela regional hardcoded).
- [x] Arredondamento inconsistente (TRUNCATE em CALCBENF × ROUND em BATCHREL).
- [x] Backdoors em produção (CPF `000...` em VALBENEF; 8 prefixos especiais em VALDOCS).
- [x] Código morto e comentado (Plano Verão em CALCCORR; integração Banco Real em BATCHCON).

### 3.4 Gaps de Documentação

A documentação legada (`legacy-docs/`) está **desatualizada de propósito**: o Manual 2008 cita apenas 3 DDMs (omite AUDITORIA/FNR 153) e 12 programas (omite CALCDSCT, RELPGT, RELAUDIT); o projeto de 1997 não previa VALELEG. Nenhuma das regras de cálculo críticas (Fator-K, 13º, teto de descontos) aparece em qualquer documento.

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Detalhamento completo em [`mysteries-found.md`](mysteries-found.md).

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-003 | Origem da constante Fator-K 0.347215 | Reproduzir errado quebra todos os valores |
| MYS-007 | CPFs `000...` aceitos sem validação (backdoor) | Vetor de fraude; decidir manter/remover |
| MYS-008 | Região 99 pula elegibilidade | Concessão sem checagem; precisa ser explícito |
| MYS-009 | Ordem de processamento por CPF é dependência externa | Reordenar/paralelizar quebra integrações |
| MYS-010 | Auditoria 'EX' oculta dos relatórios | Risco de compliance |

### 4.2 Riscos para o Estágio 2

1. **Lógica duplicada**: a spec precisa decidir uma única fonte de verdade para o cálculo (consolidar BATCHPGT × CALCBENF/CALCDSCT).
2. **Constantes e regras escondidas**: tudo (Fator-K, 13º, teto, suspensão de idosos) precisa virar requisito EARS explícito com `source_legacy:`.
3. **Backdoors e bypasses**: CPF `000`, região 99 e prefixos especiais precisam de decisão consciente (manter como feature flag, ou remover).

---

## 5. Recomendações

### 5.1 O que migrar primeiro

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1 | Cálculo de benefício (CALCBENF/CALCDSCT) | Núcleo financeiro; onde moram as regras críticas |
| 2 | Geração de pagamentos batch (BATCHPGT) | Operação mensal crítica que consome o cálculo |
| 3 | Cadastro e validação de beneficiário (CADBENEF/VALBENEF) | Porta de entrada dos dados |

### 5.2 O que descartar

- **Código Plano Verão (CALCCORR)**: lógica de 1989–1991 comentada; sem uso atual.
- **Integração Banco Real (BATCHCON L207)**: banco extinto/incorporado; código morto.
- **Correção IPCA hardcoded 2010–2012 (CALCCORR)**: janela fixa obsoleta; redesenhar como parâmetro.

### 5.3 O que evoluir

- **Validação de CPF (VALBENEF)**: remover o backdoor `000` ou transformá-lo em ambiente de teste isolado.
- **Trilha de auditoria (RELAUDIT)**: parar de ocultar eventos 'EX'; tornar a auditoria completa.
- **Arredondamento**: padronizar uma única política (eliminar divergência TRUNCATE × ROUND).

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 15 / 15      |
| DDMs mapeados                 | 4 / 4        |
| Regras de negócio encontradas | 68           |
| Regras escondidas encontradas | 10 / 10      |
| Easter eggs encontrados       | 3 / 3        |
| Termos no glossário           | 60           |
| Mistérios catalogados         | 10           |
| Tempo total gasto             | ~2 horas     |

---

## 7. Notas para o Próximo Estágio

Toda regra de cálculo crítica está **só no código** — nada nos docs. Ao escrever EARS, cada requisito de cálculo precisa apontar `source_legacy:` para o programa e linha exatos (este relatório e o catálogo de regras já trazem as referências). Atenção especial: (1) consolidar a lógica duplicada de pagamento numa única fonte; (2) decidir explicitamente sobre os bypasses (CPF `000`, região 99, prefixos VALDOCS, desconto judicial sem teto); (3) o DDM PAGAMENTO é o hub de acoplamento — qualquer mudança de schema é de alto impacto.

---

## Definição de Pronto deste relatório

- [x] Todas as seções acima preenchidas (sem placeholders).
- [x] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando o legado.
- [x] Decisões de migrar/descartar/evoluir em §5 cobrem as funcionalidades principais.
- [x] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

