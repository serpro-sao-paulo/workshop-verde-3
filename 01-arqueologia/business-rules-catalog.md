<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 | Somente beneficiários com status ativo (STATUS = 'A') são processados no ciclo de pagamento mensal. Demais são ignorados. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L164-L167` | `BENEFICIARIO.STATUS` | CRÍTICO | Confirmada — RN-SIFAP-2012 §5.1: "Todos os beneficiários ativos (BN-CD-SIT = 'A') são processados" |
| BR-002 | Beneficiários com CPF duplicado no mesmo ciclo são ignorados (proteção contra duplicatas). Apenas o primeiro registro por CPF é processado. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L158-L162` | `BENEFICIARIO.CPF` | ALTO | Inferida — não documentada, mas lógica de deduplicação no código |
| BR-003 | Um beneficiário não pode receber mais de um pagamento por competência. Se já existe pagamento para o CPF na mesma competência, o beneficiário é ignorado. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L170-L178` | `PAGAMENTO.CPF-BENEF`, `PAGAMENTO.COMPETENCIA` | CRÍTICO | Inferida — proteção contra pagamento duplo |
| BR-004 | Se o programa social vinculado ao beneficiário não é encontrado no cadastro, gera erro e o beneficiário é pulado. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L181-L190` | `BENEFICIARIO.COD-PROGRAMA`, `PROGRAMA-SOCIAL.COD-PROGRAMA` | ALTO | Inferida — tratamento de integridade referencial |
| BR-005 | Programas sociais com status inativo (STATUS-PROG ≠ 'A') não geram pagamentos — beneficiários vinculados são ignorados. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L191-L194` | `PROGRAMA-SOCIAL.STATUS-PROG` | ALTO | Inferida — coerente com lógica de status ativo |
| BR-006 | O fator regional é determinado por uma tabela fixa de 27 UFs (índices 1–27). Regiões fora de 1–25 recebem fator 1.0000. Regiões Norte/Nordeste têm fatores mais altos (1.25–1.40); Sul/Sudeste têm fatores menores (1.00–1.12). | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L115-L147` e `L200-L204` | `BENEFICIARIO.COD-REGIAO` | CRÍTICO | Inferida — tabela hardcoded. <!-- mystery: Regiões 26 e 27 recebem fator 1.0000 — que estados/territórios seriam? O Brasil tem 27 UFs mas o IF só aceita 1-25. Valores 26-27 existem na tabela mas caem no ELSE com fator 1.0. --> |
| BR-007 | Fator familiar: 0 dependentes = 1.0; 1-2 dependentes = 1.0 + (n × 0.05); 3-4 dependentes = 1.10 + ((n-2) × 0.03); 5+ dependentes = 1.16 + ((n-4) × 0.02). | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L207-L219` | `BENEFICIARIO.NUM-DEPENDENTES` | CRÍTICO | Parcialmente confirmada — RN-SIFAP-2012 §2.1 (RN-013) documenta fórmula base com acréscimo por dependente, mas não detalha as faixas progressivas |
| BR-008 | Fator de renda é determinado por 5 faixas: até R$300 → 1.0; até R$600 → 0.85; até R$1000 → 0.70; até R$1500 → 0.55; acima → 0.40. Faixa atribuída pela primeira cujo limite ≥ renda. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L149-L158` e `L365-L374` | `BENEFICIARIO.RENDA-FAMILIAR` | CRÍTICO | Confirmada — RN-SIFAP-2012 §2.2 (RN-018): "A faixa aplicável é determinada pela renda per capita familiar declarada" |
| BR-009 | Fator idade: ≥65 anos → 1.15; 60-64 anos → 1.10; <18 anos → 1.05; demais → 1.00. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L224-L235` | `BENEFICIARIO.DT-NASCIMENTO` | ALTO | Inferida — não documentada em RN-SIFAP-2012. <!-- mystery: O cálculo de idade usa apenas o ano de nascimento (sem considerar mês/dia). Beneficiário nascido em dezembro que ainda não fez aniversário será tratado com idade incorreta. Intencional ou bug? --> |
| BR-010 | Valor do benefício = VLR-BASE × FATOR-REGIONAL × FATOR-FAMILIAR × FATOR-RENDA × FATOR-IDADE × (1 + FATOR-REAJUSTE). | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L238-L241` | `PROGRAMA-SOCIAL.VLR-BASE`, `PROGRAMA-SOCIAL.FATOR-REAJUSTE` | CRÍTICO | Parcialmente confirmada — RN-SIFAP-2012 §2.1 (RN-013) documenta fórmula base, mas sem fatores de renda/região/idade |
| BR-011 | Valores monetários são truncados (não arredondados): multiplica por 100, converte para inteiro, divide por 100. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L243-L244` | Todos os campos VLR-* | CRÍTICO | Confirmada — RN-SIFAP-2012 §2.1 (RN-014): "truncamento, não arredondamento matemático" |
| BR-012 | Em dezembro (mês 12), tipo de pagamento muda para 'D' e é calculado um 13º benefício = VLR-BASE × FATOR-REGIONAL × FATOR-IDADE (sem fator familiar e renda). O bruto total soma benefício + 13º. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L249-L255` | `PAGAMENTO.TIPO-PGTO`, `PAGAMENTO.VLR-BRUTO` | CRÍTICO | Parcialmente confirmada — RN-SIFAP-2012 §6 lista "Cálculo do 13o benefício" como PENDENTE. <!-- mystery: O 13º não inclui fator familiar nem fator renda — diferente do benefício mensal normal. Intencional ou omissão? Fator-K mencionado no RN-2012 §2.1 pode ser o FATOR-REAJUSTE? --> |
| BR-013 | Em dezembro, programas tipo 'A' recebem abono natalino de 15% do valor mensal (VLR-BENF × 0.15), somado ao bruto total. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L256-L261` | `PROGRAMA-SOCIAL.TIPO`, `PAGAMENTO.VLR-ABONO` | CRÍTICO | Inferida — não documentada. <!-- mystery: O que define TIPO-PROG = 'A'? Apenas alguns programas sociais recebem abono. A documentação não explica os tipos possíveis de programa ('A' vs outros). --> |
| BR-014 | Desconto simplificado de 3% aplicado apenas quando VLR-BRUTO > R$500,00. Caso contrário, desconto é zero. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L265-L269` | `PAGAMENTO.VLR-DESCONTO`, `PAGAMENTO.VLR-BRUTO` | ALTO | Inferida — difere do CALCDSCT que tem lógica mais complexa com tipos de desconto. <!-- mystery: Comentário diz "CALC DESCONTOS SIMPLIFICADO" — por que não chama CALLNAT CALCDSCT como documentado no RN-2012 §5.1? O cabeçalho do programa diz "CHAMA CALCBENF E CALCDSCT" mas o código não faz CALLNAT para nenhum dos dois. --> |
| BR-015 | Valor líquido = bruto - desconto. Se líquido ficar negativo, é ajustado para zero. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L273-L276` | `PAGAMENTO.VLR-LIQUIDO` | ALTO | Inferida — proteção contra valor negativo |
| BR-016 | Pagamento gerado recebe status 'G' (gerado), não 'P' (pendente) como documentado. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L286` | `PAGAMENTO.STATUS-PGTO` | ALTO | <!-- mystery: RN-SIFAP-2012 §5.1 diz status 'P' (pendente), mas o código usa 'G' (gerado). Divergência entre documentação e código — qual é o correto? --> |
| BR-017 | Processamento é sequencial por CPF (otimização de 2000, substituiu ordem anterior). Nota no código avisa: "SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO". | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L152-L156` | `BENEFICIARIO.CPF` | MÉDIO | Parcialmente confirmada — RN-SIFAP-2012 §5.1 diz "ordenação padrão" e nota explica ordenação por nome, mas código ordena por CPF (alterado em 2000) |
| BR-018 | Operação de cadastro aceita apenas 'I' (inclusão) ou 'A' (alteração). Qualquer outro valor é rejeitado. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L97-L101` | — | MÉDIO | Inferida — validação de input na fronteira |
| BR-019 | CPF é campo obrigatório para cadastro de beneficiário. CPF = 0 é rejeitado. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L103-L107` | `BENEFICIARIO.CPF` | CRÍTICO | Confirmada — RN-SIFAP-2012 §1.1 (RN-001): "Todo beneficiário deve possuir CPF válido" |
| BR-020 | CPF é validado pelo algoritmo módulo 11 da Receita Federal (dois dígitos verificadores). CPF inválido bloqueia inclusão/alteração. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L109-L114` e `L224-L272` | `BENEFICIARIO.CPF` | CRÍTICO | Confirmada — RN-SIFAP-2012 §1.1 (RN-001) e Manual Técnico §3.2.1. <!-- mystery: A validação não rejeita CPFs com todos os dígitos iguais (ex.: 111.111.111-11) que passam no módulo 11 mas são inválidos na prática. Bug ou omissão intencional? --> |
| BR-021 | Nome é campo obrigatório — nome em branco bloqueia o cadastro. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L116-L120` | `BENEFICIARIO.NOME` | MÉDIO | Inferida — validação básica |
| BR-022 | Data de nascimento é obrigatória. Data = 0 é rejeitada. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L122-L126` | `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | Confirmada — RN-SIFAP-2012 §1.1 (RN-006): "Data de nascimento é campo obrigatório" |
| BR-023 | Sexo aceita apenas 'M' ou 'F'. Qualquer outro valor é rejeitado. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L128-L132` | `BENEFICIARIO.SEXO` | BAIXO | Inferida — validação de domínio |
| BR-024 | Não é permitido incluir beneficiário com CPF já existente no cadastro (duplicidade). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L135-L142` | `BENEFICIARIO.CPF` | CRÍTICO | Confirmada — RN-SIFAP-2012 §1.1 (RN-002): "Não é permitida a inclusão de beneficiário com CPF já existente" <!-- mystery: A documentação diz que beneficiários excluídos (status 'E') podem ser reincluídos, mas o código simplesmente verifica se o CPF existe SEM checar o status. Beneficiários excluídos logicamente NÃO podem ser recadastrados pelo código atual. Divergência doc vs código. --> |
| BR-025 | Alteração exige que o beneficiário já exista no cadastro. CPF não encontrado bloqueia a alteração. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L144-L148` | `BENEFICIARIO.CPF` | MÉDIO | Inferida — validação de existência |
| BR-026 | Na inclusão, o status inicial do beneficiário é automaticamente 'A' (ativo). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L155-L157` | `BENEFICIARIO.STATUS` | ALTO | Inferida — não documentada explicitamente |
| BR-027 | Beneficiários com idade > 75 anos têm o status automaticamente alterado para 'S' (suspenso), sobrescrevendo o status padrão 'A'. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L160-L162` | `BENEFICIARIO.STATUS`, `BENEFICIARIO.DT-NASCIMENTO` | CRÍTICO | Inferida — alteração de 2011 (JOSE FERREIRA - "AJUSTE STATUS IDOSO"). <!-- mystery: MYS-001 provável — programa modifica silenciosamente status baseado em critério demográfico (idade > 75). Por que 75 e não 65 (idade do Estatuto do Idoso)? Possível relação com revisão cadastral obrigatória ou BPC/LOAS que exige recadastramento periódico para idosos. Não documentada em nenhum lugar. --> |
| BR-028 | Na alteração, o CPF, código de programa e código de região NÃO são atualizados — apenas nome, endereço, município, UF, CEP, telefone, RG, status, renda e dependentes. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L179-L195` | `BENEFICIARIO.*` | ALTO | Inferida — <!-- mystery: A alteração de CPF documentada em RN-009 ("exige autorização nível 2") não está implementada neste programa. Não há verificação de perfil SUPERVISOR no código. Onde está essa funcionalidade? Pode ser em outro programa não catalogado, ou a regra foi documentada mas nunca implementada. --> |
| BR-029 | Toda operação de inclusão/alteração registra a data atual como DT-ATUALIZACAO. Na inclusão, DT-CADASTRO também recebe a data atual. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L176-L177` e `L192` | `BENEFICIARIO.DT-CADASTRO`, `BENEFICIARIO.DT-ATUALIZACAO` | MÉDIO | Inferida — rastreabilidade temporal |
| BR-030 | O programa NÃO chama LOGAUDIT para registro de auditoria, apesar do Manual Técnico §3.2.1 afirmar que "Registro de auditoria para todas as operações (chama subprograma LOGAUDIT)". | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN` (ausência) | `AUDITORIA.*` | ALTO | <!-- mystery: Manual diz que CADBENEF chama LOGAUDIT, RN-010 diz "Toda alteração cadastral gera registro de auditoria automático via LOGAUDIT". Mas NÃO há CALLNAT 'LOGAUDIT' no código. Auditoria está quebrada ou foi removida? --> |
| BR-031 | O programa NÃO valida: NIS (apenas armazena), código de região (aceita qualquer valor), código de programa (não verifica existência no DDM PROGRAMA-SOCIAL), dados bancários (não existem no programa). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN` (ausência) | `BENEFICIARIO.NIS`, `BENEFICIARIO.COD-REGIAO`, `BENEFICIARIO.COD-PROGRAMA` | ALTO | <!-- mystery: RN-001 diz "NIS/NIT ativo (validação via VALNISN)", RN-003 diz "vinculado a programa social ativo", RN-005 diz "região válida 01-27", RN-007 diz "dados bancários obrigatórios". NENHUMA dessas validações está implementada. Gap enorme entre documentação e código. --> |
| BR-032 | Máximo de 5 dependentes por beneficiário. Tentativa de incluir o 6º é bloqueada. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L59-L62` | `BENEFICIARIO.NUM-DEPENDENTES`, `BENEFICIARIO.DEPENDENTES` (PE) | ALTO | Inferida — limite hardcoded sem justificativa documentada |
| BR-033 | Parentesco de dependente aceita apenas: FI (filho), CO (cônjuge), IR (irmão), OU (outro). Outros valores são rejeitados. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L76-L80` | `BENEFICIARIO.DEPENDENTES.PARENTESCO` | MÉDIO | Inferida — validação de domínio |
| BR-034 | Não é permitido incluir dependente com CPF já cadastrado para o mesmo titular (deduplicação por CPF dentro do PE group). CPF = 0 é ignorado na verificação. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L87-L94` | `BENEFICIARIO.DEPENDENTES.CPF-DEP` | ALTO | Inferida — proteção contra duplicidade |
| BR-035 | Beneficiários com status 'C' (cancelado) ou 'D' (desligado) não permitem inclusão de dependentes. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L52-L55` | `BENEFICIARIO.STATUS` | ALTO | Inferida — restrição de operação por status |
| BR-036 | Na inclusão de programa social, o VLR-BASE é ajustado pelo Fator-K antes de gravar: `VLR-BASE-GRAVADO = VLR-BASE-DIGITADO × (1.00 + FATOR-REAJ × 0.347215)`. O usuário digita um valor, mas o sistema grava outro. | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L85-L86` | `PROGRAMA-SOCIAL.VLR-BASE`, `PROGRAMA-SOCIAL.FATOR-REAJUSTE` | CRÍTICO | Inferida — constante mágica 0.347215. <!-- mystery: MYS-003 confirmado — Fator-K do RN-SIFAP-2012 §2.1 é esta fórmula. Mas por que 0.347215? Nenhuma documentação explica a origem desta constante. Possível fator de conversão monetária ou índice econômico congelado. --> |
| BR-037 | Tipos de programa social: 'A' (assistencial), 'P' (previdenciário), 'T' (trabalho). | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L62` | `PROGRAMA-SOCIAL.TIPO` | ALTO | Inferida — domínio de valores no campo TIPO |
| BR-038 | Não é permitido incluir programa social com código já existente. | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L79-L83` | `PROGRAMA-SOCIAL.COD-PROGRAMA` | MÉDIO | Inferida — unicidade |
| BR-039 | Na inclusão de programa, status é automaticamente 'A' (ativo). Não há operação de alteração ou inativação implementada. | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L90` | `PROGRAMA-SOCIAL.STATUS-PROG` | ALTO | Inferida — <!-- mystery: Não existe funcionalidade para inativar um programa social. Como STATUS-PROG vira ≠ 'A'? Atualização direta no Adabas? --> |
| BR-040 | Contribuição social obrigatória é progressiva: VLR-BRUTO ≤ R$500 → 3%; ≤ R$1000 → 5%; ≤ R$2000 → 7%; > R$2000 → 9%. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L53-L60` e `L155-L165` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-DESCONTO` | CRÍTICO | Inferida — tabela hardcoded. Difere do desconto simplificado de 3% do BATCHPGT (BR-014). |
| BR-041 | Teto de desconto total é 30% do VLR-BRUTO, EXCETO descontos judiciais (tipo 'J') que não têm teto. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L101-L105` e `L117-L120` | `PAGAMENTO.VLR-BRUTO`, `BENEFICIARIO.DESCONTOS.TIPO-DSCT` | CRÍTICO | Inferida — regra financeira. Judicial prevalece sobre teto. |
| BR-042 | Descontos têm vigência: DT-INICIO-DSCT e DT-FIM-DSCT. Desconto vencido (DT-FIM < hoje) ou futuro (DT-INICIO > hoje) é ignorado. DT-FIM = 0 = indeterminado. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L93-L99` | `BENEFICIARIO.DESCONTOS.DT-INICIO-DSCT`, `BENEFICIARIO.DESCONTOS.DT-FIM-DSCT` | ALTO | Inferida — controle de vigência temporal |
| BR-043 | Desconto judicial: valor fixo OU percentual. Se VLR-DSCT > 0, usa valor fixo; senão usa PCT-DSCT. Pensão alimentícia e administrativo seguem mesma lógica. Imposto e sindical são apenas percentuais. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L106-L141` | `BENEFICIARIO.DESCONTOS.*` | CRÍTICO | Inferida — 6 tipos: C, I, J, S, P, A. Sindical fixo em 1%. |
| BR-044 | Correção retroativa de pagamentos usa índice IPCA acumulado do período. Fórmula: VLR-CORRIGIDO = VLR-ORIGINAL × IPCA-ACUMULADO. Só aplica se diferença > 0. | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L133-L142` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-CORRECAO` | CRÍTICO | Inferida — tabela IPCA hardcoded (2010-2012 apenas). <!-- mystery: Tabela IPCA parou em 2012. Correções após 2012 usariam índice zero (tabela não encontrada). Bug silencioso? --> |
| BR-045 | Pagamento já corrigido (IND-CORRIGIDO = 'S') não é reprocessado — idempotência da correção. | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L125-L127` | `PAGAMENTO.IND-CORRIGIDO` | ALTO | Inferida — proteção contra correção dupla |
| BR-046 | Código comentado de correção do Plano Verão (1989-1991) com fator 2.75 e subfator 1.4289 para antes de jul/1989. | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L83-L94` | — | BAIXO | <!-- mystery: Código comentado mas preservado "para referência histórica". Fator 2.75 × 1.4289 = 3.929475. Que conversão monetária era essa? Cruzado → Cruzeiro? --> |
| BR-047 | Consulta de beneficiário permite busca por CPF ou por NIS. Default é CPF quando tipo de busca não informado. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L75-L90` | `BENEFICIARIO.CPF`, `BENEFICIARIO.NIS` | BAIXO | Inferida — funcionalidade de consulta |
| BR-048 | O sistema possui 5 status de beneficiário: A (ativo), S (suspenso), C (cancelado), I (inativo), D (desligado). | `01-arqueologia/legado-sifap/natural-programs/CONSBENEF.NSN#L93-L107` | `BENEFICIARIO.STATUS` | ALTO | Confirmada — mapeamento completo em CONSBENF |
| BR-049 | CPF é mascarado na exibição: formato `***.***. XXX-XX` (3 primeiros dígitos ocultos, 6 últimos visíveis). Bug documentado: CPFs com menos de 11 dígitos exibem os 3 primeiros ao invés dos últimos. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L136-L152` | `BENEFICIARIO.CPF` | MÉDIO | <!-- mystery: Comentário no código: "INCONSISTENCIA CONHECIDA - AS VEZES MOSTRA PRIMEIROS 3 DIGITOS AO INVES DOS ULTIMOS. NAO CORRIGIR SEM APROVACAO DA AUDITORIA." Bug conhecido há anos, nunca corrigido por questão de auditoria. --> |
| BR-050 | Validação de CPF completa: módulo 11 + rejeição de CPFs com todos dígitos iguais, EXCETO CPFs iniciados com 000 que são aceitos (teste governo). | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L123-L164` | `BENEFICIARIO.CPF` | CRÍTICO | <!-- mystery: CPFs 000.000.000-00 são aceitos como válidos ("EXCECAO: CPFs INICIADOS COM 000 SAO VALIDOS - TESTE GOVERNO"). Backdoor de teste em produção? --> |
| BR-051 | Validação de nome exige pelo menos um espaço (nome + sobrenome). Nome em branco ou sem sobrenome é rejeitado. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L182-L195` | `BENEFICIARIO.NOME` | MÉDIO | Inferida — validação mínima de completude |
| BR-052 | Validação de UF contra tabela de 27 UFs válidas do Brasil (ordenadas alfabeticamente AC a TO). UF em branco é aceita (não obrigatória). | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L70-L99` e `L129-L140` | `BENEFICIARIO.UF` | MÉDIO | Inferida — tabela hardcoded |
| BR-053 | Validação de data de nascimento: ano entre 1900 e ano atual, mês 1-12, dia dentro do limite do mês. Fevereiro aceita até 29 (sempre considera bissexto). | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L100-L115` e `L167-L179` | `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | <!-- mystery: Não valida realmente ano bissexto — aceita 29/02 para qualquer ano. Bug menor. --> |
| BR-054 | Documentos com prefixos especiais de CPF (000, 001, 002, 010, 011, 099, 100, 999) bypassa TODA validação — CPF, RG, tudo é marcado como válido e erros são zerados. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L130-L141` | `BENEFICIARIO.CPF` | CRÍTICO | <!-- mystery: Backdoor de teste/governo em produção. 8 prefixos diferentes. Quem usa? Testes automatizados, CPFs de teste federal, ou vulnerabilidade? --> |
| BR-055 | RG deve ter pelo menos 5 caracteres. RG em branco é rejeitado. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L115-L128` | `BENEFICIARIO.RG` | BAIXO | Inferida — validação mínima |
| BR-056 | Beneficiário com região 99 (internacional/diplomático) é automaticamente elegível, ignorando TODAS as demais verificações. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L81-L85` | `BENEFICIARIO.COD-REGIAO` | CRÍTICO | <!-- mystery: MYS-005 provável — bypass total de elegibilidade para "região especial". Quem define região 99? Quantos beneficiários têm esse código? --> |
| BR-057 | Elegibilidade por tipo de programa: A (assistencial) — renda > R$600 sem dependentes é inelegível + docs obrigatórios; P (previdenciário) — idade mínima 60; T (trabalho) — idade entre 16 e 65. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L112-L140` | `BENEFICIARIO.RENDA-FAMILIAR`, `BENEFICIARIO.NUM-DEPENDENTES`, `BENEFICIARIO.DOCUMENTOS-OK`, `BENEFICIARIO.DT-NASCIMENTO` | CRÍTICO | Inferida — regras centrais de elegibilidade por tipo |
| BR-058 | Elegibilidade verifica faixa etária do programa: se IDADE-MIN > 0 e idade < mínimo → inelegível; se IDADE-MAX > 0 e idade > máximo → inelegível. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L95-L108` | `PROGRAMA-SOCIAL.IDADE-MIN`, `PROGRAMA-SOCIAL.IDADE-MAX` | ALTO | Inferida — parametrizado por programa |
| BR-059 | COD-ELEGIBILIDADE é interpretado posicionalmente: posição 1 = 'R' → requer NIS cadastrado; posição 2 = 'D' → requer dependentes. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L148-L162` | `PROGRAMA-SOCIAL.COD-ELEGIBILIDADE`, `BENEFICIARIO.NIS`, `BENEFICIARIO.NUM-DEPENDENTES` | ALTO | <!-- mystery: Código de elegibilidade tem 5 posições (A5) mas só 2 são interpretadas. Posições 3-5 são ignoradas. Existiam regras adicionais que foram removidas? --> |
| BR-060 | Conciliação bancária: retorno CNAB 240 com código '00' → pagamento muda para status 'P' (pago); '01' → 'D' (devolvido); '02' → 'E' (estornado). | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L142-L170` | `PAGAMENTO.STATUS-PGTO`, `PAGAMENTO.COD-RETORNO`, `PAGAMENTO.DT-PAGAMENTO` | CRÍTICO | Inferida — transição de status G → P/D/E via conciliação |
| BR-061 | Tolerância de R$0.01 na conciliação: diferença absoluta entre VLR-LIQUIDO do SIFAP e valor do retorno bancário ≤ R$0.01 é considerada conciliada. Acima disso, é divergência. | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L130-L137` | `PAGAMENTO.VLR-LIQUIDO` | CRÍTICO | Inferida — <!-- mystery: Tolerância de 1 centavo. É por causa de arredondamento bancário vs truncamento SIFAP? Relação com BR-011 (truncamento)? --> |
| BR-062 | Divergências de conciliação geram registro de auditoria com ação 'DV', incluindo valor SIFAP e valor banco. Conciliações bem-sucedidas geram ação 'CO'. | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L180-L210` | `AUDITORIA.ACAO`, `AUDITORIA.VLR-ANTERIOR`, `AUDITORIA.VLR-NOVO` | ALTO | Inferida — trilha de auditoria automática |
| BR-063 | Relatório consolidado usa ARREDONDAMENTO (+0.005) para acumular valores brutos, enquanto o cálculo de pagamento (BATCHPGT/CALCBENF) usa TRUNCAMENTO. Isso causa diferença acumulativa nos totais. | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L131-L135` | `PAGAMENTO.VLR-BRUTO` | ALTO | <!-- mystery: Bug ou decisão intencional? Relatório gerencial mostra totais diferentes dos pagamentos reais. Em volume alto, a diferença pode ser significativa. --> |
| BR-064 | Mapeamento COD-REGIAO → macro-região: 1-5 = Norte, 6-10 = Nordeste, 11-15 = Sudeste, 16-20 = Sul, 21-25 = Centro-Oeste. Códigos > 25 caem em Centro-Oeste. | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L109-L126` | `BENEFICIARIO.COD-REGIAO` | MÉDIO | Inferida — mapeamento de regiões para relatório. <!-- mystery: Região 99 (diplomático, ver BR-056) cairia em Centro-Oeste no relatório — provavelmente errado. --> |
| BR-065 | Status de pagamento completo no sistema: G (gerado), P (pago), C (cancelado), D (devolvido), E (estornado). Status desconhecidos caem no bucket 'G' (gerado) no relatório. | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L137-L151` | `PAGAMENTO.STATUS-PGTO` | MÉDIO | Confirmada — 5 estados de pagamento confirmados por múltiplos programas |
| BR-066 | No relatório de auditoria, registros com ação 'EX' (exclusão) são FILTRADOS e não aparecem no relatório, independente dos filtros aplicados. | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L91-L94` | `AUDITORIA.ACAO` | CRÍTICO | <!-- mystery: MYS-007 provável — exclusões são ocultadas do relatório de auditoria. Por que? Requisito legal de preservar exclusões mas não exibi-las? Ou tentativa de esconder operações sensíveis? --> |
| BR-067 | Ações de auditoria: IN (inclusão), AL (alteração), CO (conciliação), CN (consulta), DV (divergência), EX (exclusão — oculta). | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L107-L122` | `AUDITORIA.ACAO` | ALTO | Confirmada — domínio completo de ações |
| BR-068 | Tipo de pagamento possui 3 valores: N (normal), D (décimo/13º), T (terceiro). | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L105-L114` | `PAGAMENTO.TIPO-PGTO` | MÉDIO | Confirmada — <!-- mystery: Tipo 'T' (terceiro) aparece no RELPGT e no comentário do CALCBENF mas nenhum programa gera pagamento com tipo 'T'. Existe código não catalogado que gera isso, ou é um tipo legado descontinuado? --> |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

- **BR-006** Fator regional (tabela 27 UFs)
- **BR-007** Fator familiar (faixas por dependentes)
- **BR-008** Fator renda (5 faixas)
- **BR-009** Fator idade (idosos e menores)
- **BR-010** Fórmula principal do benefício
- **BR-011** Truncamento de valores (não arredondamento)
- **BR-012** 13º benefício em dezembro
- **BR-013** Abono natalino 15% para tipo 'A'
- **BR-014** Desconto simplificado 3% (bruto > R$500) — BATCHPGT
- **BR-036** Fator-K: ajuste de VLR-BASE na inclusão de programa (constante 0.347215)
- **BR-040** Contribuição social progressiva (3%–9%) — CALCDSCT
- **BR-041** Teto 30% de desconto, exceto judicial
- **BR-042** Vigência temporal de descontos (DT-INICIO / DT-FIM)
- **BR-043** 6 tipos de desconto: C, I, J, S, P, A (fixo ou percentual)
- **BR-044** Correção retroativa por IPCA acumulado
- **BR-061** Tolerância R$0.01 na conciliação bancária
- **BR-063** Bug: arredondamento no relatório vs truncamento no cálculo

### Validações de Cadastro

- **BR-019** CPF obrigatório
- **BR-020** CPF validado por módulo 11 (CADBENEF — sem rejeição de iguais)
- **BR-021** Nome obrigatório
- **BR-022** Data nascimento obrigatória
- **BR-023** Sexo M/F
- **BR-024** CPF único (sem duplicidade)
- **BR-025** Alteração exige beneficiário existente
- **BR-031** Validações AUSENTES: NIS, região, programa, dados bancários
- **BR-032** Máximo 5 dependentes
- **BR-033** Parentesco: FI/CO/IR/OU
- **BR-034** Dependente com CPF duplicado rejeitado
- **BR-038** Programa social com código duplicado rejeitado
- **BR-050** CPF completo: módulo 11 + todos iguais + exceção 000 (VALBENEF)
- **BR-051** Nome + sobrenome obrigatórios (mínimo 1 espaço)
- **BR-052** UF válida (27 UFs) — opcional
- **BR-053** Data nascimento: ano 1900-atual, mês 1-12, dia válido
- **BR-054** Prefixos especiais de CPF bypassam TODA validação (VALDOCS)
- **BR-055** RG mínimo 5 caracteres

### Validações de Status

- **BR-001** Somente beneficiários ativos (STATUS = 'A') processados
- **BR-005** Programas inativos (STATUS-PROG ≠ 'A') não geram pagamentos
- **BR-016** Status de pagamento gerado = 'G' (diverge da doc que diz 'P')
- **BR-026** Inclusão → status inicial 'A' (ativo)
- **BR-027** Idade > 75 → status forçado para 'S' (suspenso) — MYS-001
- **BR-035** Status C/D bloqueia inclusão de dependentes
- **BR-039** Programa incluído sempre com status 'A' (sem inativação)
- **BR-048** 5 status de beneficiário: A, S, C, I, D
- **BR-060** Transição de status pagamento via conciliação: G→P, G→D, G→E
- **BR-065** 5 status de pagamento: G, P, C, D, E

### Regras de Elegibilidade

- **BR-056** Região 99 = bypass total de elegibilidade (diplomático)
- **BR-057** Tipo A: renda ≤ R$600 ou dependentes + docs; Tipo P: idade ≥ 60; Tipo T: 16-65
- **BR-058** Faixa etária parametrizada por programa (IDADE-MIN / IDADE-MAX)
- **BR-059** COD-ELEGIBILIDADE posicional: R=requer NIS, D=requer dependentes

### Regras de Autorização / Segurança

- **BR-049** CPF mascarado na consulta (`***.***. XXX-XX`)
- **BR-054** Backdoor: prefixos especiais (000-002, 010, 011, 099, 100, 999)
- **BR-066** Exclusões (ação 'EX') ocultas do relatório de auditoria

### Regras de Negócio Temporais

- **BR-003** Idempotência por competência — não gera pagamento duplicado no mesmo mês
- **BR-012** Em dezembro, processamento especial com 13º benefício
- **BR-017** Ordenação por CPF (dependência de sistemas downstream)
- **BR-045** Correção retroativa: pagamento já corrigido não é reprocessado

### Conciliação e Auditoria

- **BR-060** CNAB 240: retorno '00' → pago, '01' → devolvido, '02' → estornado
- **BR-062** Divergências geram auditoria 'DV', conciliações geram 'CO'
- **BR-067** Ações de auditoria: IN, AL, CO, CN, DV, EX

### Tipos e Domínios

- **BR-037** Tipos de programa: A (assistencial), P (previdenciário), T (trabalho)
- **BR-064** Macro-regiões: 1-5=Norte, 6-10=Nordeste, 11-15=Sudeste, 16-20=Sul, 21-25=C.Oeste
- **BR-068** Tipos de pagamento: N (normal), D (décimo), T (terceiro)

## Resumo Estatístico

- Total de regras encontradas: **68** (15 programas cobertos = 100%)
- Regras críticas: **22** (BR-001, BR-006–BR-008, BR-010–BR-013, BR-019, BR-020, BR-024, BR-027, BR-036, BR-040, BR-041, BR-043, BR-044, BR-050, BR-054, BR-056, BR-057, BR-061, BR-066)
- Regras com duplicação: **3** (tabela regional em BATCHPGT + CALCBENF; desconto simplificado em BATCHPGT + CALCBENF; validação CPF em CADBENEF + VALBENEF + VALDOCS)
- Regras sem documentação (escondidas): **18**
- Divergências documentação vs código: **7** (BR-016 status G/P, BR-024 reinclusão, BR-028 CPF, BR-030 LOGAUDIT, BR-031 validações, BR-014 vs BR-040 descontos, BR-063 round vs truncate)
- Mistérios sinalizados: **15** (ver marcadores `<!-- mystery: -->` nas notas)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

