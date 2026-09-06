package br.com.credipolicy;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PropostaRequest(

    @NotBlank(message = "Informe o identificador ficticio da proposta.")
    @Size(
        max = 50,
        message = "O identificador deve ter no maximo 50 caracteres."
    )
    String identificador,

    @NotNull(message = "Informe o score.")
    @Min(value = 0, message = "O score deve ser pelo menos 0.")
    @Max(value = 1000, message = "O score deve ser no maximo 1000.")
    Integer score,

    @NotNull(message = "Informe a renda mensal.")
    @DecimalMin(
        value = "0",
        inclusive = false,
        message = "A renda mensal deve ser maior que zero."
    )
    @Digits(
        integer = 12,
        fraction = 2,
        message = "Use ate 12 digitos inteiros e 2 casas decimais."
    )
    BigDecimal rendaMensal,

    @NotNull(message = "Informe os compromissos mensais existentes.")
    @DecimalMin(
        value = "0",
        message = "Os compromissos mensais nao podem ser negativos."
    )
    @Digits(
        integer = 12,
        fraction = 2,
        message = "Use ate 12 digitos inteiros e 2 casas decimais."
    )
    BigDecimal compromissosMensais,

    @NotNull(message = "Informe o valor solicitado.")
    @DecimalMin(
        value = "0",
        inclusive = false,
        message = "O valor solicitado deve ser maior que zero."
    )
    @Digits(
        integer = 12,
        fraction = 2,
        message = "Use ate 12 digitos inteiros e 2 casas decimais."
    )
    BigDecimal valorSolicitado,

    @NotNull(message = "Informe o prazo em meses.")
    @Min(value = 1, message = "O prazo deve ser de pelo menos 1 mes.")
    @Max(value = 60, message = "O prazo deve ser de no maximo 60 meses.")
    Integer prazoMeses,

    @NotNull(message = "Informe a politica de credito.")
    TipoPolitica politica

) {
}