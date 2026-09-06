# CrediPolicy — Políticas de crédito

## Objetivo

Simular como diferentes políticas alteram decisões de crédito sobre propostas fictícias, mantendo os motivos e a versão das regras utilizadas.

Todas as faixas e parâmetros deste projeto são escolhas educacionais. Não representam políticas de bancos nem foram calibrados com dados reais.

## Dados da proposta

- Identificador fictício da proposta;
- Score inteiro entre 0 e 1.000;
- Renda mensal maior que zero;
- Compromissos mensais existentes, maiores ou iguais a zero;
- Valor solicitado maior que zero;
- Prazo inteiro entre 1 e 60 meses;
- Política escolhida: CONSERVADORA ou FLEXIVEL.

O score é fornecido como entrada. Esta versão não treina um modelo de score e não transforma a pontuação em probabilidade de inadimplência.

Não serão utilizados nomes, CPF, contas ou outros identificadores pessoais.

## Políticas — versão 1.0

| Critério | CONSERVADORA | FLEXIVEL |
|---|---|---|
| Score elegível para aprovação | 700 a 1.000 | 600 a 1.000 |
| Score encaminhado para análise | 650 a 699 | 550 a 599 |
| Score recusado | Abaixo de 650 | Abaixo de 550 |
| Comprometimento máximo da renda | 30% | 35% |

O comprometimento considera os compromissos mensais existentes mais a parcela simulada da nova operação.

## Taxas ilustrativas

Para isolar o efeito das políticas, ambas utilizarão a mesma tabela de taxas:

| Score | Taxa mensal ilustrativa |
|---|---|
| 800 a 1.000 | 2% |
| 700 a 799 | 3% |
| 0 a 699 | 4% |

Essas taxas não são ofertas comerciais nem estimativas de mercado. Não incluem tarifas, impostos ou seguros e não representam o CET.

## Cálculo da parcela

A simulação utilizará parcelas fixas pela fórmula:

`parcela = valor × taxa / (1 − (1 + taxa)^(-prazo))`

A taxa será utilizada em formato decimal: 2% corresponde a 0,02.

A parcela será arredondada para centavos. Para validar a capacidade de pagamento, o código comparará:

`compromissos existentes + parcela <= renda × percentual máximo`

O limite de comprometimento incluirá a igualdade.

## Ordem da avaliação

1. Validar os dados de entrada. Dados inválidos geram erro de validação, sem decisão de crédito.
2. Verificar a faixa de score. Score abaixo da faixa de análise gera RECUSADA.
3. Calcular a parcela e verificar o comprometimento. Acima do máximo gera RECUSADA.
4. Se a capacidade for suficiente, mas o score estiver na faixa de análise, retornar ANALISE_MANUAL.
5. Se todos os critérios de aprovação forem atendidos, retornar APROVADA.

Todas as decisões são simuladas e devem apresentar um motivo explícito.

## Aplicação dos padrões

- Strategy: permite selecionar a política conservadora ou flexível.
- Chain of Responsibility: organiza as verificações em sequência.
- Facade: oferece uma entrada única para avaliar a proposta e reunir os resultados.

## Evolução

Após validar esse fluxo, o projeto incluirá persistência em SQL, cálculo de limite e comparação das políticas sobre a mesma carteira fictícia.

A análise da carteira mostrará aprovação, volume e distribuição por score. Medir inadimplência ou rentabilidade exigirá dados adicionais de desempenho.