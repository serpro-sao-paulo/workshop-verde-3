<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Cenários BDD — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Aceite](https://img.shields.io/badge/TIPO-Crit%C3%A9rios%20de%20Aceite-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **bdd-scenarios**

> Cenários de aceitação em formato BDD (Dado / Quando / Então), rastreáveis a REQ-IDs da [`SPECIFICATION.md`](SPECIFICATION.md) e ao legado (`source_legacy`). Viram testes de aceitação no Estágio 3 (QA, branch `test/payment-cycle-bdd`).

---

## Cenário 1 — Teto de 30% em descontos não judiciais (com exceção judicial)

- **REQ:** REQ-PAY-005, REQ-PAY-006
- **Regras:** BR-040, BR-041, BR-043
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L101-L131`

```gherkin
Funcionalidade: Aplicação do teto de descontos no pagamento

  Contexto:
    Dado um pagamento com valor bruto de R$ 1000,00

  Cenário: Desconto não judicial é truncado no teto de 30%
    Quando é aplicado um desconto do tipo "TAX" de R$ 400,00
    Então o desconto aplicado deve ser R$ 300,00
    E o valor líquido deve ser R$ 700,00

  Cenário: Desconto judicial ignora o teto de 30%
    Quando é aplicado um desconto do tipo "JUDICIAL" de R$ 500,00
    Então o desconto aplicado deve ser R$ 500,00
    E o valor líquido deve ser R$ 500,00

  Cenário: Mistura de judicial e não judicial soma além de 30%
    Quando é aplicado um desconto "JUDICIAL" de R$ 400,00
    E é aplicado um desconto "TAX" de R$ 250,00
    Então o total de descontos deve ser R$ 650,00
    E o desconto não judicial (R$ 250,00) não excede 30% do bruto
```

---

## Cenário 2 — Geração do ciclo mensal só para beneficiários ativos

- **REQ:** REQ-PAY-001
- **Regras:** BR-001, BR-002, BR-003, BR-005
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L227`

```gherkin
Funcionalidade: Geração do ciclo de pagamento mensal

  Cenário: Apenas beneficiários ACTIVE geram pagamento
    Dado 10 beneficiários com status "ACTIVE" vinculados a um programa "ACTIVE"
    E 2 beneficiários com status "SUSPENDED"
    Quando o ciclo de pagamento mensal é gerado
    Então devem ser criados 10 registros de pagamento
    E cada pagamento deve ter status "GENERATED"

  Cenário: Beneficiário em programa inativo é ignorado
    Dado um beneficiário "ACTIVE" vinculado a um programa com status "INACTIVE"
    Quando o ciclo de pagamento mensal é gerado
    Então nenhum pagamento deve ser criado para esse beneficiário

  Cenário: CPF duplicado na mesma competência gera um único pagamento
    Dado dois registros do mesmo CPF elegíveis na competência 06/2026
    Quando o ciclo de pagamento mensal é gerado
    Então deve ser criado apenas 1 pagamento para esse CPF
```

---

## Cenário 3 — CPF de teste "000" não deve burlar a validação

- **REQ:** REQ-BEN-001, REQ-BEN-002
- **Regras:** BR-020, BR-050
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L197-L201`
- **Corrige:** MYS-007 (backdoor de teste em produção)

```gherkin
Funcionalidade: Validação de CPF no cadastro de beneficiário

  Cenário: CPF iniciado com 000 é rejeitado sem feature flag
    Dado que a feature flag "allow-test-cpf" está desativada
    Quando um beneficiário é cadastrado com CPF "000.000.000-00"
    Então o cadastro deve ser rejeitado com HTTP 400
    E a mensagem deve indicar "CPF inválido"

  Cenário: CPF com dígito verificador inválido é rejeitado
    Quando um beneficiário é cadastrado com CPF "123.456.789-00"
    Então o cadastro deve ser rejeitado com HTTP 400

  Cenário: CPF de teste só é aceito com feature flag explícita e auditada
    Dado que a feature flag "allow-test-cpf" está ativada em ambiente de teste
    Quando um beneficiário é cadastrado com CPF "000.000.000-00"
    Então o cadastro deve ser aceito
    E um evento de auditoria deve registrar o uso do CPF de teste
```

---

## Rastreabilidade

| Cenário | REQ-ID | BR | Mistério corrigido |
| --- | --- | --- | --- |
| 1 — Teto de descontos | REQ-PAY-005, REQ-PAY-006 | BR-040, BR-041, BR-043 | — |
| 2 — Ciclo mensal | REQ-PAY-001 | BR-001, BR-002, BR-003, BR-005 | — |
| 3 — CPF de teste | REQ-BEN-001, REQ-BEN-002 | BR-020, BR-050 | MYS-007 |

> Cada cenário vira um teste de aceitação no Estágio 3. O QA (Par 4) implementa em `test/payment-cycle-bdd` com JUnit 5 + Testcontainers (backend) seguindo a estratégia da skill `test-strategy`.
