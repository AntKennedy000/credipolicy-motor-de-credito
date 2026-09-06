package br.com.credipolicy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoCreditoFacade {

    private final Map<TipoPolitica, PoliticaCredito> politicas;
    private final CadeiaAvaliacao cadeia;
    private final Validator validator;
    private final AvaliacaoCreditoRepository repository;

    public AvaliacaoCreditoFacade(
            List<PoliticaCredito> implementacoes,
            CadeiaAvaliacao cadeia,
            Validator validator,
            AvaliacaoCreditoRepository repository) {

        this.cadeia = cadeia;
        this.validator = validator;
        this.repository = repository;

        EnumMap<TipoPolitica, PoliticaCredito> registro =
            new EnumMap<>(TipoPolitica.class);

        for (PoliticaCredito politica : implementacoes) {
            PoliticaCredito anterior = registro.put(
                politica.tipo(),
                politica
            );

            if (anterior != null) {
                throw new IllegalStateException(
                    "Existe mais de uma implementacao para a politica "
                        + politica.tipo()
                );
            }
        }

        for (TipoPolitica tipo : TipoPolitica.values()) {
            if (!registro.containsKey(tipo)) {
                throw new IllegalStateException(
                    "Nao foi encontrada implementacao para " + tipo
                );
            }
        }

        this.politicas = Map.copyOf(registro);
    }

    @Transactional
    public ResultadoAvaliacao avaliar(PropostaRequest proposta) {

        if (proposta == null) {
            throw new IllegalArgumentException(
                "Informe os dados da proposta."
            );
        }

        var violacoes = validator.validate(proposta);

        if (!violacoes.isEmpty()) {
            throw new ConstraintViolationException(violacoes);
        }

        PoliticaCredito politica = politicas.get(
            proposta.politica()
        );

        ResultadoAvaliacao resultado = cadeia.avaliar(
            proposta,
            politica
        );

        repository.save(
            new AvaliacaoCreditoEntity(proposta, resultado)
        );

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<HistoricoAvaliacao> listarRecentes() {

        PageRequest pagina = PageRequest.of(
            0,
            50,
            Sort.by(
                Sort.Order.desc("criadoEm"),
                Sort.Order.desc("id")
            )
        );

        return repository.findAll(pagina)
            .getContent()
            .stream()
            .map(HistoricoAvaliacao::de)
            .toList();
    }
}