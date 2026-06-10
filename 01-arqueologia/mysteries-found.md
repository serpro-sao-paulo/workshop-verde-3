<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

| ID      | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
| ------- | --------- | --------------- | ----------------- | --------- |
| MYS-001 | Beneficiário com mais de 75 anos é suspenso (status 'S') silenciosamente no cadastro | `CADBENEF.NSN#L167-L168` | Suspensão indevida de idosos; perda de benefício sem aviso | ALTA |
| MYS-002 | Limite de 5 dependentes hardcoded contradiz o DDM (campo N2, suporta 99) | `CADDEPEND.NSN#L63` | Regra de negócio invisível; divergência código × dados | ALTA |
| MYS-003 | Constante mágica `0.347215` no cálculo do Fator-K, sem documentação | `CADPROG.NSN#L87` | Origem desconhecida; reproduzir errado quebra todos os valores | ALTA |
| MYS-004 | Em dezembro (mês 12) o cálculo muda para fórmula de 13º salário | `CALCBENF.NSN#L242-L244` | Cálculo sazonal não documentado; erro de pagamento anual | ALTA |
| MYS-005 | Arredondamento por TRUNCATE causa perda sistemática de centavos | `CALCBENF.NSN#L231`, `BATCHREL.NSN#L136` | Perda acumulada de centavos × milhões de pagamentos | MÉDIA |
| MYS-006 | Desconto judicial ignora o teto de 30% aplicado aos demais descontos | `CALCDSCT.NSN#L101-L131` | Beneficiário pode ter desconto > 30% do bruto | ALTA |
| MYS-007 | CPFs iniciados com `000` são aceitos sem validação de dígito | `VALBENEF.NSN#L197-L201` | Backdoor de teste em produção; fraude possível | ALTA |
| MYS-008 | Região 99 (internacional/diplomático) pula TODA verificação de elegibilidade | `VALELEG.NSN#L105-L107` | Concessão sem checagem para uma região inteira | ALTA |
| MYS-009 | BATCHPGT lê em ordem alfabética por CPF; sistemas downstream dependem disso | `BATCHPGT.NSN#L178-L179` | Mudar a ordem quebra integrações externas não documentadas | MÉDIA |
| MYS-010 | Eventos de auditoria com ação 'EX' (exclusão) são ocultados dos relatórios | `RELAUDIT.NSN#L103-L105` | Exclusões somem da trilha de auditoria; risco de compliance | ALTA |

## Detalhamento dos Mistérios

### MYS-001: Suspensão silenciosa de idosos (>75 anos)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L167-L168`
- **Trecho de código**:

```natural
IF #IDADE > 75
  MOVE 'S' TO #STATUS
```

- **O que esperávamos**: status inicial 'A' (ativo) para todo cadastro válido.
- **O que o código faz**: define status 'S' (suspenso) para qualquer beneficiário com mais de 75 anos, sem aviso ao operador. O cabeçalho cita "AJUSTE STATUS IDOSO" (2011) mas não explica a regra.
- **Hipótese do time**: regra normativa da época para revisão de benefícios de idosos, implementada como suspensão automática.
- **Risco se ignorarmos**: na migração, idosos seriam cadastrados como ativos, mudando o comportamento de pagamento. (Cobre checklist MYS-001.)

---

### MYS-002: Limite de 5 dependentes hardcoded contra o DDM

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L63`
- **Trecho de código**:

```natural
IF #NUM-DEP > 5
```

- **O que esperávamos**: limite vindo do DDM ou de parâmetro do programa social.
- **O que o código faz**: bloqueia o 6º dependente com um literal `5`, embora o campo `NUM-DEPENDENTES (N2)` suporte até 99 e o manual cite "máximo 3".
- **Hipótese do time**: limite operacional fixado no código sem alinhamento com docs.
- **Risco se ignorarmos**: divergência tripla (código=5, doc=3, DDM=99). (Cobre MYS-002 e INC-001.)

---

### MYS-003: Constante mágica 0.347215 (Fator-K)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L87`
- **Trecho de código**:

```natural
COMPUTE #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)
```

- **O que esperávamos**: índice de reajuste documentado e parametrizável.
- **O que o código faz**: multiplica o fator de reajuste por uma constante `0.347215` cuja origem não está em nenhum documento.
- **Hipótese do time**: derivada de um índice econômico dos anos 90/2003 ("INC FATOR CORRECAO", 2003).
- **Risco se ignorarmos**: reproduzir o cálculo sem essa constante altera o valor base de todos os programas. (Cobre MYS-003 e INC-003.)

---

### MYS-004: Fórmula de cálculo muda em dezembro (13º)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L242-L244`
- **Trecho de código**:

```natural
IF #MES = 12
  COMPUTE #VLR-13 = #VLR-BASE * #FATOR-REG * #FATOR-IDADE
```

- **O que esperávamos**: fórmula única o ano todo.
- **O que o código faz**: no mês 12 aplica fórmula de 13º salário (proporcional a meses ativos/12), diferente dos demais meses.
- **Hipótese do time**: pagamento de 13º incluído em 2001 ("INC 13O SALARIO").
- **Risco se ignorarmos**: dezembro pagaria valor errado se a sazonalidade não for migrada. (Cobre MYS-004.)

---

### MYS-005: Perda de centavos por TRUNCATE

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L231` e `BATCHREL.NSN#L136`
- **Trecho de código**:

```natural
* TRUNCAR P/ 2 CASAS DECIMAIS - PADRAO MAINFRAME   (CALCBENF L231)
* NOTA: ARREDONDAMENTO DIFERE DO CALCBENF (ROUND VS TRUNCATE)  (BATCHREL L136)
```

