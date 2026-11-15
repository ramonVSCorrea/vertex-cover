# Validação da implementação

## Ambiente e comandos efetivamente executados

JDK 17.0.20.1 (Eclipse Temurin), Windows 11 Education, Intel Core i5-9300H e aproximadamente 15,84 GiB de memória física. Detalhes completos em `experimental-methodology.md` e `../data/results/benchmark-environment.md`.

Na raiz `vertex-cover/`:

```text
javac -encoding UTF-8 --release 17 -Xlint:all -d out src/*.java tests/*.java
java "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out TestRunner
java "-Dfile.encoding=UTF-8" -cp out Main generate --output data/instances/
java "-Dfile.encoding=UTF-8" -cp out Main solve --algorithm exact --input data/instances/exemplo.txt
java "-Dfile.encoding=UTF-8" -cp out Main solve --algorithm approximation --input data/instances/exemplo.txt
java "-Dfile.encoding=UTF-8" -cp out Main solve --algorithm exact --timeout-ms 0 --input data/instances/exemplo.txt
java -Xms128m -Xmx512m "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out Main benchmark --timeout-ms 5000 --repetitions 3 --warmup 100 --output data/results/benchmark.csv
java "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out Main charts --input data/results/benchmark.csv --output data/results/figures/
java "-Dfile.encoding=UTF-8" -cp out Main report --input data/results/benchmark.csv --output docs/experimental-results.md
```

Os comandos mínimos `javac -encoding UTF-8 -d out src/*.java tests/*.java` e `java -cp out TestRunner` também foram verificados. Não houve dependências baixadas.

## Resultados observados

- Compilação com `--release 17 -Xlint:all`: **sem erros e sem avisos**.
- TestRunner: **14 grupos aprovados, 0 reprovados**, incluindo 1.110 comparações com força bruta.
- Exemplo exato: `OPTIMAL`, tamanho **3**, cobertura `{2, 3, 4}`.
- Exemplo aproximado: `APPROXIMATE`, tamanho **4**, cobertura `{1, 2, 3, 5}`.
- Exemplo com timeout zero: `TIME_LIMIT`, cobertura válida de tamanho **4**, sem prova de ótimo.
- Suíte gerada: **200 aleatórias + 30 estruturadas**; sementes individuais registradas.
- Benchmark: **230 linhas-resumo**, **690 linhas de execuções individuais por algoritmo**.
- Resumo pelas medianas: **228 OPTIMAL e 2 TIME_LIMIT**.
- Execuções individuais do exato: **6 timeouts em 690 execuções**; nenhuma medição descartada.
- Coberturas válidas: **todas**; nenhum fator de aproximação acima de 2 nos casos com ótimo comprovado.
- Razão definida em **223** instâncias; média **1,492982**, mínimo **1**, máximo **2**. Cinco ótimos zero e dois timeouts não entram nessa amostra.
- Figuras: **seis PNGs de 1280 × 800**, decodificados nos testes e conferidos visualmente na suíte real.
- Os timeouts selecionados são `random_n30_p0.7_i06` e `complete_n30`. Seus campos de ótimo, razão e gap permanecem `NA`.
- Duração experimental registrada: aproximadamente **146,665 segundos**, incluindo validação/exportação/progresso; não deve ser confundida com um tempo individual do algoritmo.

Os CSVs brutos, o ambiente e os hashes do relatório permitem auditar esses números. Os tempos mudam em uma nova execução; não se promete que a quantidade de timeouts se repita.

## Critérios do planejamento

- [x] Java 17 puro, pacote padrão, `src/` e `tests/`; sem gerenciadores/frameworks.
- [x] Modelo com adjacência, graus, densidade, isolados e rejeição de grafos inválidos.
- [x] Matching maximal 2-aproximado, prova e testes da garantia.
- [x] Branch and Bound com incumbente aproximada, matching residual, graus, nós e podas.
- [x] Timeout configurável, cobertura factível e distinção explícita de prova de ótimo.
- [x] Força bruta independente e testes das seis famílias estruturadas.
- [x] Arquivos base um, geradores G(n,p), sementes fixas e manifesto.
- [x] Aquecimento separado, três medições, seleção por mediana e preservação dos runs.
- [x] CSV com todas as métricas e NA sem falso ótimo ou divisão por zero.
- [x] Seis gráficos Java2D derivados dos dados reais, com legendas/unidades e tratamento de timeout.
- [x] README, teoria/NP-Completude, metodologia, hardware, execução e resultados reproduzíveis.

## Correções durante a validação

No PowerShell, a propriedade `-Dfile.encoding=UTF-8` precisou ser passada entre aspas para não ser interpretada incorretamente pelo terminal; a documentação usa a forma correta. Na conferência dos gráficos, a altura das barras de status foi alinhada exatamente à escala do eixo. Após as correções, o projeto foi recompilado e os testes executados novamente.

## Atualização: execução simplificada e apresentação

O registro experimental acima continua sendo o da execução original. As melhorias de interface **não substituíram os CSVs originais**, cujos hashes SHA-256 foram conferidos.

Foram acrescentados:

- `executar.cmd`: entra na pasta correta, compila com `javac` e abre o menu em UTF-8; também aceita comandos diretos.
- Menu numérico com demonstração explicada, comparação de arquivo, testes, experimento rápido/completo e análise de CSV.
- Desenho do grafo de demonstração e tabela indicando qual ponta cobre cada ligação.
- Tabelas comparativas, progresso de benchmark, resumos e explicação de timeout, sem códigos ANSI.
- Testes com resumo limpo e detalhes de integração exibidos somente em caso de falha.
- Fluxo rápido de **8 grafos**, com três medições, CSVs, seis PNGs e relatório em pasta própria.
- Fluxo completo de **230 grafos padrão**, separado do modo didático; a confirmação é solicitada no menu.
- Diretórios novos para as execuções guiadas; destinos já existentes são rejeitados.
- `COMECE-AQUI.md` e instruções no início do README.

Validação da nova interface:

```powershell
# Executados de Projetos, não de dentro de vertex-cover:
.\vertex-cover\executar.cmd demo
.\vertex-cover\executar.cmd experiment --mode quick
```

O experimento rápido real gerou **8 resultados com ótimo comprovado, 24 medições por algoritmo e seis PNGs**, na pasta `data/results/execucoes/rapido-20261115-094942-445/`.
O menu foi executado com entradas automatizadas, incluindo demonstração e duas chamadas seguidas dos testes: **16 grupos aprovados e zero falhas em cada chamada**, sem acumular contadores.

A suíte atual também verifica entradas inválidas, EOF, caminhos com espaços/aspas, recuperação após arquivo inexistente, cancelamento do experimento completo, análise sem modificar o CSV, proteção de destino existente e o fluxo completo de 230 instâncias com timeout zero. Esse último é um **teste de integração**, não uma nova medição do protocolo acadêmico de 5.000 ms.
