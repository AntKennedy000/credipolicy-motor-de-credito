package br.com.credipolicy;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadoresController {

    private final IndicadoresRepository repository;

    public IndicadoresController(IndicadoresRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<IndicadoresPolitica> consultar() {
        return repository.consultar();
    }
}