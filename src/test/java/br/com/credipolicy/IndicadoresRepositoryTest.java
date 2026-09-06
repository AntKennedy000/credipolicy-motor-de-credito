package br.com.credipolicy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class IndicadoresRepositoryTest {

    @Autowired
    private AvaliacaoCreditoFacade facade;

    @Autowired
    private AvaliacaoCreditoRepository avaliacoes;

    @Autowired
    private IndicadoresRepository indicadores;

    @Test
    void deveConsolidarDecisoesEValoresPorPolitica() {

        // Limpa somente o banco em memoria deste teste.
        avaliacoes.deleteAllInBatch();

        avaliar("SQL-001", 850, TipoPolitica.CONSERVADORA);
        avaliar("SQL-002", 680, TipoPolitica.CONSERVADORA);
        avaliar("SQL-003", 500, TipoPolitica.CONSERVADORA);
        avaliar("SQL-004", 850, TipoPolitica.FLEXIVEL);

        // Garante que o SQL consulte as gravacoes realizadas pelo JPA.
        avaliacoes.flush();

        var resultado = indicadores.consultar();

        assertEquals(2, resultado.size());

        var conservadora = resultado.stream()
            .filter(item -> item.politica().equals("CONSERVADORA"))
            .findFirst()
            .orElseThrow();

        assertEquals("1.0", conservadora.versaoPolitica());
        assertEquals(3L, conservadora.totalAvaliacoes());
        assertEquals(1L, conservadora.aprovadas());
        assertEquals(1L, conservadora.recusadas());
        assertEquals(1L, conservadora.analiseManual());

        assertEquals(
            0,
            new BigDecimal("33.33").compareTo(
                conservadora.percentualAprovacao()
            )
        );

        assertEquals(
            0,
            new BigDecimal("5000.00").compareTo(
                conservadora.valorSolicitadoAprovado()
            )
        );

        var flexivel = resultado.stream()
            .filter(item -> item.politica().equals("FLEXIVEL"))
            .findFirst()
            .orElseThrow();

        assertEquals(1L, flexivel.totalAvaliacoes());
        assertEquals(1L, flexivel.aprovadas());

        assertEquals(
            0,
            new BigDecimal("100.00").compareTo(
                flexivel.percentualAprovacao()
            )
        );
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremAvaliacoes() {
        avaliacoes.deleteAllInBatch();

        assertTrue(indicadores.consultar().isEmpty());
    }

    private void avaliar(
            String identificador,
            int score,
            TipoPolitica politica) {

        facade.avaliar(
            new PropostaRequest(
                identificador,
                score,
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                24,
                politica
            )
        );
    }
}