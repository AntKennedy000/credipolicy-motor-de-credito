package br.com.credipolicy;

import java.time.Instant;
import java.util.UUID;

public record HistoricoAvaliacao(
    UUID id,
    Instant criadoEm,
    PropostaRequest proposta,
    ResultadoAvaliacao resultado
) {

    public static HistoricoAvaliacao de(
            AvaliacaoCreditoEntity registro) {

        return new HistoricoAvaliacao(
            registro.getId(),
            registro.getCriadoEm(),
            registro.getProposta(),
            registro.getResultado()
        );
    }
}