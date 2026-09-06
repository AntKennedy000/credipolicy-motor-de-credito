package br.com.credipolicy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CadeiaAvaliacaoTest {

    private final CadeiaAvaliacao cadeia = new CadeiaAvaliacao(
        new CalculadoraCredito(),
        new CapacidadePagamento()
    );

    @ParameterizedTest
    @CsvSource({
        "CONSERVADORA, 640, 0, RECUSADA",
        "CONSERVADORA, 680, 1000, ANALISE_MANUAL",
        "CONSERVADORA, 750, 1000, APROVADA",
        "CONSERVADORA, 680, 1400, RECUSADA",
        "CONSERVADORA, 750, 1400, RECUSADA",
        "FLEXIVEL, 680, 1200, APROVADA"
    })
    void deveCombinarScoreECapacidadeDePagamento(
            TipoPolitica tipo,
            int score,
            String compromissos,
            DecisaoCredito decisaoEsperada) {

        PoliticaCredito politica = selecionarPolitica(tipo);

        PropostaRequest proposta = criarProposta(
            tipo,
            score,
            compromissos
        );

        ResultadoAvaliacao resultado = cadeia.avaliar(
            proposta,
            politica
        );

        assertEquals(decisaoEsperada, resultado.decisao());
        assertEquals(tipo, resultado.politica());
        assertEquals("1.0", resultado.versaoPolitica());
        assertEquals("TESTE-001", resultado.identificador());
        assertFalse(resultado.motivo().isBlank());
    }

    @Test
    void deveEncerrarAntesDoCalculoQuandoScoreForInelegivel() {
        PropostaRequest proposta = criarProposta(
            TipoPolitica.CONSERVADORA,
            640,
            "0"
        );

        ResultadoAvaliacao resultado = cadeia.avaliar(
            proposta,
            new PoliticaConservadora()
        );

        assertEquals(DecisaoCredito.RECUSADA, resultado.decisao());
        assertNull(resultado.taxaMensal());
        assertNull(resultado.parcelaSimulada());
        assertNull(resultado.totalComprometido());
    }

    @Test
    void deveRejeitarPoliticaDiferenteDaInformadaNaProposta() {
        PropostaRequest proposta = criarProposta(
            TipoPolitica.CONSERVADORA,
            750,
            "1000"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> cadeia.avaliar(
                proposta,
                new PoliticaFlexivel()
            )
        );
    }

    private PoliticaCredito selecionarPolitica(TipoPolitica tipo) {
        return switch (tipo) {
            case CONSERVADORA -> new PoliticaConservadora();
            case FLEXIVEL -> new PoliticaFlexivel();
        };
    }

    private PropostaRequest criarProposta(
            TipoPolitica tipo,
            int score,
            String compromissos) {

        return new PropostaRequest(
            "TESTE-001",
            score,
            new BigDecimal("5000"),
            new BigDecimal(compromissos),
            new BigDecimal("5000"),
            24,
            tipo
        );
    }
}