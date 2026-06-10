<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003 — Autenticação via Gov.br (OIDC) e Autorização por Perfis (RBAC)

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge)

**Data**: 2026-06-10
**Status**: Aceita
**Decisores**: Par 2 (EA + SA) + Par 5 (DevOps), com revisão do Par 1 (PO)

## Contexto

O legado autenticava por **transação no monitor COM-PLETE** (mainframe 3270), sem perfis granulares no código analisado. Os programas implicam papéis distintos:

- **Operador** — cadastra beneficiários, gera/aprova ciclos de pagamento (CADBENEF, BATCHPGT).
- **Auditor** — consulta e emite relatórios de auditoria (RELAUDIT, CONSBENF).
- **Administrador** — gere programas sociais e parâmetros (CADPROG).

Além disso, o código contém **bypasses sensíveis** que não podem virar brecha no sistema novo: CPFs `000...` aceitos sem validação (MYS-007), região 99 que pula elegibilidade (MYS-008), prefixos especiais em VALDOCS (EGG-002). A autorização moderna precisa ser explícita e auditável.

Restrições: stack Spring Boot 3.3; diretriz do projeto é **OAuth2/JWT** e, em produção, **Managed Identity** para serviço-a-serviço; sem segredos hardcoded; CORS sem wildcard em produção.

## Opções Consideradas

### Opção 1: Gov.br (OIDC) para usuários + Spring Security RBAC

- **Descrição**: login federado via Gov.br (OpenID Connect); o backend valida o JWT e mapeia claims para perfis (`OPERATOR`, `AUDITOR`, `ADMIN`); autorização por método com `@PreAuthorize`.
- **Vantagens**: SSO governamental padrão; sem gestão de senhas própria; perfis explícitos e testáveis; alinhado ao diagrama C4 (ator Gov.br).
- **Desvantagens**: dependência externa do Gov.br; exige configurar fluxo OIDC e refresh de token.

### Opção 2: Usuários locais com tabela própria + JWT emitido pelo backend

- **Descrição**: o próprio SIFAP 2.0 gerencia credenciais e emite JWT.
- **Vantagens**: independente de terceiros.
- **Desvantagens**: reintroduz gestão de senhas (hashing, reset, política), maior superfície de ataque; não aproveita identidade governamental; contra a diretriz de SSO.

### Opção 3: API Keys / Basic Auth

- **Descrição**: chave estática por sistema/usuário.
- **Vantagens**: trivial de implementar.
- **Desvantagens**: sem identidade de usuário real; difícil de revogar/auditar; inadequado para dados sensíveis (CPF, valores de benefício); viola OWASP.

## Decisão

**Decidimos usar Gov.br via OIDC para autenticação de usuários e Spring Security com RBAC para autorização (Opção 1).**

1. **Autenticação**: fluxo Authorization Code + PKCE contra o Gov.br; o backend (resource server) valida o JWT (assinatura, `iss`, `aud`, expiração).
2. **Autorização (RBAC)**: 3 perfis — `OPERATOR`, `AUDITOR`, `ADMIN` — aplicados por `@PreAuthorize` na camada de controller/service. Exemplos:
   - Aprovar pagamento: `OPERATOR` ou `ADMIN` (espelha BR-007 do legado).
   - Emitir relatório de auditoria: `AUDITOR` ou `ADMIN`.
   - Gerir programas sociais: `ADMIN`.
3. **Serviço-a-serviço** (ex.: SIAFI, banco): **Managed Identity** no Azure; nenhum segredo em código ou variável — apenas `azurerm_key_vault_secret`.
4. **Auditoria de acesso**: toda ação sensível grava evento via `AuditRecorder` (contexto Audit), com usuário e timestamp UTC.
5. **Sem bypass herdado**: os atalhos do legado (CPF `000`, região 99, prefixos VALDOCS) **não** são reimplementados como exceções silenciosas; se necessários, viram feature flag explícita e auditada.

## Justificativa

Gov.br é o provedor de identidade padrão do governo federal: elimina gestão de senhas, oferece SSO e MFA, e casa com o ator já previsto no C4. O RBAC com 3 perfis traduz diretamente os papéis implícitos nos programas legados (operador/auditor/admin), tornando explícito o que no mainframe era controle por transação. Managed Identity remove segredos do código, atendendo OWASP e a diretriz de segurança do projeto.

## Consequências

### Positivas

- Sem armazenamento de senhas; menor superfície de ataque.
- Perfis testáveis (ex.: AUDITOR tentando aprovar pagamento → 403).
- Acesso a recursos Azure sem segredos (Managed Identity).

### Negativas

- Acoplamento ao Gov.br (disponibilidade externa) — **mitigação**: cache de JWKS e tratamento de indisponibilidade com mensagem clara; ambiente de homologação com IdP mock.
- Complexidade do fluxo OIDC — **mitigação**: usar starter `spring-boot-starter-oauth2-*` e configuração declarativa.

### Riscos

- Mapeamento incorreto de claims → perfil pode dar acesso indevido — **contingência**: testes de autorização por perfil no CI; negar por padrão (deny-by-default).

## Como saber que esta decisão envelheceu mal

- 🚨 Gov.br não atende um perfil de usuário necessário (ex.: sistema externo sem conta Gov.br) → avaliar IdP complementar.
- 🚨 Necessidade de perfis muito granulares (além de 3) → migrar de RBAC para ABAC.

## Referências

- Diretrizes de segurança do projeto (OAuth2/JWT, Managed Identity, OWASP Top 10)
- [`bounded-contexts.md`](bounded-contexts.md) — contexto Audit como destino dos eventos de acesso
- Mistérios relacionados: MYS-007 (CPF 000), MYS-008 (região 99), EGG-002 (prefixos VALDOCS) — não reimplementar como bypass
- Regras relacionadas: BR-007 (aprovação por perfil), BR-066/BR-067 (auditoria)
