<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1   | `BENF` | Benefício / Beneficiário | `BATCHPGT.NSN`, `CADBENEF.NSN` | Abreviação usada em nomes de variáveis (#VLR-BENF) e nomes de programa (CADBENEF, CONSBENF, VALBENEF, CALCBENF) |
| 2   | `DSCT` | Desconto | `BATCHPGT.NSN`, `CALCDSCT.NSN` | Tipo de dedução sobre valor bruto. Tipos conhecidos: 'J' (judicial), 'I' (imposto). Variável #VLR-DESC |
| 3   | `PGT` | Pagamento | `BATCHPGT.NSN` | Sufixo de programas batch de pagamento. Também aparece como PGTO em STATUS-PGTO |
| 4   | `VLR` | Valor | `BATCHPGT.NSN`, `CADBENEF.NSN` | Prefixo para campos monetários: VLR-BRUTO, VLR-DESCONTO, VLR-LIQUIDO, VLR-ABONO, VLR-BASE |
| 5   | `CPF` | Cadastro de Pessoa Física | `CADBENEF.NSN`, `BATCHPGT.NSN` | Identificador principal de beneficiários (N11). Chave de busca e deduplicação |
| 6   | `NIS` | Número de Identificação Social | `CADBENEF.NSN`, `BATCHPGT.NSN` | Identificador social (N11). Armazenado mas NÃO validado em CADBENEF |
| 7   | `DT` | Data | `BATCHPGT.NSN`, `CADBENEF.NSN` | Prefixo para campos de data no formato N8 (AAAAMMDD): DT-NASCIMENTO, DT-GERACAO, DT-CADASTRO, DT-ATUALIZACAO |
| 8   | `COD` | Código | `BATCHPGT.NSN`, `CADBENEF.NSN` | Prefixo para classificadores: COD-PROGRAMA, COD-REGIAO |
| 9   | `NUM` | Número | `BATCHPGT.NSN`, `CADBENEF.NSN` | Prefixo para quantidades/sequenciais: NUM-PAGTO, NUM-DEPENDENTES |
| 10  | `COMPETENCIA` | Mês/Ano de referência do pagamento | `BATCHPGT.NSN` | Formato N6 = AAAAMM. Identifica o ciclo mensal. Usado para evitar pagamento duplicado |
| 11  | `STATUS` | Situação cadastral do beneficiário | `CONSBENF.NSN`, `VALBENEF.NSN`, `CADBENEF.NSN` | 5 valores: 'A' (ativo), 'S' (suspenso), 'C' (cancelado), 'I' (inativo), 'D' (desligado). Campo A1 |
| 12  | `STATUS-PGTO` | Situação do pagamento | `BATCHPGT.NSN`, `BATCHCON.NSN`, `BATCHREL.NSN` | 5 valores: 'G' (gerado), 'P' (pago), 'C' (cancelado), 'D' (devolvido), 'E' (estornado). Transição via conciliação CNAB |
| 13  | `TIPO-PGTO` | Tipo de pagamento | `BATCHPGT.NSN`, `RELPGT.NSN`, `CALCBENF.NSN` | 'N' (normal), 'D' (décimo/13º), 'T' (terceiro — nunca gerado por nenhum programa catalogado) |
| 14  | `TIPO-PROG` / `TIPO` | Tipo de programa social | `CADPROG.NSN`, `BATCHPGT.NSN`, `VALELEG.NSN` | 'A' (assistencial — abono 15%), 'P' (previdenciário — idade ≥60), 'T' (trabalho — 16-65) |
| 15  | `FATOR-REG` | Fator regional | `BATCHPGT.NSN` | Multiplicador por UF (1.00 a 1.40). Tabela hardcoded com 27 posições. Norte/Nordeste têm fatores mais altos |
| 16  | `FATOR-FAM` | Fator familiar | `BATCHPGT.NSN` | Multiplicador por número de dependentes. Progressivo: 0 dep=1.0, 1-2=+5% cada, 3-4=+3% cada, 5+=+2% cada |
| 17  | `FATOR-RND` | Fator renda | `BATCHPGT.NSN` | Multiplicador por faixa de renda familiar. 5 faixas: até R$300=1.0, até R$600=0.85, até R$1000=0.70, até R$1500=0.55, acima=0.40 |
| 18  | `FATOR-IDADE` | Fator idade | `BATCHPGT.NSN` | Multiplicador por faixa etária: ≥65=1.15, 60-64=1.10, <18=1.05, demais=1.00 |
| 19  | `FATOR-REAJ` | Fator de reajuste | `BATCHPGT.NSN` | Índice anual de reajuste definido por decreto. Vem do DDM PROGRAMA-SOCIAL. Aplicado como (1 + FATOR-REAJ) |
| 20  | `ABONO` | Abono natalino | `BATCHPGT.NSN` | Parcela extra de 15% em dezembro, apenas para programas tipo 'A'. Variável VLR-ABONO |
| 21  | `13O` / `VLR-13` | Décimo terceiro benefício | `BATCHPGT.NSN` | Benefício extra em dezembro = VLR-BASE × FATOR-REG × FATOR-IDADE (sem fator familiar/renda) |
| 22  | `CALLNAT` | Chamada de subprograma Natural | Linguagem Natural | Instrução que chama programa externo passando parâmetros por referência. Equivalente a function call |
| 23  | `STORE` | Gravação de registro Adabas | Linguagem Natural | Instrução para inserir novo registro no banco. Equivalente a INSERT |
| 24  | `UPDATE` | Atualização de registro Adabas | Linguagem Natural | Instrução para alterar registro existente. Equivalente a UPDATE SQL |
| 25  | `FIND` | Busca de registros Adabas | Linguagem Natural | Instrução de busca por descritor (índice). Equivalente a SELECT...WHERE |
| 26  | `READ` | Leitura sequencial Adabas | Linguagem Natural | Leitura ordenada por descritor. Equivalente a SELECT...ORDER BY com cursor |
| 27  | `ESCAPE TOP` | Pula para próxima iteração | Linguagem Natural | Equivalente a `continue` em Java/JavaScript |
| 28  | `ESCAPE BOTTOM` | Sai do loop atual | Linguagem Natural | Equivalente a `break` em Java/JavaScript |
| 29  | `FNR` | File Number (Adabas) | DDMs | Número do arquivo no Adabas: 150=BENEFICIARIO, 151=PROGRAMA-SOCIAL, 152=PAGAMENTO, 153=AUDITORIA |
| 30  | `DE` | Descritor (Descriptor) | DDMs | Campo indexado no Adabas. Equivalente a índice/chave em PostgreSQL |
| 31  | `MU` | Multiple Value (campo multivalorado) | DDMs, `PROGRAMA-SOCIAL.ddm` | Campo que pode conter múltiplos valores (array). Em PostgreSQL → tabela separada ou JSONB |
| 32  | `PE` | Periodic Group (grupo periódico) | DDMs, `PROGRAMA-SOCIAL.ddm` | Grupo repetitivo de campos (como tabela embedded). Em PostgreSQL → @OneToMany ou array JSONB |
| 33  | `OPER` | Operação | `CADBENEF.NSN` | Flag de tipo de operação: 'I' (inclusão) ou 'A' (alteração). Determina se faz STORE ou UPDATE |
| 34  | `SIFAP` | Sistema de Fiscalização e Administração de Pagamentos | Todos | Nome do sistema legado. Em produção desde 1998. Natural 6.3 / Adabas 7.4 |
| 35  | `CNAB` | Centro Nacional de Automação Bancária | `BATCHPGT.NSN` | Padrão de arquivo para remessa bancária (layout 240 posições). Gerado pelo BATCHPGT para Banco do Brasil/CAIXA |
| 36  | `SIAFI` | Sistema Integrado de Administração Financeira | `BATCHCON.NSN` | Sistema federal de controle financeiro. BATCHCON faz conciliação SIFAP ↔ SIAFI |
| 37  | `BN-` | Prefixo de campos de Beneficiário | DDM BENEFICIARIO | Convenção SUPDE: BN-NR-CPF, BN-NM-BENEF, BN-CD-SIT, BN-DT-NASC |
| 38  | `PG-` | Prefixo de campos de Pagamento | DDM PAGAMENTO | Convenção SUPDE: PG-VL-BRUTO, PG-VL-LIQ, PG-DT-CRED, PG-NR-SEQ |
| 39  | `PS-` | Prefixo de campos de Programa Social | DDM PROGRAMA-SOCIAL | Convenção SUPDE: PS-CD-PROG, PS-NM-PROG, PS-VL-MIN, PS-IN-ATIVO |
| 40  | `AU-` | Prefixo de campos de Auditoria | DDM AUDITORIA | Convenção SUPDE: AU-DT-OCORR (data da ocorrência de auditoria) |
| 41  | `PARENTESCO` | Grau de parentesco do dependente | `CADDEPEND.NSN` | Valores: 'FI' (filho), 'CO' (cônjuge), 'IR' (irmão), 'OU' (outro). Campo A2 no PE group DEPENDENTES |
| 42  | `FATOR-K` | Fator de ajuste de valor base | `CADPROG.NSN` | Fórmula: `1.00 + (FATOR-REAJ × 0.347215)`. Constante mágica 0.347215. Ajusta VLR-BASE antes de gravar no DDM |
| 43  | `COD-ELEG` / `COD-ELEGIBILIDADE` | Código de elegibilidade do programa | `CADPROG.NSN`, `VALELEG.NSN` | Campo A5 posicional: pos.1='R' requer NIS; pos.2='D' requer dependentes. Posições 3-5 não interpretadas |
| 44  | `TIPO-DSCT` | Tipo de desconto | `CALCDSCT.NSN` | 6 tipos: 'C' (contribuição), 'I' (imposto), 'J' (judicial), 'S' (sindical), 'P' (pensão), 'A' (administrativo) |
| 45  | `PCT-DSCT` | Percentual de desconto | `CALCDSCT.NSN` | Campo N3.2 no PE group DESCONTOS. Usado quando VLR-DSCT = 0. Sindical fixo 1% |
| 46  | `VLR-CORRECAO` | Valor corrigido (correção monetária) | `CALCCORR.NSN` | Resultado de VLR-ORIGINAL × IPCA-ACUMULADO. Gravado no DDM PAGAMENTO |
| 47  | `IND-CORRIGIDO` | Indicador de pagamento já corrigido | `CALCCORR.NSN` | Campo A1: 'S' = já corrigido (idempotência). Evita reprocessamento de correção retroativa |
| 48  | `IPCA` | Índice de Preços ao Consumidor Amplo | `CALCCORR.NSN` | Índice de inflação mensal. Tabela hardcoded no programa (2010-2012). Usado para correção retroativa |
| 49  | `COD-RETORNO` | Código de retorno bancário CNAB | `BATCHCON.NSN` | Valores: '00' (pago), '01' (devolvido), '02' (estornado). Define transição de STATUS-PGTO |
| 50  | `ACAO` | Tipo de ação de auditoria | `BATCHCON.NSN`, `RELAUDIT.NSN` | 6 valores: 'IN' (inclusão), 'AL' (alteração), 'CO' (conciliação), 'CN' (consulta), 'DV' (divergência), 'EX' (exclusão — oculta em relatórios) |
| 51  | `DOCUMENTOS-OK` | Flag de documentação validada | `VALDOCS.NSN`, `VALELEG.NSN` | Campo A1: 'S' = documentação completa. Obrigatório para programas tipo 'A' (assistencial) |
| 52  | `PREF-ESP` | Prefixos especiais de CPF | `VALDOCS.NSN` | 8 prefixos (000-002, 010, 011, 099, 100, 999) que bypassam toda validação documental. Backdoor de teste/governo |
| 53  | `REGIAO 99` | Região especial (internacional/diplomático) | `VALELEG.NSN` | COD-REGIAO = 99 bypassa TODAS as verificações de elegibilidade. Origem desconhecida |
| 54  | `MAP` | Mapa de tela 3270 | `CONSBENF.NSN` | Layout de tela para terminal mainframe. Equivalente a template HTML. Usa INPUT USING MAP |
| 55  | `PERFORM` | Chamada de sub-rotina interna | Linguagem Natural | Executa DEFINE SUBROUTINE dentro do mesmo programa. Diferente de CALLNAT (externo) |
| 56  | `END TRANSACTION` | Commit de transação Adabas | Linguagem Natural | Equivalente a COMMIT em SQL. Confirma STORE/UPDATE pendentes. Sem ele, alterações são perdidas |
| 57  | `WORK FILE` | Arquivo externo (flat file) | `BATCHCON.NSN` | Arquivo sequencial lido/escrito pelo programa. CNAB 240 é lido como WORK FILE |
| 58  | `DECIDE ON FIRST VALUE OF` | Switch/case em Natural | Linguagem Natural | Equivalente a switch-case em Java. Avalia primeira correspondência e sai |
| 59  | `*DATN` | Data do sistema (formato numérico) | Linguagem Natural | Variável de sistema Natural. Retorna data atual no formato N8 (AAAAMMDD) |
| 60  | `*TIMN` | Hora do sistema (formato numérico) | `BATCHCON.NSN` | Variável de sistema Natural. Retorna hora atual no formato N6 (HHMMSS) |

> Adicione mais linhas conforme necessário. Não se limite a 30!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- Anote aqui qualquer padrão de nomenclatura que o time identificou:
  - Prefixos de entidade nos DDMs: `BN-` (beneficiário), `PG-` (pagamento), `PS-` (programa social), `AU-` (auditoria)
  - Sufixos de tipo: `NM-` (nome), `NR-` (número), `CD-` (código), `DT-` (data), `VL-` (valor), `QT-` (quantidade), `IN-` (indicador S/N)
  - Variáveis locais: prefixo `#` (ex.: `#VLR-BENF`, `#FATOR-REG`)
- Convenções de prefixo/sufixo encontradas:
  - Programas: `CAD` (cadastro), `CALC` (cálculo), `VAL` (validação), `BATCH` (processamento em lote), `CONS` (consulta), `REL` (relatório)
  - Fatores de cálculo: `FATOR-REG` (regional), `FATOR-FAM` (familiar), `FATOR-RND` (renda), `FATOR-IDADE`, `FATOR-REAJ` (reajuste)
- Termos ambíguos que precisam de validação com especialista:
  - `TIPO-PROG = 'A'` — o que diferencia programa tipo 'A' dos outros? Só 'A' recebe abono.
  - `COD-REGIAO` valores 26 e 27 — recebem fator 1.0, mas IF aceita apenas 1-25. Que UFs são?
  - `STATUS = 'S'` — suspenso por idade >75? Por que 75 e não 65 (Estatuto do Idoso)?
  - `Fator-K` mencionado no RN-SIFAP-2012 — **CONFIRMADO**: é `1.00 + (FATOR-REAJ × 0.347215)` em CADPROG.NSN#L85-L86. Constante 0.347215 sem explicação.

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
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

