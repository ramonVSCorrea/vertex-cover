# Resultados experimentais medidos

Documento gerado em Java diretamente do CSV real, sem números experimentais pré-preenchidos.
Consulte `experimental-methodology.md` para hardware, comandos, sementes e limitações.

## Evidências

- Fonte: `data/results/benchmark.csv`
- SHA-256 do CSV: `e4e1d95f17c429d6e29fa7955b611f72e93de5824a04df5f1d6e196065a07607`
- Instâncias no resumo: **230**.
- Execuções selecionadas: **228 OPTIMAL**, **2 TIME_LIMIT**.
- Ótimos iguais a zero: **5**; razão e gap são NA.
- Medições individuais por algoritmo: **690**; timeouts individuais do exato: **6**.
- CSV individual: `data/results/benchmark-runs.csv`; SHA-256: `a58b4644f3349ee267273e1751f0e026c32151c58fd3d558e298d0234afe3a53`.

Os tempos abaixo são das execuções selecionadas pela mediana por instância. Métricas de qualidade incluem apenas ótimo comprovado e positivo.

## Qualidade da aproximação

- Instâncias com razão definida: **223**.
- Razão mínima/média/máxima: **1.000000 / 1.492982 / 2.000000**.
- Gap percentual médio/máximo: **49.298155% / 100.000000%**.
- Razões acima de 2: **0**.
- Coincidência de cardinalidade com o ótimo positivo: **2** instâncias. Isso não transforma o algoritmo aproximado em algoritmo exato.

## G(n,p), por configuração

Tempos em ms: mediana superior **entre as dez instâncias** de cada configuração,
depois da seleção por repetição. O tempo exato inclui eventuais observações censuradas
por timeout: não é uma estimativa de tempo até o ótimo nessas observações.
Razão média utiliza somente as linhas com prova e OPT>0; a coluna N da razão explicita sua amostra.

| n | p | instâncias | OPTIMAL | TIME_LIMIT | mediana exato (ms) | mediana aprox (ms) | N razão | razão média |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 0.1 | 10 | 10 | 0 | 0.025000 | 0.005699 | 10 | 2.000000 |
| 10 | 0.3 | 10 | 10 | 0 | 0.038700 | 0.006400 | 10 | 1.710000 |
| 10 | 0.5 | 10 | 10 | 0 | 0.062399 | 0.003200 | 10 | 1.496190 |
| 10 | 0.7 | 10 | 10 | 0 | 0.067300 | 0.002500 | 10 | 1.361905 |
| 15 | 0.1 | 10 | 10 | 0 | 0.027100 | 0.001399 | 10 | 1.960000 |
| 15 | 0.3 | 10 | 10 | 0 | 0.101700 | 0.002300 | 10 | 1.479206 |
| 15 | 0.5 | 10 | 10 | 0 | 0.440800 | 0.003300 | 10 | 1.313737 |
| 15 | 0.7 | 10 | 10 | 0 | 1.250699 | 0.004401 | 10 | 1.187879 |
| 20 | 0.1 | 10 | 10 | 0 | 0.024699 | 0.001700 | 10 | 1.850873 |
| 20 | 0.3 | 10 | 10 | 0 | 0.482499 | 0.004000 | 10 | 1.449301 |
| 20 | 0.5 | 10 | 10 | 0 | 5.951100 | 0.005101 | 10 | 1.293333 |
| 20 | 0.7 | 10 | 10 | 0 | 6.152199 | 0.001800 | 10 | 1.193382 |
| 25 | 0.1 | 10 | 10 | 0 | 0.048201 | 0.000700 | 10 | 1.651573 |
| 25 | 0.3 | 10 | 10 | 0 | 1.991800 | 0.001200 | 10 | 1.413252 |
| 25 | 0.5 | 10 | 10 | 0 | 17.049300 | 0.001901 | 10 | 1.211871 |
| 25 | 0.7 | 10 | 10 | 0 | 133.245399 | 0.002400 | 10 | 1.144286 |
| 30 | 0.1 | 10 | 10 | 0 | 0.053999 | 0.000701 | 10 | 1.652958 |
| 30 | 0.3 | 10 | 10 | 0 | 22.094301 | 0.002001 | 10 | 1.313506 |
| 30 | 0.5 | 10 | 10 | 0 | 560.041401 | 0.002300 | 10 | 1.221014 |
| 30 | 0.7 | 10 | 9 | 1 | 3865.782100 | 0.002600 | 9 | 1.125812 |

## Instâncias estruturadas

Ótimo abaixo significa certificado pelo solver nesta execução, não uma fórmula externa.
Para TIME_LIMIT, o ótimo permanece NA mesmo em famílias cujo ótimo é conhecido teoricamente.

