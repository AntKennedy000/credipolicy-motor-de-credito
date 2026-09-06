package br.com.credipolicy;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/api/status")
    public Map<String, String> consultarStatus() {
        return Map.of(
            "projeto", "CrediPolicy",
            "status", "Em funcionamento",
            "finalidade", "Simulador educacional de politicas de credito"
        );
    }
}