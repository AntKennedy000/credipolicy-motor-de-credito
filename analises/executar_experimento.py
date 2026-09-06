import csv
import json
from collections import Counter
from datetime import datetime, timezone
from decimal import Decimal
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen
from uuid import uuid4


RAIZ = Path(__file__).resolve().parents[1]
ARQUIVO_ENTRADA = RAIZ / "data" / "propostas_sinteticas.csv"
ENDERECO_API = "http://127.0.0.1:8080/api/avaliacoes"
POLITICAS = ("CONSERVADORA", "FLEXIVEL")

CAMPOS_SAIDA = [
    "experimento",
    "identificador",
    "politica",
    "versaoPolitica",
    "score",
    "rendaMensal",
    "compromissosMensais",
    "valorSolicitado",
    "prazoMeses",
    "decisao",
    "taxaMensal",
    "parcelaSimulada",
    "limiteMensalComprometimento",
    "totalComprometido",
    "motivo",
]


def enviar_proposta(proposta):
    requisicao = Request(
        ENDERECO_API,
        data=json.dumps(proposta).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    try:
        with urlopen(requisicao, timeout=30) as resposta:
            return json.loads(resposta.read().decode("utf-8"))

    except HTTPError as erro:
        detalhes = erro.read().decode("utf-8", errors="replace")
        raise RuntimeError(
            f"A API retornou HTTP {erro.code}: {detalhes}"
        ) from erro

    except URLError as erro:
        raise RuntimeError(
            "Nao foi possivel acessar a API. "
            "Confira se o CrediPolicy esta em execucao."
        ) from erro


def mostrar_resumo(resultados):
    print("\nRESULTADO DO EXPERIMENTO")
    print(
        f"{'Politica':<15} {'Total':>5} {'Aprov.':>7} "
        f"{'Recus.':>7} {'Manual':>7} {'Aprovacao':>11}"
    )

    for politica in POLITICAS:
        grupo = [
            item for item in resultados
            if item["politica"] == politica
        ]

        contagens = Counter(item["decisao"] for item in grupo)
        total = len(grupo)

        percentual = (
            Decimal(contagens["APROVADA"])
            * Decimal("100")
            / Decimal(total)
        )

        print(
            f"{politica:<15} {total:>5} "
            f"{contagens['APROVADA']:>7} "
            f"{contagens['RECUSADA']:>7} "
            f"{contagens['ANALISE_MANUAL']:>7} "
            f"{percentual:>10.2f}%"
        )

        volume = sum(
            (
                Decimal(str(item["valorSolicitado"]))
                for item in grupo
                if item["decisao"] == "APROVADA"
            ),
            Decimal("0"),
        )

        print(f"  Valor solicitado nas aprovadas: R$ {volume:.2f}")

    print(
        "\nOs percentuais consideram somente esta execucao, "
        "com a mesma base nas duas politicas."
    )
    print(
        "Mais aprovacoes nao comprovam menor risco "
        "ou maior rentabilidade."
    )


def main():
    with ARQUIVO_ENTRADA.open(
        "r", encoding="utf-8-sig", newline=""
    ) as arquivo:
        propostas = list(csv.DictReader(arquivo))

    if not propostas:
        raise ValueError("O arquivo de propostas esta vazio.")

    identificadores = [
        item["identificador"] for item in propostas
    ]

    if len(identificadores) != len(set(identificadores)):
        raise ValueError(
            "O CSV deve conter identificadores distintos."
        )

    instante = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    experimento = f"EXP-{instante}-{uuid4().hex[:8]}"

    pasta_saida = RAIZ / "analises" / "resultados" / experimento
    pasta_saida.mkdir(parents=True, exist_ok=False)

    arquivo_saida = pasta_saida / "avaliacoes.csv"
    resultados = []

    with arquivo_saida.open(
        "w", encoding="utf-8-sig", newline=""
    ) as arquivo:
        escritor = csv.DictWriter(
            arquivo,
            fieldnames=CAMPOS_SAIDA,
        )
        escritor.writeheader()

        for linha in propostas:
            for politica in POLITICAS:
                proposta = {
                    "identificador": (
                        f"{experimento}-{linha['identificador']}"
                    ),
                    "score": int(linha["score"]),
                    "rendaMensal": float(linha["rendaMensal"]),
                    "compromissosMensais": float(
                        linha["compromissosMensais"]
                    ),
                    "valorSolicitado": float(
                        linha["valorSolicitado"]
                    ),
                    "prazoMeses": int(linha["prazoMeses"]),
                    "politica": politica,
                }

                resposta = enviar_proposta(proposta)

                registro = {
                    "experimento": experimento,
                    **proposta,
                    **resposta,
                }

                escritor.writerow(registro)
                arquivo.flush()
                resultados.append(registro)

                print(
                    f"{linha['identificador']} | "
                    f"{politica} | {resposta['decisao']}"
                )

    mostrar_resumo(resultados)
    print(f"\nResultados salvos em:\n{arquivo_saida}")


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, RuntimeError) as erro:
        raise SystemExit(f"Experimento interrompido: {erro}")