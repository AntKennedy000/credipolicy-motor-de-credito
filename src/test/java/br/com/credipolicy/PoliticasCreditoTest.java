package br.com.credipolicy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoliticasCreditoTest {

    private final PoliticaCredito conservadora =
        new PoliticaConservadora();

    private final PoliticaCredito flexivel =
        new PoliticaFlexivel();

    @ParameterizedTest
    @CsvSource({
        "0, INELEGIVEL",
        "649, INELEGIVEL",
        "650, ANALISE_MANUAL",
        "699, ANALISE_MANUAL",
        "700, ELEGIVEL",
        "1000, ELEGIVEL"
    })
    void deveClassificarScoreNaPoliticaConservadora(
            int score,
            ClassificacaoScore esperado) {

        assertEquals(esperado, conservadora.classificarScore(score));
    }

    @ParameterizedTest
    @CsvSource({
        "0, INELEGIVEL",
        "549, INELEGIVEL",
        "550, ANALISE_MANUAL",
        "599, ANALISE_MANUAL",
        "600, ELEGIVEL",
        "1000, ELEGIVEL"
    })
    void deveClassificarScoreNaPoliticaFlexivel(
            int score,
            ClassificacaoScore esperado) {

        assertEquals(esperado, flexivel.classificarScore(score));
    }

    @ParameterizedTest
    @CsvSource({"-1", "1001"})
    void deveRejeitarScoreForaDoIntervalo(int score) {
        assertThrows(
            IllegalArgumentException.class,
            () -> conservadora.classificarScore(score)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> flexivel.classificarScore(score)
        );
    }

    @Test
    void deveAplicarLimitesDeComprometimentoDocumentados() {
        assertEquals(
            0,
            new BigDecimal("0.30").compareTo(
                conservadora.percentualMaximoComprometimento()
            )
        );

        assertEquals(
            0,
            new BigDecimal("0.35").compareTo(
                flexivel.percentualMaximoComprometimento()
            )
        );
    }

    @Test
    void deveClassificarMesmoScoreConformePoliticaEscolhida() {
        assertEquals(
            ClassificacaoScore.ANALISE_MANUAL,
            conservadora.classificarScore(680)
        );

        assertEquals(
            ClassificacaoScore.ELEGIVEL,
            flexivel.classificarScore(680)
        );
    }
}