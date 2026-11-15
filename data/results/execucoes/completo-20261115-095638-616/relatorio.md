# Resultados experimentais medidos

Documento gerado em Java diretamente do CSV real, sem números experimentais pré-preenchidos.
Consulte `docs/experimental-methodology.md`, na raiz do projeto, para o protocolo completo.
Os parâmetros e o ambiente desta execução estão no arquivo `*-environment.md` ao lado do CSV.

## Evidências

- Fonte: `C:/Users/Ramon Corrêa/Desktop/Projetos/vertex-cover/data/results/execucoes/completo-20261115-095638-616/benchmark.csv`
- SHA-256 do CSV: `8f63069aff8536759351a04de6be613ffd4e3f0da18124e24a7a23f5e600c578`
- Instâncias no resumo: **230**.
- Execuções selecionadas: **229 OPTIMAL**, **1 TIME_LIMIT**.
- Ótimos iguais a zero: **5**; razão e gap são NA.
- Medições individuais por algoritmo: **690**; timeouts individuais do exato: **3**.
- CSV individual: `C:/Users/Ramon Corrêa/Desktop/Projetos/vertex-cover/data/results/execucoes/completo-20261115-095638-616/benchmark-runs.csv`; SHA-256: `7d47f7551bec0304c6198cb923925bc2940f315f25e0d4bd11c3913092608b39`.

Os tempos abaixo são das execuções selecionadas pela mediana por instância. Métricas de qualidade incluem apenas ótimo comprovado e positivo.

## Qualidade da aproximação

- Instâncias com razão definida: **224**.
- Razão mínima/média/máxima: **1.000000 / 1.491124 / 2.000000**.
- Gap percentual médio/máximo: **49.112415% / 100.000000%**.
- Razões acima de 2: **0**.
- Coincidência de cardinalidade com o ótimo positivo: **2** instâncias. Isso não transforma o algoritmo aproximado em algoritmo exato.

## G(n,p), por configuração

Tempos em ms: mediana superior **entre as instâncias** de cada configuração,
depois da seleção por repetição. O tempo exato inclui eventuais observações censuradas
por timeout: não é uma estimativa de tempo até o ótimo nessas observações.
Razão média utiliza somente as linhas com prova e OPT>0; a coluna N da razão explicita sua amostra.

| n | p | instâncias | OPTIMAL | TIME_LIMIT | mediana exato (ms) | mediana aprox (ms) | N razão | razão média |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 0.1 | 10 | 10 | 0 | 0.011400 | 0.000800 | 10 | 2.000000 |
| 10 | 0.3 | 10 | 10 | 0 | 0.023699 | 0.001399 | 10 | 1.710000 |
| 10 | 0.5 | 10 | 10 | 0 | 0.031001 | 0.001199 | 10 | 1.496190 |
| 10 | 0.7 | 10 | 10 | 0 | 0.077799 | 0.002201 | 10 | 1.361905 |
| 15 | 0.1 | 10 | 10 | 0 | 0.025501 | 0.001300 | 10 | 1.960000 |
| 15 | 0.3 | 10 | 10 | 0 | 0.064700 | 0.001701 | 10 | 1.479206 |
| 15 | 0.5 | 10 | 10 | 0 | 0.264200 | 0.003100 | 10 | 1.313737 |
| 15 | 0.7 | 10 | 10 | 0 | 0.919700 | 0.004600 | 10 | 1.187879 |
| 20 | 0.1 | 10 | 10 | 0 | 0.015600 | 0.001100 | 10 | 1.850873 |
| 20 | 0.3 | 10 | 10 | 0 | 0.483399 | 0.003100 | 10 | 1.449301 |
| 20 | 0.5 | 10 | 10 | 0 | 4.359200 | 0.005799 | 10 | 1.293333 |
| 20 | 0.7 | 10 | 10 | 0 | 11.783500 | 0.003600 | 10 | 1.193382 |
| 25 | 0.1 | 10 | 10 | 0 | 0.589400 | 0.001700 | 10 | 1.651573 |
| 25 | 0.3 | 10 | 10 | 0 | 5.490800 | 0.003600 | 10 | 1.413252 |
| 25 | 0.5 | 10 | 10 | 0 | 22.665600 | 0.003300 | 10 | 1.211871 |
| 25 | 0.7 | 10 | 10 | 0 | 157.408300 | 0.003101 | 10 | 1.144286 |
| 30 | 0.1 | 10 | 10 | 0 | 0.045500 | 0.000801 | 10 | 1.652958 |
| 30 | 0.3 | 10 | 10 | 0 | 23.449400 | 0.002399 | 10 | 1.313506 |
| 30 | 0.5 | 10 | 10 | 0 | 492.315500 | 0.002900 | 10 | 1.221014 |
| 30 | 0.7 | 10 | 10 | 0 | 3845.370900 | 0.003000 | 10 | 1.120923 |

## Instâncias estruturadas

