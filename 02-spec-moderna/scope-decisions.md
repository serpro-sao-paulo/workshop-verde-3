<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o SIFAP 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: SIFAP 2.0
**Data**: 2026-06-10
**Edição**: Workshop de Modernização
**Par 1 (Product Owner) responsável**: Par 1

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

| #   | Funcionalidade | Decisão | Justificativa | Regra de Negócio (BR-XXX) | Prioridade |
| --- | --- | --- | --- | --- | --- |
| 1 | Cadastro de Beneficiários | **Migrar** | Porta de entrada dos dados; afeta o ciclo de pagamento | BR-019…BR-031 | Alta |
| 2 | Cadastro de Dependentes | **Evoluir** | Limite de 5 hardcoded vira parâmetro configurável | BR-032…BR-035 | Média |
| 3 | Cadastro de Programa Social | **Migrar** | Parâmetros (valor-base, Fator-K) essenciais ao cálculo | BR-036…BR-039 | Alta |
| 4 | Cálculo de Benefícios | **Migrar** | Núcleo financeiro; regras críticas | BR-006…BR-013 | Alta |
| 5 | Cálculo de Descontos | **Migrar** | Teto 30% e desconto judicial são regras legais | BR-040…BR-043 | Alta |
| 6 | Correção Retroativa (IPCA) | **Evoluir** | Janela IPCA hardcoded 2010-2012 vira parâmetro | BR-044, BR-045 | Baixa |
| 7 | Geração de Pagamentos (Batch) | **Migrar** | Ciclo mensal crítico; opera a folha | BR-001…BR-016 | Alta |
| 8 | Conciliação Bancária (CNAB 240) | **Migrar** | Fecha o ciclo financeiro; regulatório | BR-060…BR-062 | Alta |
| 9 | Validação Cadastral (CPF/nome/data) | **Evoluir** | Remover backdoor CPF 000 (MYS-007) | BR-020, BR-050…BR-053 | Alta |
| 10 | Validação de Documentos | **Evoluir** | Remover backdoor de prefixos especiais (EGG-002) | BR-054, BR-055 | Média |
| 11 | Validação de Elegibilidade | **Migrar** | Cruzamento beneficiário × programa; região 99 explícita | BR-056…BR-059 | Alta |
| 12 | Consulta de Beneficiário | **Migrar** | Corrigir bug de máscara de CPF (BR-049) | BR-047…BR-049 | Média |
| 13 | Relatório de Pagamentos | **Migrar** | Relatório regulatório (TCU) | BR-063…BR-065, BR-068 | Média |
| 14 | Relatório/Trilha de Auditoria | **Evoluir** | Parar de ocultar exclusões 'EX' (MYS-010) | BR-066, BR-067 | Alta |
| 15 | Correção Plano Verão (1989-1991) | **Descartar** | Código morto comentado; sem uso atual (EGG-001) | BR-046 | Baixa |
| 16 | Integração Banco Real | **Descartar** | Banco extinto/incorporado; código morto (EGG-003) | — | Baixa |

---

## Funcionalidades Novas (não existem no legado)

> Liste funcionalidades que o SIFAP 2.0 deveria ter e que não existem no sistema legado. Cada uma vira REQ-ID com `source_legacy: [GREENFIELD] <justificativa>`.

| #   | Funcionalidade Nova | Justificativa | Prioridade | Complexidade |
| --- | --- | --- | --- | --- |
| N1 | Autenticação Gov.br (OIDC) + RBAC | Legado usava controle por transação 3270; SSO governamental (ADR-003) | Alta | Média |
| N2 | Trilha de auditoria imutável (sem UPDATE/DELETE) | Compliance TCU/CGU; legado não protege contra remoção (REQ-AUD-001) | Alta | Baixa |
| N3 | Mascaramento de CPF em logs (LGPD) | LGPD Art. 6º minimização; sem equivalente no legado (REQ-AUD-004) | Alta | Baixa |

---

## Resumo de Escopo

| Decisão   | Quantidade | Percentual |
| --------- | ---------- | ---------- |
| Migrar    | 9          | 56%        |
| Descartar | 2          | 13%        |
| Evoluir   | 5          | 31%        |
| **Total** | 16         | 100%       |

## Riscos de Escopo

> Liste os riscos das decisões tomadas:

| Risco | Probabilidade | Impacto | Mitigação |
| --- | --- | --- | --- |
| Origem da constante Fator-K (0.347215) desconhecida (MYS-003) | Média | Alto | Parametrizar; validar valores por equivalência contra o legado |
| Remover backdoors (CPF 000, região 99, prefixos) pode quebrar fluxos de teste | Média | Médio | Substituir por feature flags explícitas e auditadas (ADR-003) |
| Migração de 180 mi+ registros sem expurgo | Alta | Alto | Expand-contract por competência (ADR-002) |
| Divergência TRUNCATE × ROUND entre cálculo e relatório (MYS-005) | Média | Médio | Política única de truncamento (REQ-PAY-003) |

## Aprovação

- [ ] Par 1 (Product Owner) aprovou as decisões de escopo
- [x] Par 2 (Enterprise Architect) validou a viabilidade técnica
- [ ] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3
- [ ] Time concordou com as prioridades

> **Aprovação obrigatória na Passagem #2** (~16:00). Sem ela, o Estágio 3 não começa.

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