- **O que esperávamos**: arredondamento consistente em todo o sistema.
- **O que o código faz**: CALCBENF trunca valores; BATCHREL arredonda (ROUND). Os dois divergem para o mesmo tipo de valor.
- **Hipótese do time**: "padrão mainframe" herdado; BATCHREL foi escrito por outra pessoa em outro ano.
- **Risco se ignorarmos**: relatórios não batem com pagamentos; perda sistemática de centavos. (Cobre MYS-005 e INC-004.)

---

### MYS-006: Desconto judicial sem teto de 30%

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L101-L131`
- **Trecho de código**:

```natural
* CALC TETO MAXIMO DESCONTO - 30% DO BRUTO
COMPUTE #VLR-MAX-DSCT = #VLR-BRUTO * 0.30
...
* JUDICIAL NAO TEM TETO
ADD #VLR-DSCT-ITEM TO #VLR-TOTAL-DSCT
```

- **O que esperávamos**: o teto de 30% aplicado a todos os descontos.
- **O que o código faz**: o tipo 'J' (judicial) soma ao total sem respeitar o `#VLR-MAX-DSCT`.
- **Hipótese do time**: descontos judiciais (penhora) têm prioridade legal sobre o teto.
- **Risco se ignorarmos**: beneficiário pode receber líquido abaixo do esperado; regra legal omitida. (Cobre MYS-006.)

---

### MYS-007: CPFs `000...` aceitos sem validação

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L197-L201`
- **Trecho de código**:

```natural
* EXCECAO: CPFs INICIADOS COM 000 SAO VALIDOS (TESTE GOVERNO)
IF #DIG(1) = 0 AND #DIG(2) = 0 AND #DIG(3) = 0
  MOVE TRUE TO #CPF-VALIDO
  ESCAPE ROUTINE
```

- **O que esperávamos**: todo CPF passa por módulo 11.
- **O que o código faz**: CPFs com todos os dígitos iguais começando por `000` (ex.: `000.000.000-00`) são marcados válidos sem checar o dígito verificador.
- **Hipótese do time**: backdoor de teste do governo deixado em produção.
- **Risco se ignorarmos**: vetor de fraude; precisa virar feature flag controlada ou ser removido. (Cobre MYS-007 e EGG-002 parcial.)

---

### MYS-008: Região 99 pula elegibilidade

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L105-L107`
- **Trecho de código**:

```natural
* REGIAO 99 - INTERNACIONAL/DIPLOMATICO
IF #COD-REG = 99
```

- **O que esperávamos**: toda concessão passa pelas regras do programa social.
- **O que o código faz**: beneficiários da região 99 (internacional/diplomático) pulam todas as verificações de elegibilidade. Incluído em 2013 ("INC REGIAO 99").
- **Hipótese do time**: tratamento especial para diplomatas/exterior.
- **Risco se ignorarmos**: concessão sem checagem para uma região inteira; precisa ser explícito na spec. (Cobre MYS-008.)

---

### MYS-009: Ordem batch por CPF é dependência externa

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L178-L179`
- **Trecho de código**:

```natural
* LEITURA EM ORDEM ALFABETICA POR CPF (OTIMIZACAO 1999)
* NOTA: SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO
```

- **O que esperávamos**: ordem de processamento irrelevante para o resultado.
- **O que o código faz**: processa beneficiários ordenados por CPF; o próprio comentário avisa que sistemas downstream dependem dessa ordem.
- **Hipótese do time**: arquivo de remessa/integração consumido por terceiros assume essa sequência.
- **Risco se ignorarmos**: paralelizar ou reordenar o batch moderno quebra integrações externas. (Cobre MYS-009.)

---

### MYS-010: Auditoria 'EX' oculta dos relatórios

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L103-L105`
- **Trecho de código**:

```natural
* FILTRO ACAO - EXCLUSOES NAO SAO EXIBIDAS
...
IF AUDITORIA-V.ACAO = 'EX'
```

- **O que esperávamos**: relatório de auditoria mostra todos os eventos.
- **O que o código faz**: filtra e oculta eventos de ação 'EX' (exclusão) do relatório.
- **Hipótese do time**: decisão antiga para "limpar" o relatório, mas remove rastreabilidade de exclusões.
- **Risco se ignorarmos**: exclusões invisíveis violam requisitos de compliance/auditoria. (Cobre MYS-010.)

---

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado.

1. [x] **EGG-001 — Plano Verão**: bloco de correção comentado referencia o Plano Verão (1989–1991), transição Cruzado→Cruzeiro, nunca removido. `CALCCORR.NSN#L100-L101`.
2. [x] **EGG-002 — Backdoor de documentos**: `VALDOCS` carrega 8 prefixos especiais (`000`,`001`,`002`,`010`,`011`,...) que aceitam documentos sem verificação real. `VALDOCS.NSN#L42-L55`.
3. [x] **EGG-003 — Integração extinta**: código morto referencia integração com o "BANCO REAL — DESCONTINUADA" (banco incorporado em 2009). `BATCHCON.NSN#L207`.

## Resumo

- Total de mistérios encontrados: **10 / 10**
- Confiança alta: **8**
- Confiança média: **2**
- Confiança baixa: **0**
- Easter eggs encontrados: **3 / 3**
- Inconsistências doc × código: **INC-001** (limite dependentes), **INC-002** (DDM AUDITORIA ausente do projeto de 1997), **INC-003** (Fator-K não documentado), **INC-004** (ROUND vs TRUNCATE)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