Ótimo abaixo significa certificado pelo solver nesta execução, não uma fórmula externa.
Para TIME_LIMIT, o ótimo permanece NA mesmo em famílias cujo ótimo é conhecido teoricamente.

| instância | status | melhor conhecida | ótimo comprovado | aprox | tempo exato (ms) | nós explorados |
|---|---|---:|---:|---:|---:|---:|
| path_n10 | OPTIMAL | 5 | 5 | 10 | 0.003000 | 11 |
| cycle_n10 | OPTIMAL | 5 | 5 | 10 | 0.002999 | 11 |
| star_n10 | OPTIMAL | 1 | 1 | 2 | 0.001500 | 3 |
| complete_n10 | OPTIMAL | 9 | 9 | 10 | 0.068900 | 513 |
| bipartite_n10 | OPTIMAL | 5 | 5 | 10 | 0.005699 | 11 |
| empty_n10 | OPTIMAL | 0 | 0 | 0 | 0.001501 | 0 |
| path_n15 | OPTIMAL | 7 | 7 | 14 | 0.059200 | 15 |
| cycle_n15 | OPTIMAL | 8 | 8 | 14 | 0.090200 | 17 |
| star_n15 | OPTIMAL | 1 | 1 | 2 | 0.016800 | 3 |
| complete_n15 | OPTIMAL | 14 | 14 | 14 | 3.139300 | 16383 |
| bipartite_n15 | OPTIMAL | 7 | 7 | 14 | 0.028500 | 15 |
| empty_n15 | OPTIMAL | 0 | 0 | 0 | 0.000301 | 0 |
| path_n20 | OPTIMAL | 10 | 10 | 20 | 0.016700 | 21 |
| cycle_n20 | OPTIMAL | 10 | 10 | 20 | 0.010300 | 21 |
| star_n20 | OPTIMAL | 1 | 1 | 2 | 0.005300 | 3 |
| complete_n20 | OPTIMAL | 19 | 19 | 20 | 76.989200 | 524289 |
| bipartite_n20 | OPTIMAL | 10 | 10 | 20 | 0.031401 | 21 |
| empty_n20 | OPTIMAL | 0 | 0 | 0 | 0.000200 | 0 |
| path_n25 | OPTIMAL | 12 | 12 | 24 | 0.022300 | 25 |
| cycle_n25 | OPTIMAL | 13 | 13 | 24 | 0.019400 | 27 |
| star_n25 | OPTIMAL | 1 | 1 | 2 | 0.009900 | 3 |
| complete_n25 | OPTIMAL | 24 | 24 | 24 | 2480.738100 | 16777215 |
| bipartite_n25 | OPTIMAL | 12 | 12 | 24 | 0.027100 | 25 |
| empty_n25 | OPTIMAL | 0 | 0 | 0 | 0.000501 | 0 |
| path_n30 | OPTIMAL | 15 | 15 | 30 | 0.010499 | 31 |
| cycle_n30 | OPTIMAL | 15 | 15 | 30 | 0.011400 | 31 |
| star_n30 | OPTIMAL | 1 | 1 | 2 | 0.003099 | 3 |
| complete_n30 | TIME_LIMIT | 29 | NA | 30 | 5000.051999 | 31741510 |
| bipartite_n30 | OPTIMAL | 15 | 15 | 30 | 0.089700 | 31 |
| empty_n30 | OPTIMAL | 0 | 0 | 0 | 0.000901 | 0 |

## Interrupções selecionadas no resumo

As linhas seguintes não fornecem razão/gap nem certificado de ótimo.

| instância | melhor conhecida | aprox | tempo parcial (ms) | nós parciais |
|---|---:|---:|---:|---:|
| complete_n30 | 29 | 30 | 5000.051999 | 31741510 |

## Interpretação e limites

- Os testes independentes verificam corretude em grafos pequenos; um benchmark não substitui a prova.
- O matching aproxima rapidamente, mas pode selecionar até o dobro dos vértices necessários.
  Há famílias estruturadas em que o fator 2 é atingido; não se promete que a aproximação seja sempre ótima.
- A busca exata depende da estrutura, não apenas de n ou p. O matching inferior pode ser fraco em completos.
- TIME_LIMIT é uma observação parcial, não uma solução ótima nem um tempo completo de resolução.
- Não se deve generalizar as médias de razão às instâncias sem ótimo comprovado.
- Tempos muito pequenos estão sujeitos à resolução do relógio/JIT/GC. Os resultados são desta máquina/execução.
- Os seis PNGs ficam em `figures/` na execução guiada, ou `data/results/figures/` no benchmark padrão.
  As figuras distinguem medições de referências teóricas.

Regenerar este relatório (a partir da raiz do projeto):

```text
java -cp out Main report --input "C:\Users\Ramon Corrêa\Desktop\Projetos\vertex-cover\data\results\execucoes\completo-20261115-095638-616\benchmark.csv" --output "C:\Users\Ramon Corrêa\Desktop\Projetos\vertex-cover\data\results\execucoes\completo-20261115-095638-616\relatorio.md"
```
