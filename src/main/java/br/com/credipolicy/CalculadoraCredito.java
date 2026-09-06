package br.com.credipolicy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class CalculadoraCredito {

    private static final MathContext PRECISAO = MathContext.DECIMAL128;

    public BigDecimal taxaMensalParaScore(int score) {
        if (score < 0 || score > 1000) {
            throw new IllegalArgumentException(
                "O score deve estar entre 0 e 1000."
            );
        }

        if (score >= 800) {
            return new BigDecimal("0.02");
        }

        if (score >= 700) {
            return new BigDecimal("0.03");
        }

        return new BigDecimal("0.04");
    }

    public BigDecimal calcularParcela(
            BigDecimal valorSolicitado,
            int prazoMeses,
            int score) {

        if (valorSolicitado == null
                || valorSolicitado.signum() <= 0) {
            throw new IllegalArgumentException(
                "O valor solicitado deve ser maior que zero."
            );
        }

        if (prazoMeses < 1 || prazoMeses > 60) {
            throw new IllegalArgumentException(
                "O prazo deve estar entre 1 e 60 meses."
            );
        }

        BigDecimal taxa = taxaMensalParaScore(score);

        // Fator = (1 + taxa) elevado ao prazo.
        BigDecimal fator = BigDecimal.ONE
            .add(taxa, PRECISAO)
            .pow(prazoMeses, PRECISAO);

        // Forma equivalente da formula de parcelas fixas:
        // parcela = valor * taxa * fator / (fator - 1).
        BigDecimal numerador = valorSolicitado
            .multiply(taxa, PRECISAO)
            .multiply(fator, PRECISAO);

        BigDecimal denominador = fator.subtract(
            BigDecimal.ONE,
            PRECISAO
        );

        return numerador
            .divide(denominador, PRECISAO)
            .setScale(2, RoundingMode.HALF_UP);
    }
}