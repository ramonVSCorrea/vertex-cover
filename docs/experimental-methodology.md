# Metodologia experimental

## Objetivo e conjunto de instâncias

Comparar tempo, qualidade da cobertura, prova de otimalidade e esforço de busca do Branch and Bound com a aproximação por matching maximal. A unidade experimental é um grafo salvo em arquivo e identificado no manifesto.

Sem reduzir ou ampliar os valores do planejamento:

| Família | Configuração | Quantidade |
|---|---|---:|
| Erdős–Rényi G(n,p) | n = 10,15,20,25,30; p = 0.1,0.3,0.5,0.7; 10 sementes por par | 200 |
| Caminho | mesmos cinco valores de n | 5 |
| Ciclo | mesmos cinco valores de n | 5 |
| Estrela | centro + n-1 folhas | 5 |
| Completo | K_n | 5 |
| Bipartido completo | K_floor(n/2),ceil(n/2) | 5 |
| Sem arestas | mesmos cinco valores de n | 5 |
| **Total** | | **230** |

Semente base **20261115**. As 200 sementes vão até **20261314**, inclusive. Ordem: n crescente, p crescente e índice de instância de 0 a 9. Cada grafo usa seu próprio `new Random(seed)`; percorre pares `(u,v)` com u crescente e v de u+1 a n-1, inserindo quando `nextDouble() < p`.

`manifest.csv` registra `instance_id,family,seed,n,m,p,file`. A ordem de arestas é preservada na escrita/leitura; ela influencia o matching guloso. A densidade observada não é confundida com p.

## Medição

1. Gerar ou ler os grafos **antes** da medição dos algoritmos.
2. Aquecer a JVM com 100 pares de execuções em grafos separados: n=8..10, p=0.35, sementes `-100000-i`, i=0..99. Esse aquecimento reduz, mas não elimina, efeitos do JIT.
3. Executar os dois algoritmos sobre a **mesma instância** e na mesma máquina.
4. No experimento entregue, medir **três execuções por algoritmo e instância** (690 do exato e 690 da aproximação). O padrão da CLI é uma execução; o comando da reprodução fixa três.
5. Alternar a ordem dos dois algoritmos pela paridade de índice da instância + número da repetição.
6. Usar `System.nanoTime()` dentro de cada solver. O exato inclui seu próprio upper bound inicial por aproximação, suas estruturas internas e a busca. A aproximação inclui a construção do matching/cobertura.
7. Excluir geração, leitura de instâncias, validação posterior, mensagens de progresso, CSV e gráficos dos tempos individuais.
8. Selecionar a execução real de tempo na mediana superior separadamente por algoritmo. Para r repetições ordenadas, índice `floor(r/2)` em base zero. Com r=3, a segunda execução ordenada. Com r par, não calcular a média das centrais, pois ela não teria status/nós próprios.
9. Preservar **todas** as medições em `benchmark-runs.csv`. A linha-resumo pode escolher diferentes números de repetição para os dois algoritmos. Status, incumbente, nós e podas do resumo sempre pertencem à execução exata escolhida.
10. Aplicar timeout inicial de **5.000 ms por execução exata**, configurável. Não descartar timeouts da análise. A aproximação não recebe timeout.

O benchmark verifica validade das duas coberturas, `OPT <= |C_aprox| <= 2OPT` quando há prova e cobertura vazia para ótimo zero. Em caso de inconsistência, encerra em vez de publicar silenciosamente resultados inválidos.

## Ambiente observado

- Máquina: **Acer Nitro AN517-51**.
- CPU: **Intel Core i5-9300H @ 2.40 GHz**, 4 núcleos físicos, 8 processadores lógicos.
- Memória física reportada: **17.009.004.544 bytes** (aproximadamente 15,84 GiB).
- SO: **Microsoft Windows 11 Education**, versão **10.0.26300**, 64 bits.
- JDK/JVM: **Eclipse Temurin OpenJDK 17.0.20.1+1**, OpenJDK 64-Bit Server VM.
- Compilação: `javac -encoding UTF-8 --release 17 -Xlint:all -d out src/*.java tests/*.java`.
- Execução oficial: heap inicial de 128 MiB e máximo de 512 MiB; demais argumentos abaixo.

Esses dados foram consultados no sistema durante a execução, não inferidos de resultados. O arquivo `benchmark-environment.md` registra também data UTC, versão Java efetiva, argumentos da JVM, número de aquecimentos, quantidade de repetições e timeouts. O Java padrão não fornece identificação portátil de toda a memória física; a consulta foi feita pelo PowerShell e o valor foi passado ao registro de ambiente pela variável opcional `VERTEX_COVER_PHYSICAL_MEMORY`. Em outra máquina, ela pode ser omitida.

## Comandos de reprodução

A partir da raiz:

