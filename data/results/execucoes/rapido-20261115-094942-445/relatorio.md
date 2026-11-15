# Resultados experimentais medidos

Documento gerado em Java diretamente do CSV real, sem números experimentais pré-preenchidos.
Consulte `docs/experimental-methodology.md`, na raiz do projeto, para o protocolo completo.
Os parâmetros e o ambiente desta execução estão no arquivo `*-environment.md` ao lado do CSV.

## Evidências

- Fonte: `C:/Users/Ramon Corrêa/Desktop/Projetos/vertex-cover/data/results/execucoes/rapido-20261115-094942-445/benchmark.csv`
- SHA-256 do CSV: `31dd5c6738cbac5755d17a38169332b553693f406d462087bc076e73314f989d`
- Instâncias no resumo: **8**.
- Execuções selecionadas: **8 OPTIMAL**, **0 TIME_LIMIT**.
- Ótimos iguais a zero: **1**; razão e gap são NA.
- Medições individuais por algoritmo: **24**; timeouts individuais do exato: **0**.
- CSV individual: `C:/Users/Ramon Corrêa/Desktop/Projetos/vertex-cover/data/results/execucoes/rapido-20261115-094942-445/benchmark-runs.csv`; SHA-256: `483d6b25136239243f32e1579ac96ff2f902c2406c26760a6a769dfc9d4361bd`.

Os tempos abaixo são das execuções selecionadas pela mediana por instância. Métricas de qualidade incluem apenas ótimo comprovado e positivo.

## Qualidade da aproximação

- Instâncias com razão definida: **7**.
- Razão mínima/média/máxima: **1.111111 / 1.637302 / 2.000000**.
- Gap percentual médio/máximo: **63.730159% / 100.000000%**.
- Razões acima de 2: **0**.
- Coincidência de cardinalidade com o ótimo positivo: **0** instâncias. Isso não transforma o algoritmo aproximado em algoritmo exato.

## G(n,p), por configuração

Tempos em ms: mediana superior **entre as instâncias** de cada configuração,
depois da seleção por repetição. O tempo exato inclui eventuais observações censuradas
por timeout: não é uma estimativa de tempo até o ótimo nessas observações.
Razão média utiliza somente as linhas com prova e OPT>0; a coluna N da razão explicita sua amostra.

| n | p | instâncias | OPTIMAL | TIME_LIMIT | mediana exato (ms) | mediana aprox (ms) | N razão | razão média |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 0.3 | 1 | 1 | 0 | 0.044601 | 0.005700 | 1 | 1.500000 |
| 12 | 0.5 | 1 | 1 | 0 | 0.678201 | 0.019800 | 1 | 1.250000 |

## Instâncias estruturadas

Ótimo abaixo significa certificado pelo solver nesta execução, não uma fórmula externa.
Para TIME_LIMIT, o ótimo permanece NA mesmo em famílias cujo ótimo é conhecido teoricamente.

| instância | status | melhor conhecida | ótimo comprovado | aprox | tempo exato (ms) | nós explorados |
|---|---|---:|---:|---:|---:|---:|
| caminho_n10 | OPTIMAL | 5 | 5 | 10 | 0.138200 | 11 |
| ciclo_n9 | OPTIMAL | 5 | 5 | 8 | 0.077899 | 11 |
| estrela_n10 | OPTIMAL | 1 | 1 | 2 | 0.048801 | 3 |
| completo_n10 | OPTIMAL | 9 | 9 | 10 | 2.269601 | 513 |
| bipartido_n10 | OPTIMAL | 5 | 5 | 10 | 0.109499 | 11 |
| sem_arestas_n10 | OPTIMAL | 0 | 0 | 0 | 0.005200 | 0 |

## Interrupções selecionadas no resumo

As linhas seguintes não fornecem razão/gap nem certificado de ótimo.

| instância | melhor conhecida | aprox | tempo parcial (ms) | nós parciais |
|---|---:|---:|---:|---:|

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
java -cp out Main report --input "C:\Users\Ramon Corrêa\Desktop\Projetos\vertex-cover\data\results\execucoes\rapido-20261115-094942-445\benchmark.csv" --output "C:\Users\Ramon Corrêa\Desktop\Projetos\vertex-cover\data\results\execucoes\rapido-20261115-094942-445\relatorio.md"
```
