package br.com.credipolicy;

import java.math.BigDecimal;

public record ResultadoAvaliacao(
    String identificador,
    TipoPolitica politica,
    String versaoPolitica,
    DecisaoCredito decisao,
    String motivo,
    int score,
    BigDecimal taxaMensal,
    BigDecimal parcelaSimulada,
    BigDecimal limiteMensalComprometimento,
    BigDecimal totalComprometido
) {
}