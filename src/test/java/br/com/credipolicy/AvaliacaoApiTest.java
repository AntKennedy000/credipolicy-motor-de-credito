package br.com.credipolicy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "server.address=127.0.0.1",
        "spring.datasource.url=jdbc:h2:mem:credipolicy-api-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class AvaliacaoApiTest {

    @LocalServerPort
    private int porta;

    @Autowired
    private JsonMapper jsonMapper;

    @ParameterizedTest
    @CsvSource({
        "CONSERVADORA, 680, 1000, ANALISE_MANUAL",
        "FLEXIVEL, 680, 1000, APROVADA",
        "FLEXIVEL, 850, 1600, RECUSADA",
        "CONSERVADORA, 640, 0, RECUSADA"
    })
    void deveRetornarDecisaoPelaApi(
            String politica,
            int score,
            int compromissos,
            DecisaoCredito decisaoEsperada) throws Exception {

        String corpo = criarProposta(
            politica,
            score,
            5000,
            compromissos
        );

        HttpResponse<String> resposta = enviar(corpo);

        assertEquals(200, resposta.statusCode(), resposta.body());

        ResultadoAvaliacao resultado = jsonMapper.readValue(
            resposta.body(),
            ResultadoAvaliacao.class
        );

        assertEquals(decisaoEsperada, resultado.decisao());
        assertEquals(TipoPolitica.valueOf(politica), resultado.politica());
        assertEquals("1.0", resultado.versaoPolitica());
        assertEquals("API-TESTE", resultado.identificador());
        assertEquals(score, resultado.score());
        assertNotNull(resultado.motivo());
        assertFalse(resultado.motivo().isBlank());

        if (score == 640) {
            assertNull(resultado.parcelaSimulada());
            assertNull(resultado.taxaMensal());
        } else {
            assertNotNull(resultado.parcelaSimulada());
            assertNotNull(resultado.totalComprometido());
        }
    }

    @Test
    void deveRetornarErroDeValidacaoParaRendaZero() throws Exception {

        HttpResponse<String> resposta = enviar(
            criarProposta("FLEXIVEL", 850, 0, 0)
        );

        assertEquals(400, resposta.statusCode(), resposta.body());

        ErroApi erro = jsonMapper.readValue(
            resposta.body(),
            ErroApi.class
        );

        assertEquals("DADOS_INVALIDOS", erro.codigo());

        assertTrue(
            erro.detalhes().contains(
                "rendaMensal: A renda mensal deve ser maior que zero."
            )
        );

        assertFalse(
            jsonMapper.readTree(resposta.body()).has("decisao")
        );
    }

    @Test
    void deveRejeitarPoliticaInexistente() throws Exception {

        HttpResponse<String> resposta = enviar(
            criarProposta("INEXISTENTE", 850, 5000, 0)
        );

        assertEquals(400, resposta.statusCode(), resposta.body());

        ErroApi erro = jsonMapper.readValue(
            resposta.body(),
            ErroApi.class
        );

        assertEquals("REQUISICAO_INVALIDA", erro.codigo());

        assertFalse(
            jsonMapper.readTree(resposta.body()).has("decisao")
        );
    }

    @Test
    void deveRejeitarJsonMalformado() throws Exception {

        HttpResponse<String> resposta = enviar("{");

        assertEquals(400, resposta.statusCode(), resposta.body());

        ErroApi erro = jsonMapper.readValue(
            resposta.body(),
            ErroApi.class
        );

        assertEquals("REQUISICAO_INVALIDA", erro.codigo());
    }

    private HttpResponse<String> enviar(String corpo) throws Exception {

        HttpRequest requisicao = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "http://127.0.0.1:" + porta + "/api/avaliacoes"
                )
            )
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(corpo))
            .build();

        try (HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            return cliente.send(
                requisicao,
                HttpResponse.BodyHandlers.ofString()
            );
        }
    }

    private String criarProposta(
            String politica,
            int score,
            int renda,
            int compromissos) {

        return """
            {
                "identificador": "API-TESTE",
                "score": %d,
                "rendaMensal": %d,
                "compromissosMensais": %d,
                "valorSolicitado": 5000,
                "prazoMeses": 24,
                "politica": "%s"
            }
            """.formatted(score, renda, compromissos, politica);
    }
}