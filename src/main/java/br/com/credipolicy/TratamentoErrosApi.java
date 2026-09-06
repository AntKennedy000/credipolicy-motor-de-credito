package br.com.credipolicy;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratamentoErrosApi {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroApi> tratarValidacao(
            ConstraintViolationException exception) {

        List<String> detalhes = exception
            .getConstraintViolations()
            .stream()
            .map(violacao ->
                violacao.getPropertyPath()
                    + ": "
                    + violacao.getMessage()
            )
            .sorted()
            .toList();

        ErroApi erro = new ErroApi(
            "DADOS_INVALIDOS",
            "Revise os campos da proposta.",
            detalhes
        );

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApi> tratarCorpoInvalido(
            HttpMessageNotReadableException exception) {

        ErroApi erro = new ErroApi(
            "REQUISICAO_INVALIDA",
            "Nao foi possivel interpretar os dados enviados.",
            List.of(
                "Envie um objeto JSON com os campos da proposta.",
                "Use CONSERVADORA ou FLEXIVEL no campo politica.",
                "Informe valores numericos sem simbolo de moeda."
            )
        );

        return ResponseEntity.badRequest().body(erro);
    }
}