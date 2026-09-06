package br.com.credipolicy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class CapacidadePagamento {

    public Resultado avaliar(
            BigDecimal rendaMensal,
            BigDecimal compromissosMensais,
            BigDecimal parcelaSimulada,
            PoliticaCredito politica) {

        if (rendaMensal == null || rendaMensal.signum() <= 0) {
            throw new IllegalArgumentException(
                "A renda mensal deve ser maior que zero."
            );
        }

        if (compromissosMensais == null
                || compromissosMensais.signum() < 0) {
            throw new IllegalArgumentException(
                "Os compromissos mensais nao podem ser negativos."
            );
        }

        if (parcelaSimulada == null
                || parcelaSimulada.signum() < 0) {
            throw new IllegalArgumentException(
                "A parcela simulada nao pode ser negativa."
            );
        }

        if (politica == null) {
            throw new IllegalArgumentException(
                "Informe a politica de credito."
            );
        }

        BigDecimal limiteMensal = rendaMensal.multiply(
            politica.percentualMaximoComprometimento()
        );

        BigDecimal totalComprometido = compromissosMensais.add(
            parcelaSimulada
        );

        // A igualdade e permitida pela politica.
        boolean dentroDoLimite =
            totalComprometido.compareTo(limiteMensal) <= 0;

        return new Resultado(
            dentroDoLimite,
            limiteMensal,
            totalComprometido
        );
    }

    public record Resultado(
        boolean dentroDoLimite,
        BigDecimal limiteMensal,
        BigDecimal totalComprometido
    ) {
    }
}