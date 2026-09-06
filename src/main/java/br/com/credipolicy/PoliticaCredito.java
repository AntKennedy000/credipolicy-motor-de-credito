package br.com.credipolicy;

import java.math.BigDecimal;

public interface PoliticaCredito {

    TipoPolitica tipo();

    String versao();

    ClassificacaoScore classificarScore(int score);

    BigDecimal percentualMaximoComprometimento();
}