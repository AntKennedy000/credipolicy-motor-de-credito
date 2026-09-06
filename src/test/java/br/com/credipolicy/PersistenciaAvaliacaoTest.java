package br.com.credipolicy;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PersistenciaAvaliacaoTest {

    @Autowired
    private AvaliacaoCreditoRepository repository;

    @Autowired
    private AvaliacaoCreditoFacade facade;

    @Autowired
    private CadeiaAvaliacao cadeia;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveGravarERecuperarPropostaEDecisao() {

        PropostaRequest proposta = criarProposta(
            850,
            new BigDecimal("5000.00")
        );

        ResultadoAvaliacao resultado = cadeia.avaliar(
            proposta,
            new PoliticaFlexivel()
        );

        AvaliacaoCreditoEntity registro = repository.saveAndFlush(
            new AvaliacaoCreditoEntity(proposta, resultado)
        );

        UUID id = registro.getId();
        assertNotNull(id);

        entityManager.clear();

        AvaliacaoCreditoEntity recuperada = repository
            .findById(id)
            .orElseThrow();

        assertNotNull(recuperada.getCriadoEm());

        PropostaRequest entradaSalva = recuperada.getProposta();
        ResultadoAvaliacao decisaoSalva = recuperada.getResultado();

        assertEquals("BANCO-TESTE", entradaSalva.identificador());
        assertEquals(Integer.valueOf(850), entradaSalva.score());
        assertEquals(Integer.valueOf(24), entradaSalva.prazoMeses());

        assertEquals(
            0,
            proposta.rendaMensal().compareTo(entradaSalva.rendaMensal())
        );

        assertEquals(
            0,
            proposta.valorSolicitado().compareTo(
                entradaSalva.valorSolicitado()
            )
        );

        assertEquals(DecisaoCredito.APROVADA, decisaoSalva.decisao());
        assertEquals(TipoPolitica.FLEXIVEL, decisaoSalva.politica());
        assertEquals("1.0", decisaoSalva.versaoPolitica());
        assertEquals(resultado.motivo(), decisaoSalva.motivo());

        assertEquals(
            0,
            new BigDecimal("264.36").compareTo(
                decisaoSalva.parcelaSimulada()
            )
        );

        assertEquals(
            0,
            resultado.limiteMensalComprometimento().compareTo(
                decisaoSalva.limiteMensalComprometimento()
            )
        );
    }

    @Test
    void devePreservarCamposNulosNaRecusaPorScore() {

        PropostaRequest proposta = criarProposta(
            500,
            new BigDecimal("5000.00")
        );

        ResultadoAvaliacao resultado = cadeia.avaliar(
            proposta,
            new PoliticaFlexivel()
        );

        AvaliacaoCreditoEntity registro = repository.saveAndFlush(
            new AvaliacaoCreditoEntity(proposta, resultado)
        );

        UUID id = registro.getId();
        entityManager.clear();

        ResultadoAvaliacao recuperado = repository
            .findById(id)
            .orElseThrow()
            .getResultado();

        assertEquals(DecisaoCredito.RECUSADA, recuperado.decisao());
        assertNull(recuperado.taxaMensal());
        assertNull(recuperado.parcelaSimulada());
        assertNull(recuperado.limiteMensalComprometimento());
        assertNull(recuperado.totalComprometido());
    }

    @Test
    void facadeDeveGravarUmaAvaliacaoValida() {

        long quantidadeAntes = repository.count();

        ResultadoAvaliacao resultado = facade.avaliar(
            criarProposta(850, new BigDecimal("5000.00"))
        );

        entityManager.flush();

        assertEquals(DecisaoCredito.APROVADA, resultado.decisao());
        assertEquals(quantidadeAntes + 1, repository.count());
    }

    @Test
    void facadeNaoDeveGravarPropostaComRendaZero() {

        long quantidadeAntes = repository.count();

        assertThrows(
            ConstraintViolationException.class,
            () -> facade.avaliar(
                criarProposta(850, BigDecimal.ZERO)
            )
        );

        assertEquals(quantidadeAntes, repository.count());
    }

    @Test
    void facadeNaoDeveGravarValorComFracaoDeCentavo() {

        long quantidadeAntes = repository.count();

        assertThrows(
            ConstraintViolationException.class,
            () -> facade.avaliar(
                criarProposta(850, new BigDecimal("5000.001"))
            )
        );

        assertEquals(quantidadeAntes, repository.count());
    }

    private PropostaRequest criarProposta(
            int score,
            BigDecimal renda) {

        return new PropostaRequest(
            "BANCO-TESTE",
            score,
            renda,
            new BigDecimal("1000.00"),
            new BigDecimal("5000.00"),
            24,
            TipoPolitica.FLEXIVEL
        );
    }
}