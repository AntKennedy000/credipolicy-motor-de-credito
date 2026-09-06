package br.com.credipolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "avaliacoes_credito")
public class AvaliacaoCreditoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(nullable = false, length = 50)
    private String identificador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPolitica politica;

    @Column(name = "versao_politica", nullable = false, length = 20)
    private String versaoPolitica;

    @Column(nullable = false)
    private Integer score;

    @Column(
        name = "renda_mensal",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal rendaMensal;

    @Column(
        name = "compromissos_mensais",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal compromissosMensais;

    @Column(
        name = "valor_solicitado",
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal valorSolicitado;

    @Column(name = "prazo_meses", nullable = false)
    private Integer prazoMeses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisaoCredito decisao;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "taxa_mensal", precision = 10, scale = 6)
    private BigDecimal taxaMensal;

    @Column(name = "parcela_simulada", precision = 19, scale = 2)
    private BigDecimal parcelaSimulada;

    @Column(
        name = "limite_mensal_comprometimento",
        precision = 19,
        scale = 4
    )
    private BigDecimal limiteMensalComprometimento;

    @Column(name = "total_comprometido", precision = 19, scale = 2)
    private BigDecimal totalComprometido;

    protected AvaliacaoCreditoEntity() {
        // Construtor exigido pelo JPA.
    }

    public AvaliacaoCreditoEntity(
            PropostaRequest proposta,
            ResultadoAvaliacao resultado) {

        this.criadoEm = Instant.now();

        this.identificador = proposta.identificador();
        this.score = proposta.score();
        this.rendaMensal = proposta.rendaMensal();
        this.compromissosMensais = proposta.compromissosMensais();
        this.valorSolicitado = proposta.valorSolicitado();
        this.prazoMeses = proposta.prazoMeses();

        this.politica = resultado.politica();
        this.versaoPolitica = resultado.versaoPolitica();
        this.decisao = resultado.decisao();
        this.motivo = resultado.motivo();
        this.taxaMensal = resultado.taxaMensal();
        this.parcelaSimulada = resultado.parcelaSimulada();

        this.limiteMensalComprometimento =
            resultado.limiteMensalComprometimento();

        this.totalComprometido = resultado.totalComprometido();
    }

    public UUID getId() {
        return id;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public PropostaRequest getProposta() {
        return new PropostaRequest(
            identificador,
            score,
            rendaMensal,
            compromissosMensais,
            valorSolicitado,
            prazoMeses,
            politica
        );
    }

    public ResultadoAvaliacao getResultado() {
        return new ResultadoAvaliacao(
            identificador,
            politica,
            versaoPolitica,
            decisao,
            motivo,
            score,
            taxaMensal,
            parcelaSimulada,
            limiteMensalComprometimento,
            totalComprometido
        );
    }
}