```text
javac -encoding UTF-8 --release 17 -Xlint:all -d out src/*.java tests/*.java
java "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out TestRunner
java "-Dfile.encoding=UTF-8" -cp out Main generate --output data/instances/ --seed 20261115
java -Xms128m -Xmx512m "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out Main benchmark --timeout-ms 5000 --repetitions 3 --warmup 100 --output data/results/benchmark.csv
java "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out Main charts --input data/results/benchmark.csv --output data/results/figures/
java "-Dfile.encoding=UTF-8" -cp out Main report --input data/results/benchmark.csv --output docs/experimental-results.md
```

No PowerShell da medição, antes do benchmark foi usada:

```powershell
$env:VERTEX_COVER_PHYSICAL_MEMORY = '17009004544 bytes (15.84 GiB), Win32_ComputerSystem'
```

Os comandos sem opções de codificação/headless também funcionam; essas opções apenas tornam a execução neste terminal e sem janela explícita. O README inclui a sintaxe mínima solicitada no planejamento.

## Esquema do CSV

UTF-8, separador vírgula, decimal com ponto e cabeçalho em uma linha. Campos desconhecidos/não aplicáveis recebem **NA**. Strings com vírgula, aspas ou quebra de linha usam escaping CSV. Os tempos são inteiros em nanossegundos; os gráficos os convertem em milissegundos.

| Campo | Significado |
|---|---|
| instance_id | Identificador do manifesto |
| family | Família do grafo |
| seed | Semente de G(n,p); NA nas estruturadas |
| n, m | Quantidades de vértices e arestas |
| density | `2m/[n(n-1)]`, ou 0 para n<2 |
| p | Probabilidade de geração, NA fora de G(n,p) |
| exact_status | OPTIMAL ou TIME_LIMIT |
| exact_runtime_ns | Tempo medido da execução exata selecionada |
| exact_best_known_size | Cardinalidade da incumbente válida, mesmo em timeout |
| exact_optimal_size | Cardinalidade ótima **somente se comprovada** |
| exact_optimality_proven | true apenas para OPTIMAL |
| exact_nodes_explored | Estados retirados da pilha |
| exact_pruned_nodes | Estados descartados por limite/cardinalidade |
| approx_runtime_ns | Tempo medido da aproximação selecionada |
| approx_solution_size | Cardinalidade da cobertura aproximada |
| approx_valid | Validação de todas as arestas |
| approximation_ratio | `aprox/OPT`, apenas se comprovado e positivo |
| gap_percent | `100*(aprox-OPT)/OPT`, apenas se comprovado e positivo |
| exact_valid | Validade da incumbente exata |
| repetitions | Repetições consideradas na seleção do resumo; 1 em cada linha individual |
| exact_selected_repetition | Número da execução exata que forneceu a linha |
| approx_selected_repetition | Número da execução aproximada que forneceu a linha |

Se `OPT=0`, razão e gap são NA (não 0 nem 1); a aproximação é verificada separadamente como vazia. Se há timeout, `exact_optimal_size`, razão e gap são NA, mesmo que a família tenha uma fórmula de ótimo conhecida: o campo documenta o certificado emitido pelo algoritmo, não um certificado externo.

## Figuras e interpretação

As seis figuras Java2D leem exclusivamente `benchmark.csv`. Cada linha é uma instância, não cada repetição bruta. Os PNGs informam sua amostra e as exclusões:

- Tempos positivos são mostrados em escala log10, sem fabricar mínimo quando o relógio retorna zero.
- Cruz vermelha indica **tempo até interrupção**, uma observação parcial/censurada, não tempo para resolver.
- Comparação de tamanhos inclui apenas provas de ótimo, inclusive ótimo zero; razão exclui ótimo zero.
- Densidade usa a medida observada, não p.
- Nós mostram `contagem + 1` em escala log10, permitindo representar zero; contagens de timeout são parciais.
- Status conta a execução exata selecionada por instância. Para saber se **qualquer** repetição teve timeout, consultar `benchmark-runs.csv` e o ambiente.
- Linhas tracejadas são referências teóricas explícitas (`y=x`, `y=2x`, razão 2), não medições.
- Não há jitter, preenchimento manual nem interpolação de pontos; sobreposições são esperadas.

Uma nova execução pode gerar tempos, interrupções e até incumbentes de timeout diferentes. Escalonamento, carga externa, aquecimento incompleto, frequência da CPU e GC influenciam as medidas. Não se usa uma amostra de até 30 vértices como prova de comportamento assintótico nem como indicação de desempenho em grafos arbitrariamente grandes.

## Validação e rastreabilidade

O `TestRunner` compara o exato com um enumerador independente de subconjuntos em 1.110 grafos: 1.024 de cinco vértices, 85 aleatórios com sementes fixas e um caminho com 20 vértices. Também testa fórmulas das famílias estruturadas, matching maximal, fator 2, entradas inválidas, timeout e pipeline CSV/PNG.

As evidências da execução real e a interpretação das observações são registradas em `docs/experimental-results.md`. Não são resultados esperados ou pré-preenchidos.