| instância | status | melhor conhecida | ótimo comprovado | aprox | tempo exato (ms) | nós explorados |
|---|---|---:|---:|---:|---:|---:|
| path_n10 | OPTIMAL | 5 | 5 | 10 | 0.002700 | 11 |
| cycle_n10 | OPTIMAL | 5 | 5 | 10 | 0.002600 | 11 |
| star_n10 | OPTIMAL | 1 | 1 | 2 | 0.001200 | 3 |
| complete_n10 | OPTIMAL | 9 | 9 | 10 | 0.067999 | 513 |
| bipartite_n10 | OPTIMAL | 5 | 5 | 10 | 0.003900 | 11 |
| empty_n10 | OPTIMAL | 0 | 0 | 0 | 0.000801 | 0 |
| path_n15 | OPTIMAL | 7 | 7 | 14 | 0.042501 | 15 |
| cycle_n15 | OPTIMAL | 8 | 8 | 14 | 0.052501 | 17 |
| star_n15 | OPTIMAL | 1 | 1 | 2 | 0.010601 | 3 |
| complete_n15 | OPTIMAL | 14 | 14 | 14 | 3.110101 | 16383 |
| bipartite_n15 | OPTIMAL | 7 | 7 | 14 | 0.014401 | 15 |
| empty_n15 | OPTIMAL | 0 | 0 | 0 | 0.000200 | 0 |
| path_n20 | OPTIMAL | 10 | 10 | 20 | 0.009299 | 21 |
| cycle_n20 | OPTIMAL | 10 | 10 | 20 | 0.016699 | 21 |
| star_n20 | OPTIMAL | 1 | 1 | 2 | 0.003700 | 3 |
| complete_n20 | OPTIMAL | 19 | 19 | 20 | 81.000100 | 524289 |
| bipartite_n20 | OPTIMAL | 10 | 10 | 20 | 0.016799 | 21 |
| empty_n20 | OPTIMAL | 0 | 0 | 0 | 0.000200 | 0 |
| path_n25 | OPTIMAL | 12 | 12 | 24 | 0.007700 | 25 |
| cycle_n25 | OPTIMAL | 13 | 13 | 24 | 0.007800 | 27 |
| star_n25 | OPTIMAL | 1 | 1 | 2 | 0.001699 | 3 |
| complete_n25 | OPTIMAL | 24 | 24 | 24 | 2621.206900 | 16777215 |
| bipartite_n25 | OPTIMAL | 12 | 12 | 24 | 0.026800 | 25 |
| empty_n25 | OPTIMAL | 0 | 0 | 0 | 0.000200 | 0 |
| path_n30 | OPTIMAL | 15 | 15 | 30 | 0.009899 | 31 |
| cycle_n30 | OPTIMAL | 15 | 15 | 30 | 0.010000 | 31 |
| star_n30 | OPTIMAL | 1 | 1 | 2 | 0.001700 | 3 |
| complete_n30 | TIME_LIMIT | 29 | NA | 30 | 5000.000400 | 31109928 |
| bipartite_n30 | OPTIMAL | 15 | 15 | 30 | 0.136199 | 31 |
| empty_n30 | OPTIMAL | 0 | 0 | 0 | 0.000400 | 0 |

## Interrupções selecionadas no resumo

As linhas seguintes não fornecem razão/gap nem certificado de ótimo.

| instância | melhor conhecida | aprox | tempo parcial (ms) | nós parciais |
|---|---:|---:|---:|---:|
| random_n30_p0.7_i06 | 26 | 28 | 5000.060501 | 21986165 |
| complete_n30 | 29 | 30 | 5000.000400 | 31109928 |

## Interpretação e limites

- Os testes independentes verificam corretude em grafos pequenos; um benchmark não substitui a prova.
- O matching aproxima rapidamente, mas pode selecionar até o dobro dos vértices necessários.
  Há famílias estruturadas em que o fator 2 é atingido; não se promete que a aproximação seja sempre ótima.
- A busca exata depende da estrutura, não apenas de n ou p. O matching inferior pode ser fraco em completos.
- TIME_LIMIT é uma observação parcial, não uma solução ótima nem um tempo completo de resolução.
- Não se deve generalizar as médias de razão às instâncias sem ótimo comprovado.
- Tempos muito pequenos estão sujeitos à resolução do relógio/JIT/GC. Os resultados são desta máquina/execução.
- Os seis PNGs em `data/results/figures/` distinguem medições de referências teóricas.

Regenerar:

```text
java -cp out Main report --input data/results/benchmark.csv --output docs/experimental-results.md
```
