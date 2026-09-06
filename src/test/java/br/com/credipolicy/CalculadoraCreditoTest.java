package br.com.credipolicy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraCreditoTest {

    private final CalculadoraCredito calculadora =
        new CalculadoraCredito();

    @ParameterizedTest
    @CsvSource({
        "0, 0.04",
        "699, 0.04",
        "700, 0.03",
        "799, 0.03",
        "800, 0.02",
        "1000, 0.02"
    })
    void deveSelecionarTaxaPorFaixaDeScore(
            int score,
            String taxaEsperada) {

        assertEquals(
            0,
            new BigDecimal(taxaEsperada).compareTo(
                calculadora.taxaMensalParaScore(score)
            )
        );
    }

    @Test
    void deveCalcularParcelaUnicaComJuros() {
        // R$ 1.000 por um mes, com juros de 2%: R$ 1.020.
        BigDecimal parcela = calculadora.calcularParcela(
            new BigDecimal("1000.00"),
            1,
            800
        );

        assertEquals(new BigDecimal("1020.00"), parcela);
    }

    @Test
    void deveCalcularParcelasFixasEmVinteEQuatroMeses() {
        // R$ 5.000 em 24 meses, com juros de 3% ao mes.
        BigDecimal parcela = calculadora.calcularParcela(
            new BigDecimal("5000.00"),
            24,
            700
        );

        assertEquals(new BigDecimal("295.24"), parcela);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-100"})
    void deveRejeitarValorNaoPositivo(String valor) {
        assertThrows(
            IllegalArgumentException.class,
            () -> calculadora.calcularParcela(
                new BigDecimal(valor),
                24,
                700
            )
        );
    }

    @Test
    void deveRejeitarValorAusente() {
        assertThrows(
            IllegalArgumentException.class,
            () -> calculadora.calcularParcela(null, 24, 700)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 61})
    void deveRejeitarPrazoInvalido(int prazo) {
        assertThrows(
            IllegalArgumentException.class,
            () -> calculadora.calcularParcela(
                new BigDecimal("5000"),
                prazo,
                700
            )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1001})
    void deveRejeitarScoreInvalido(int score) {
        assertThrows(
            IllegalArgumentException.class,
            () -> calculadora.calcularParcela(
                new BigDecimal("5000"),
                24,
                score
            )
        );
    }
}