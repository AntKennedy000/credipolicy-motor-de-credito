# Experimento — Comparação de políticas de crédito

## Objetivo

Avaliar como as políticas CONSERVADORA e FLEXIVEL alteram as decisões sobre uma mesma população fictícia.

O experimento utiliza oito propostas sintéticas, avaliadas nas duas políticas, totalizando 16 avaliações.

Os dados foram escolhidos para exercitar as regras do sistema. Não representam uma amostra estatística de clientes reais.

## Método

- Entrada: `data/propostas_sinteticas.csv`.
- Execução: `analises/executar_experimento.py`.
- Motor de decisão: API Java com Spring Boot.
- Políticas utilizadas: CONSERVADORA e FLEXIVEL, versão 1.0.
- Resultado detalhado: arquivo `avaliacoes.csv` gerado na pasta de resultados do experimento.

Para cada proposta, score, renda, compromissos, valor solicitado e prazo foram mantidos iguais. Alterou-se apenas a política selecionada.

A tabela de taxas também foi mantida igual entre as políticas.

## Resultados observados

| Indicador | Conservadora | Flexível |
|---|---:|---:|
| Propostas avaliadas | 8 | 8 |
| Aprovadas | 2 | 5 |
| Recusadas | 4 | 2 |
| Encaminhadas para análise manual | 2 | 1 |
| Percentual de aprovação | 25% | 62,5% |
| Valor solicitado nas propostas aprovadas | R$ 10.000,00 | R$ 25.000,00 |

A diferença de aprovação foi de 37,5 pontos percentuais.

A política flexível apresentou três aprovações adicionais e R$ 15.000,00 adicionais em valor solicitado nas propostas aprovadas.

Esses valores representam resultados da simulação, não crédito contratado ou desembolsado.

## Explicação das mudanças

| Proposta | Conservadora | Flexível | Motivo da mudança |
|---|---|---|---|
| P002 | Análise manual | Aprovada | Score 680 atende à faixa de aprovação da flexível. |
| P003 | Recusada | Análise manual | Score 580 entra na faixa de análise da flexível. |
| P004 | Recusada | Aprovada | O comprometimento cabe no limite de 35%, mas ultrapassa o de 30%. |
| P008 | Análise manual | Aprovada | Score 650 atende à faixa de aprovação da flexível. |

As propostas P001 e P007 foram aprovadas nas duas políticas.

A proposta P005 foi recusada nas duas políticas por score insuficiente.

A proposta P006 foi recusada nas duas políticas por comprometimento acima do limite, mesmo apresentando score 850.

## Interpretação de negócio

A mudança de política ampliou a aprovação nesta base e alterou o encaminhamento para análise manual.

O caso P006 demonstra que score elevado não elimina a necessidade de avaliar a capacidade de pagamento.

O experimento permite identificar quais propostas mudaram de decisão e quais regras explicam essas mudanças. Isso ajuda a discutir alterações de política de maneira rastreável.

## Limitações

Maior aprovação não comprova menor risco ou maior rentabilidade.

Não há dados de contratação, pagamento, atraso, recuperação, custos ou perdas. Portanto, o experimento não mede inadimplência, perda esperada ou retorno financeiro.

O score foi fornecido como entrada. Não foi desenvolvido ou validado um modelo preditivo de score.

As taxas são ilustrativas e não constituem uma precificação calibrada por risco.

As conclusões se limitam aos oito casos sintéticos. Uma recomendação de mudança de política exigiria uma avaliação mais ampla e dados de desempenho.

## Rastreabilidade

Cada execução do script recebe um identificador próprio e salva seus resultados em uma pasta separada.

A API registra as avaliações no banco, incluindo dados de entrada, decisão, motivo, data e versão da política.

O endpoint `/api/indicadores` consolida todas as avaliações registradas. Seu resultado pode incluir testes manuais e outras execuções.

Os números deste documento consideram exclusivamente o experimento com as oito propostas, sem misturar avaliações anteriores.

## Próximas evoluções

- Ampliar os cenários de teste e a diversidade da base.
- Incluir filtros de experimento nos indicadores SQL.
- Implementar o cálculo do valor máximo financiável.
- Avaliar políticas com dados de desempenho, quando disponíveis.
- Evoluir a análise de carteira em Python e, em uma etapa posterior, Spark ou Databricks.