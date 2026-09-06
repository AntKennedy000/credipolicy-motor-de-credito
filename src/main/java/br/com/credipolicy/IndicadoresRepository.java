package br.com.credipolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IndicadoresRepository {

    private final JdbcTemplate jdbcTemplate;

    public IndicadoresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<IndicadoresPolitica> consultar() {

        String sql = """
            SELECT
                politica,
                versao_politica,
                COUNT(*) AS total_avaliacoes,
                SUM(
                    CASE WHEN decisao = 'APROVADA'
                    THEN 1 ELSE 0 END
                ) AS aprovadas,
                SUM(
                    CASE WHEN decisao = 'RECUSADA'
                    THEN 1 ELSE 0 END
                ) AS recusadas,
                SUM(
                    CASE WHEN decisao = 'ANALISE_MANUAL'
                    THEN 1 ELSE 0 END
                ) AS analise_manual,
                SUM(
                    CASE WHEN decisao = 'APROVADA'
                    THEN valor_solicitado ELSE 0 END
                ) AS valor_solicitado_aprovado
            FROM avaliacoes_credito
            GROUP BY politica, versao_politica
            ORDER BY politica, versao_politica
            """;

        return jdbcTemplate.query(sql, (linha, numero) -> {

            long total = linha.getLong("total_avaliacoes");
            long aprovadas = linha.getLong("aprovadas");

            BigDecimal percentual = BigDecimal.valueOf(aprovadas)
                .multiply(new BigDecimal("100"))
                .divide(
                    BigDecimal.valueOf(total),
                    2,
                    RoundingMode.HALF_UP
                );

            return new IndicadoresPolitica(
                linha.getString("politica"),
                linha.getString("versao_politica"),
                total,
                aprovadas,
                linha.getLong("recusadas"),
                linha.getLong("analise_manual"),
                percentual,
                linha.getBigDecimal("valor_solicitado_aprovado")
            );
        });
    }
}