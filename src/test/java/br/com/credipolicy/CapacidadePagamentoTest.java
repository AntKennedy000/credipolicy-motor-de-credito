package br.com.credipolicy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapacidadePagamentoTest {

    private final CapacidadePagamento capacidade =
        new CapacidadePagamento();

    private final PoliticaCredito conservadora =
        new PoliticaConservadora();

    private final PoliticaCredito flexivel =
        new PoliticaFlexivel();

    @ParameterizedTest
    @CsvSource({
        "299.99, true",
        "300.00, true",
        "300.01, false"
    })
    void deveRespeitarFronteiraDaPoliticaConservadora(
            String parcela,
            boolean esperado) {

        var resultado = capacidade.avaliar(
            new BigDecimal("5000"),
            new BigDecimal("1200"),
            new BigDecimal(parcela),
            conservadora
        );

        assertEquals(esperado, resultado.dentroDoLimite());
    }

    @ParameterizedTest
    @CsvSource({
        "549.99, true",
        "550.00, true",
        "550.01, false"
    })
    void deveRespeitarFronteiraDaPoliticaFlexivel(
            String parcela,
            boolean esperado) {

        var resultado = capacidade.avaliar(
            new BigDecimal("5000"),
            new BigDecimal("1200"),
            new BigDecimal(parcela),
            flexivel
        );

        assertEquals(esperado, resultado.dentroDoLimite());
    }

    @Test
    void deveAvaliarMesmaPropostaConformePolitica() {
        BigDecimal renda = new BigDecimal("5000");
        BigDecimal compromissos = new BigDecimal("1200");
        BigDecimal parcela = new BigDecimal("400");

        var resultadoConservador = capacidade.avaliar(
            renda, compromissos, parcela, conservadora
        );

        var resultadoFlexivel = capacidade.avaliar(
            renda, compromissos, parcela, flexivel
        );

        assertFalse(resultadoConservador.dentroDoLimite());
        assertTrue(resultadoFlexivel.dentroDoLimite());

        assertEquals(
            0,
            new BigDecimal("1600").compareTo(
                resultadoFlexivel.totalComprometido()
            )
        );
    }

    @Test
    void naoDeveArredondarLimiteParaAprovarExcesso() {
        // 30% de 1000.02 = 300.006.
        // Uma parcela de 300.01 ultrapassa esse limite.
        var resultado = capacidade.avaliar(
            new BigDecimal("1000.02"),
            BigDecimal.ZERO,
            new BigDecimal("300.01"),
            conservadora
        );

        assertFalse(resultado.dentroDoLimite());
    }

    @Test
    void deveRejeitarRendaZero() {
        assertThrows(
            IllegalArgumentException.class,
            () -> capacidade.avaliar(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100"),
                conservadora
            )
        );
    }

    @Test
    void deveRejeitarCompromissosNegativos() {
        assertThrows(
            IllegalArgumentException.class,
            () -> capacidade.avaliar(
                new BigDecimal("5000"),
                new BigDecimal("-1"),
                new BigDecimal("100"),
                conservadora
            )
        );
    }
}