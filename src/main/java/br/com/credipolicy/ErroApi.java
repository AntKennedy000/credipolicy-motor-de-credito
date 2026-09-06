package br.com.credipolicy;

import java.util.List;

public record ErroApi(
    String codigo,
    String mensagem,
    List<String> detalhes
) {
}