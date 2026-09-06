package br.com.credipolicy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class PoliticaConservadora implements PoliticaCredito {

    @Override
    public TipoPolitica tipo() {
        return TipoPolitica.CONSERVADORA;
    }

    @Override
    public String versao() {
        return "1.0";
    }

    @Override
    public ClassificacaoScore classificarScore(int score) {
        if (score < 0 || score > 1000) {
            throw new IllegalArgumentException(
                "O score deve estar entre 0 e 1000."
            );
        }

        if (score >= 700) {
            return ClassificacaoScore.ELEGIVEL;
        }

        if (score >= 650) {
            return ClassificacaoScore.ANALISE_MANUAL;
        }

        return ClassificacaoScore.INELEGIVEL;
    }

    @Override
    public BigDecimal percentualMaximoComprometimento() {
        return new BigDecimal("0.30");
    }
}