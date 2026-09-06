package br.com.credipolicy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class CadeiaAvaliacao {

    private final Regra primeiraRegra;

    public CadeiaAvaliacao(
            CalculadoraCredito calculadora,
            CapacidadePagamento capacidade) {

        Regra conclusao = new RegraConclusao();

        Regra pagamento = new RegraCapacidade(
            calculadora,
            capacidade,
            conclusao
        );

        this.primeiraRegra = new RegraScore(pagamento);
    }

    // Recebe uma proposta previamente validada.
    public ResultadoAvaliacao avaliar(
            PropostaRequest proposta,
            PoliticaCredito politica) {

        if (proposta == null || politica == null) {
            throw new IllegalArgumentException(
                "Informe a proposta e a politica."
            );
        }

        if (proposta.politica() != politica.tipo()) {
            throw new IllegalArgumentException(
                "A politica selecionada difere da politica da proposta."
            );
        }

        Contexto contexto = new Contexto(proposta, politica);

        return primeiraRegra.avaliar(contexto);
    }

    private interface Regra {
        ResultadoAvaliacao avaliar(Contexto contexto);
    }

    private static final class Contexto {

        private final PropostaRequest proposta;
        private final PoliticaCredito politica;

        private ClassificacaoScore classificacaoScore;
        private BigDecimal taxaMensal;
        private BigDecimal parcela;
        private CapacidadePagamento.Resultado capacidade;

        private Contexto(
                PropostaRequest proposta,
                PoliticaCredito politica) {
            this.proposta = proposta;
            this.politica = politica;
        }

        private ResultadoAvaliacao concluir(
                DecisaoCredito decisao,
                String motivo) {

            return new ResultadoAvaliacao(
                proposta.identificador(),
                politica.tipo(),
                politica.versao(),
                decisao,
                motivo,
                proposta.score(),
                taxaMensal,
                parcela,
                capacidade == null ? null : capacidade.limiteMensal(),
                capacidade == null ? null : capacidade.totalComprometido()
            );
        }
    }

    private static final class RegraScore implements Regra {

        private final Regra proxima;

        private RegraScore(Regra proxima) {
            this.proxima = proxima;
        }

        @Override
        public ResultadoAvaliacao avaliar(Contexto contexto) {

            contexto.classificacaoScore =
                contexto.politica.classificarScore(
                    contexto.proposta.score()
                );

            if (contexto.classificacaoScore
                    == ClassificacaoScore.INELEGIVEL) {

                return contexto.concluir(
                    DecisaoCredito.RECUSADA,
                    "Score abaixo da faixa minima de analise da politica."
                );
            }

            return proxima.avaliar(contexto);
        }
    }

    private static final class RegraCapacidade implements Regra {

        private final CalculadoraCredito calculadora;
        private final CapacidadePagamento capacidade;
        private final Regra proxima;

        private RegraCapacidade(
                CalculadoraCredito calculadora,
                CapacidadePagamento capacidade,
                Regra proxima) {

            this.calculadora = calculadora;
            this.capacidade = capacidade;
            this.proxima = proxima;
        }

        @Override
        public ResultadoAvaliacao avaliar(Contexto contexto) {

            PropostaRequest proposta = contexto.proposta;

            contexto.taxaMensal =
                calculadora.taxaMensalParaScore(proposta.score());

            contexto.parcela = calculadora.calcularParcela(
                proposta.valorSolicitado(),
                proposta.prazoMeses(),
                proposta.score()
            );

            contexto.capacidade = capacidade.avaliar(
                proposta.rendaMensal(),
                proposta.compromissosMensais(),
                contexto.parcela,
                contexto.politica
            );

            if (!contexto.capacidade.dentroDoLimite()) {
                return contexto.concluir(
                    DecisaoCredito.RECUSADA,
                    "Compromissos existentes mais a nova parcela "
                        + "ultrapassam o limite mensal da politica."
                );
            }

            return proxima.avaliar(contexto);
        }
    }

    private static final class RegraConclusao implements Regra {

        @Override
        public ResultadoAvaliacao avaliar(Contexto contexto) {

            if (contexto.classificacaoScore
                    == ClassificacaoScore.ANALISE_MANUAL) {

                return contexto.concluir(
                    DecisaoCredito.ANALISE_MANUAL,
                    "Capacidade de pagamento suficiente, mas o score "
                        + "exige analise manual pela politica."
                );
            }

            return contexto.concluir(
                DecisaoCredito.APROVADA,
                "Score e capacidade de pagamento atendem "
                    + "aos criterios da politica simulada."
            );
        }
    }
}