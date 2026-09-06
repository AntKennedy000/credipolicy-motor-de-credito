package br.com.credipolicy;

import java.math.BigDecimal;

public record IndicadoresPolitica(
    String politica,
    String versaoPolitica,
    long totalAvaliacoes,
    long aprovadas,
    long recusadas,
    long analiseManual,
    BigDecimal percentualAprovacao,
    BigDecimal valorSolicitadoAprovado
) {
}