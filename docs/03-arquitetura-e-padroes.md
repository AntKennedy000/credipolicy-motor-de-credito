# Arquitetura e padrões de projeto

## Visão geral

O CrediPolicy é uma API educacional que recebe propostas fictícias, aplica uma política de crédito e registra decisões explicáveis.

A implementação combina Java, Spring Boot, validação de dados, persistência em H2, consultas SQL e um experimento comparativo em Python.

## Fluxo da avaliação

1. `AvaliacaoController` recebe a proposta.
2. `AvaliacaoCreditoFacade` valida os dados e seleciona a política.
3. `CadeiaAvaliacao` verifica score, capacidade de pagamento e decisão final.
4. `AvaliacaoCreditoRepository` grava os dados de entrada e o resultado.
5. A API devolve a decisão com seu motivo e a versão da política.

Entradas inválidas retornam HTTP 400 e não são gravadas como avaliações de crédito.

## Strategy — Políticas intercambiáveis

Arquivos principais:

- `PoliticaCredito.java`
- `PoliticaConservadora.java`
- `PoliticaFlexivel.java`

A interface `PoliticaCredito` define os comportamentos comuns às políticas.

Cada implementação estabelece suas faixas de score e seu percentual máximo de comprometimento da renda.

A Facade seleciona a implementação correspondente ao campo `politica` da proposta. A cadeia utiliza a interface, sem precisar conhecer os detalhes de cada implementação.

Exemplo observado: uma proposta com score 680 e capacidade suficiente recebe ANALISE_MANUAL na política conservadora e APROVADA na flexível.

## Chain of Responsibility — Avaliação sequencial

Arquivo principal:

- `CadeiaAvaliacao.java`

A cadeia possui três etapas:

| Etapa | Responsabilidade |
|---|---|
| `RegraScore` | Recusar scores abaixo da faixa mínima de análise ou encaminhar à próxima etapa. |
| `RegraCapacidade` | Calcular a parcela, verificar o comprometimento e recusar quando o limite for ultrapassado. |
| `RegraConclusao` | Concluir com aprovação ou análise manual. |

Uma regra pode encerrar a avaliação sem executar as seguintes.

Quando a recusa ocorre pelo score, os campos financeiros não calculados permanecem nulos.

Cada avaliação recebe um contexto próprio. Os dados de uma proposta não são armazenados como estado compartilhado entre requisições.

## Facade — Entrada única para o processo

Arquivo principal:

- `AvaliacaoCreditoFacade.java`

A Facade reúne validação, seleção da política, avaliação e persistência em uma operação.

O controller não precisa coordenar essas etapas nem conhecer as regras internas de cada política.

A operação utiliza uma transação para a gravação. Uma falha de persistência impede que a chamada seja concluída como uma avaliação gravada com sucesso.

## Spring e acesso aos dados

O Spring gerencia a criação e a injeção dos componentes por seus construtores.

As principais anotações utilizadas são:

- `@RestController`: endpoints HTTP.
- `@Service`: coordenação do processo de avaliação.
- `@Component`: políticas, cálculos e cadeia.
- `@Repository`: consulta de indicadores por SQL.
- `@Transactional`: controle das transações.

O Spring Data JPA fornece operações de persistência por meio de `AvaliacaoCreditoRepository`.

Repository é uma abstração de acesso a dados utilizada no projeto; não faz parte dos padrões clássicos GoF demonstrados aqui.

## Banco e histórico

A tabela `avaliacoes_credito` armazena:

- Identificador da avaliação e data de criação;
- Identificador fictício e dados da proposta;
- Política e versão;
- Decisão e motivo;
- Taxa, parcela e valores de comprometimento, quando calculados.

Cada envio válido gera uma nova avaliação. O identificador fictício da proposta pode se repetir para permitir comparações, mas cada avaliação possui um UUID próprio.

O histórico da API retorna as 50 avaliações mais recentes. Os indicadores SQL consideram todos os registros.

A aplicação utiliza H2 em arquivo. Os testes utilizam bancos em memória separados dos dados locais.

## Endpoints

| Método | Caminho | Finalidade |
|---|---|---|
| GET | `/api/status` | Consultar o funcionamento da aplicação. |
| POST | `/api/avaliacoes` | Avaliar e registrar uma proposta. |
| GET | `/api/avaliacoes` | Consultar as 50 avaliações mais recentes. |
| GET | `/api/indicadores` | Consultar indicadores agrupados por política e versão. |

## Verificação

Os testes automatizados cobrem políticas, faixas de score, parcelas, capacidade de pagamento, cadeia de decisão, API, validações, persistência e indicadores SQL.

Também foi verificada manualmente a permanência de uma avaliação no histórico após reiniciar a aplicação, mantendo o mesmo UUID.

O experimento Python avaliou oito propostas nas duas políticas. Seus resultados e limitações estão documentados em `02-resultados-experimento.md`.