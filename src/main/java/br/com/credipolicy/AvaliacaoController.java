package br.com.credipolicy;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoCreditoFacade facade;

    public AvaliacaoController(AvaliacaoCreditoFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResultadoAvaliacao avaliar(
            @RequestBody PropostaRequest proposta) {

        return facade.avaliar(proposta);
    }

    @GetMapping
    public List<HistoricoAvaliacao> listarRecentes() {
        return facade.listarRecentes();
    }
}