# 🕸️ TP 2 - PAA: Estudo de um Problema NP-Completo - Cobertura por Vértices

> 🎓 Trabalho prático de **Projeto e Análise de Algoritmos (PAA)** da **PUC Minas**: estudo do problema de Cobertura Mínima por Vértices e comparação entre um algoritmo exato e uma aproximação com garantia de fator 2.

**Alunos:** Ian Pereira Pinto Bomfim · João Miguel de Abreu Constâncio · Ramon Vinícius Silva Corrêa

O projeto executa **Branch and Bound** e **Matching 2-aproximado** sobre as **mesmas instâncias de grafos**, mede tempo e esforço de busca, verifica a validade das coberturas e exporta CSVs, gráficos PNG e um relatório dos resultados reais.

**☕ Tecnologia:** Java 17 puro · **🔧 Compilação:** `javac` · **▶️ Execução:** `java` · **📦 Dependências externas:** nenhuma

**📁 Diretório do projeto:** `vertex-cover`

> 💡 **Sem Maven, Gradle, Spring ou frameworks.** Todos os arquivos `.java` ficam diretamente em `src/` e `tests/`, sem declarações de pacote. Não é necessário usar uma IDE.

## ⚡ Só quero abrir e entender o projeto

**Dê dois cliques em [`executar.cmd`](executar.cmd)** dentro da pasta `vertex-cover`.
Ele compila automaticamente e abre um **menu interativo**.

No PowerShell, se você estiver em `Projetos`, basta:

```powershell
.\vertex-cover\executar.cmd
```

**Comece pela opção `1` (demonstração) e depois pela `4` (experimento rápido).**
Não é necessário decorar os comandos abaixo. Veja o [guia para começar](COMECE-AQUI.md).

## 🧭 Sumário

- 🎯 [O problema](#o-problema)
- 🧠 [Algoritmos implementados](#algoritmos-implementados)
- 🚀 [Como executar](#como-executar)
- ✅ [Como executar os testes](#como-executar-os-testes)
- 📝 [Formato das instâncias](#formato-das-instancias)
- 🧪 [Protocolo experimental](#protocolo-experimental)
- 📂 [Arquivos gerados](#arquivos-gerados)
- 📈 [Gráficos em Java](#graficos-em-java)
- 📊 [Como interpretar os resultados](#como-interpretar-os-resultados)
- 🏗️ [Estrutura do projeto](#estrutura-do-projeto)
- 📚 [Documentação e escopo da entrega](#documentacao-e-escopo-da-entrega)

<a id="o-problema"></a>
## 🎯 O problema

Dado um grafo simples e não direcionado **G = (V, E)**, uma **cobertura por vértices** é um conjunto **C ⊆ V** tal que toda aresta possui pelo menos uma extremidade em C. O objetivo é encontrar uma cobertura com a **menor quantidade possível de vértices**.

| Conceito | Significado |
| --- | --- |
| **Cobertura válida** | Todas as arestas estão cobertas |
| **Cobertura mínima/ótima** | Cobertura válida de menor cardinalidade possível |
| **Melhor solução conhecida** | Cobertura válida encontrada até o momento, sem necessariamente haver prova de ótimo |

<details>
<summary><strong>🔎 Por que estudar um problema NP-Completo?</strong></summary>

A versão de **decisão** pergunta se existe uma cobertura com no máximo `k` vértices e é **NP-completa**. A versão de **otimização**, implementada aqui, procura a menor cobertura e é **NP-difícil**.

Essa distinção motiva a comparação: o algoritmo exato pode comprovar o ótimo, mas tem pior caso exponencial; a aproximação executa em tempo linear no tamanho da representação do grafo e garante uma cobertura de tamanho no máximo duas vezes o ótimo.

A definição formal, a demonstração de NP-Completude, as aplicações e as referências estão em [notas teóricas](docs/algorithm-notes.md).

</details>

<a id="algoritmos-implementados"></a>
## 🧠 Algoritmos implementados

| Algoritmo | Estratégia | Garantia | Complexidade |
| --- | --- | --- | --- |
| **Branch and Bound** | Ramifica pelos extremos de uma aresta residual e poda com limites | Ótimo quando a busca termina com prova | `O(2^\|V\| · poly(\|V\|, \|E\|))` no pior caso |
| **Matching 2-aproximado** | Constrói um matching maximal e seleciona seus dois extremos | Cobertura válida com `\|C\| <= 2OPT` | `O(\|V\| + \|E\|)` |

O **Branch and Bound** começa com a solução da aproximação como limite superior. Um matching maximal no grafo residual fornece o limite inferior; o ramo é podado quando `selecionados + LB >= melhor`. A busca prioriza arestas de maior soma de graus residuais e explora primeiro o extremo de maior grau. Uma pilha explícita e cópias de `BitSet` isolam os estados e evitam restaurações incorretas.

<details>
<summary><strong>🔗 De onde vem a garantia de fator 2?</strong></summary>

As `M` arestas do matching não compartilham extremos, então qualquer cobertura precisa de pelo menos `M` vértices: `M <= OPT`. Como o algoritmo seleciona os dois extremos de cada aresta escolhida, sua cobertura tem `2M <= 2OPT` vértices.

O matching é **maximal**: nenhuma outra aresta pode ser acrescentada sem repetir um extremo. Assim, não pode existir uma aresta com ambos os extremos fora da cobertura. Não é necessário encontrar um matching **máximo**.

</details>

> ⚠️ **Timeout não significa ótimo.** Se a busca exata for interrompida, ela retorna uma cobertura válida com status `TIME_LIMIT`, mas não declara sua otimalidade.

<a id="como-executar"></a>
## 🚀 Como executar

### 🪟 Jeito recomendado: um arquivo, um menu

Com o **JDK 17** instalado, abra `executar.cmd` com dois cliques, ou use:

```powershell
# Se estiver em Projetos:
.\vertex-cover\executar.cmd

# Se já estiver em vertex-cover:
.\executar.cmd
```

O script entra na pasta certa, compila com `javac`, configura UTF-8 e abre:

```text
1  Ver uma demonstração explicada                    [COMECE AQUI]
2  Resolver um arquivo com os dois algoritmos
3  Executar os testes de corretude
4  Experimento rápido: dados + gráficos + relatório   [8 grafos]
5  Experimento completo do trabalho                   [230 grafos]
6  Gerar gráficos e relatório de um CSV existente
7  Entender o projeto e os arquivos de saída
0  Sair
```

**Sugestão:** opção 1 para entender a cobertura; opção 4 para ver todo o fluxo funcionando.
O modo rápido usa **8 grafos**, três medições por algoritmo e timeout de **1.000 ms**.
O completo usa **230 grafos padrão**, três medições e timeout de **5.000 ms**; pede confirmação e pode demorar.

Novas execuções guiadas ficam em **`data/results/execucoes/`**, cada uma em sua própria pasta.
Ao terminar, leia **`relatorio.md`** e abra os PNGs em **`figures/`**.
O benchmark original não é sobrescrito. Um destino de experimento já existente é rejeitado.

> 💡 O `executar.cmd` é apenas um atalho para `javac` e `java`, não um gerenciador de projetos.

<details>
<summary><strong>⌨️ Prefiro executar sem menu ou compilar manualmente</strong></summary>

Os comandos abaixo devem ser executados **dentro de `vertex-cover`**, onde ficam `src/` e `tests/`.
Se o terminal estiver em `Projetos`, execute **`cd .\vertex-cover`** antes de compilar.

Atalhos que compilam e executam sem abrir o menu:

```powershell
.\executar.cmd demo
.\executar.cmd test
.\executar.cmd experiment --mode quick
```

### 1. ☕ Verificar o Java

```text
java -version
javac -version
```

### 2. 🔧 Compilar

Os comandos funcionam no **CMD**, no **PowerShell** e em terminais com JDK 17:

```text
javac -encoding UTF-8 -d out src/*.java tests/*.java
java -cp out Main
```

Sem argumentos, `Main` abre o menu. A pasta `out/` é criada pelo compilador e ignorada pelo Git.
Para verificar explicitamente a compatibilidade com Java 17 e habilitar os avisos:

```text
javac -encoding UTF-8 --release 17 -Xlint:all -d out src/*.java tests/*.java
```

### 3. ▶️ Resolver uma instância

```text
java -cp out Main solve --algorithm exact --input data/instances/exemplo.txt
java -cp out Main solve --algorithm approximation --input data/instances/exemplo.txt
```

Para comparar os dois algoritmos em uma única tabela:

```text
java -cp out Main compare --input data/instances/exemplo.txt
java -cp out Main demo
```

Na instância de exemplo, o **ótimo é 3** e a **aproximação tem tamanho 4**, considerando a ordem das arestas fornecida.

Para ajustar o timeout ou consultar a ajuda:

```text
java -cp out Main solve --algorithm exact --input data/instances/exemplo.txt --timeout-ms 5000
java -cp out Main solve --algorithm exact --input data/instances/exemplo.txt --timeout-ms 0
java -cp out Main help
```

> 🪟 **Acentos no Windows:** se necessário, use `java "-Dfile.encoding=UTF-8" ...`. As aspas são importantes no PowerShell. Os arquivos do projeto são lidos e gravados em UTF-8.

</details>

<a id="como-executar-os-testes"></a>
## ✅ Como executar os testes

No menu, escolha **3**. Ou execute `.\executar.cmd test`, que compila e testa automaticamente.
Após uma compilação manual, o comando tradicional também funciona:

```text
java -cp out TestRunner
```

Os testes **não exigem JUnit, `-ea` ou acesso à rede**. São **16 grupos independentes**, com uma linha por grupo, duração e resumo final. Detalhes das integrações aparecem somente em caso de falha; qualquer falha encerra o processo com status diferente de zero.

| Área | Verificações |
| --- | --- |
| **Modelo do grafo** | Graus, densidade, isolados, imutabilidade, laços, duplicatas e índices inválidos |
| **Corretude do exato** | Comparação com força bruta em **1.110 grafos**: 1.024 de cinco vértices, 85 aleatórios de até 16 vértices e um caminho de 20 vértices |
| **Casos estruturados** | Ótimos conhecidos de caminhos, ciclos, estrelas, completos, bipartidos completos e grafos sem arestas; caso desconexo |
| **Aproximação** | Matching disjunto e maximal, cobertura válida e garantia `\|C\| <= 2OPT` |
| **Timeout** | Limites zero e positivo, preservação da cobertura e ausência de falso certificado de ótimo |
| **Integração** | Reprodução das 230 instâncias, arquivos/CSV, repetições, mediana, seis PNGs e rejeição de argumentos inválidos |
| **Experiência de uso** | Menu, entrada inválida, caminhos com espaços, EOF, demonstração e fluxo rápido completo sem sobrescrever resultados |

Os testes de integração geram medições próprias em pastas temporárias `opencode/vertex-cover-tests-*`; **não alteram o benchmark oficial**.

<a id="formato-das-instancias"></a>
## 📝 Formato das instâncias

A primeira linha contém `n m`: número de vértices e de arestas. As demais linhas contêm exatamente `m` pares de identificadores **de 1 a n**:

```text
5 6
1 2
1 3
2 3
2 4
3 5
4 5
```

- **Índices internos:** `0..n-1`; arquivos e soluções impressas usam `1..n`.
- **Rejeitados:** laços, índices inválidos, arestas duplicadas — inclusive invertidas — e contagens incorretas.
- **Aceitos:** grafos desconexos, vértices isolados, grafos sem arestas e o grafo vazio `0 0`.
- **Leitura:** ignora linhas vazias, aceita BOM UTF-8 e rejeita dados excedentes.

<a id="protocolo-experimental"></a>
## 🧪 Protocolo experimental

> 🧭 **Pelo menu:** a opção **5** faz o protocolo completo abaixo automaticamente, incluindo gráficos e relatório em uma nova pasta. A opção **4** é apenas uma demonstração rápida de **8 grafos**, claramente separada da suíte completa. Os comandos individuais abaixo continuam disponíveis para controle manual.

### 1. 🎲 Gerar as instâncias

```text
java -cp out Main generate --output data/instances/ --seed 20261115
```

| Família | Configuração | Quantidade |
| --- | --- | ---: |
| **Erdős–Rényi G(n,p)** | `n = 10, 15, 20, 25, 30`; `p = 0.1, 0.3, 0.5, 0.7`; 10 instâncias por par | **200** |
| **Grafos estruturados** | Caminho, ciclo, estrela, completo, bipartido completo balanceado e sem arestas, nos mesmos cinco tamanhos | **30** |
| **Total** | Instâncias identificadas em `manifest.csv` | **230** |

As sementes aleatórias são `20261115 + i`, para `i = 0..199`, percorrendo n, depois p e depois repetição. O gerador usa `java.util.Random`, pares `u < v` em ordem crescente e teste `nextDouble() < p`. O manifesto registra identificador, família, semente, n, m, p e arquivo; sementes de grafos estruturados são `NA`.

> 📝 **Preservação de dados:** `generate` regrava os arquivos da suíte gerada e o manifesto, mas não altera `exemplo.txt`. Use outro `--output` para guardar uma suíte anterior.

### 2. ⏱️ Executar o benchmark

Execução simples:

```text
java -cp out Main benchmark --timeout-ms 5000 --output data/results/benchmark.csv
```

Para reproduzir o protocolo da execução entregue, com **três medições por algoritmo e instância**:

```text
java -Xms128m -Xmx512m "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp out Main benchmark --timeout-ms 5000 --repetitions 3 --warmup 100 --output data/results/benchmark.csv
```

| Parâmetro | Convenção |
| --- | --- |
| **Instância comparada** | Exatamente o mesmo grafo para os dois algoritmos |
| **Aquecimento** | 100 pares de execuções em grafos pequenos e separados |
| **Medições** | Padrão da CLI: 1; protocolo entregue: **3 por algoritmo/instância** |
| **Resumo temporal** | Execução real na **mediana superior** de cada algoritmo; com três repetições, a mediana usual |
| **Timeout do exato** | **5.000 ms por execução**, ajustável por `--timeout-ms` |
| **Relógio** | `System.nanoTime()` |
| **Fora do tempo individual** | Geração/leitura do grafo, validação posterior, escrita e gráficos |

O tempo do exato inclui sua aproximação inicial e a preparação interna. Todas as repetições são preservadas; status, solução, nós e podas do resumo pertencem à execução exata selecionada, não a médias artificiais.

O benchmark lê os arquivos indicados pelo manifesto. Sem `--input`, gera a suíte padrão somente se `data/instances/manifest.csv` não existir. Um `--input` explícito deve apontar para um diretório com manifesto válido.

<details>
<summary><strong>🔧 Sobre memória, repetições e reprodução</strong></summary>

- `-Xms128m` e `-Xmx512m` configuram o heap da JVM; não são dependências.
- `java.awt.headless=true` permite gerar gráficos sem interface gráfica.
- `--repetitions` vale **1** por padrão; `--warmup` vale **100**.
- Com quantidade par de repetições, seleciona-se a mediana **superior**, sem criar uma duração média sem status ou contagem de nós próprios.
- Os arquivos são regravados no caminho escolhido. Use outro nome de saída para preservar experimentos anteriores.
- Grafos e resultados combinatórios são reproduzíveis; tempos, timeouts e incumbentes após interrupção podem variar.
- Hardware, sementes, campos do CSV e comandos detalhados estão na [metodologia experimental](docs/experimental-methodology.md).

</details>

### 3. 📈 Gerar os gráficos e o relatório

```text
java -cp out Main charts --input data/results/benchmark.csv --output data/results/figures/
java -cp out Main report --input data/results/benchmark.csv --output docs/experimental-results.md
```

Ambos leem os **CSVs reais**. O relatório também registra hashes **SHA-256** para identificar os arquivos analisados.

<a id="arquivos-gerados"></a>
## 📂 Arquivos gerados

### Fluxo guiado: onde abrir os resultados novos

```text
data/results/execucoes/
└── rapido-DATA-HORA/                 Ou completo-DATA-HORA/
    ├── LEIA-ME.txt                   Explica os arquivos desta execução
    ├── instances/                    Os grafos medidos e suas sementes
    ├── benchmark.csv                 Resumo por grafo
    ├── benchmark-runs.csv            Todas as medições
    ├── benchmark-environment.md      Ambiente e parâmetros
    ├── figures/                      Abra os seis PNGs
    ├── relatorio.md                  Comece a análise por este arquivo
    └── CONCLUIDO.txt                 Criado ao terminar todas as etapas
```

A opção **6** cria uma pasta `analise-DATA-HORA/` com gráficos e relatório, sem modificar o CSV de entrada.

### Comandos individuais: conjunto original do trabalho

```text
data/
├── instances/
│   ├── exemplo.txt                   Instância para execução individual
│   ├── manifest.csv                  Identificadores, famílias, sementes e arquivos
│   └── *.txt                         200 grafos aleatórios + 30 estruturados
└── results/
    ├── benchmark.csv                 Uma linha por instância, após seleção por mediana
    ├── benchmark-runs.csv            Todas as execuções individuais, inclusive timeouts
    ├── benchmark-environment.md      Ambiente, parâmetros e contagens da execução
    └── figures/
        ├── 01-runtime-vs-n.png
        ├── 02-approx-vs-optimum.png
        ├── 03-ratio-vs-n.png
        ├── 04-runtime-vs-density.png
        ├── 05-explored-nodes.png
        ├── 06-status.png
        └── README.md                 Notas de interpretação das figuras
```

Os CSVs usam **vírgula como separador**, **ponto decimal** e tempos em **nanossegundos**. Para obter milissegundos, divida por `1.000.000`. Campos desconhecidos ou não aplicáveis recebem **`NA`**.

As colunas `*_selected_repetition` identificam a repetição selecionada. O arquivo de ambiente registra data, SO, Java, processadores disponíveis, heap, parâmetros e timeouts de **todas** as repetições.

<a id="graficos-em-java"></a>
## 📈 Gráficos em Java

As figuras são produzidas exclusivamente com **Java2D e ImageIO** (`BufferedImage`, `Graphics2D` e `ImageIO`), sem Python ou bibliotecas externas.

| Gráfico | O que apresenta |
| --- | --- |
| **Tempo × vértices** | Duração dos dois algoritmos em escala logarítmica |
| **Aproximado × ótimo** | Tamanhos das coberturas com ótimo comprovado |
| **Razão × vértices** | Qualidade da aproximação e referência teórica de fator 2 |
| **Tempo × densidade** | Tempos em função da densidade observada, não do parâmetro p |
| **Nós explorados** | Esforço do Branch and Bound, usando `nós + 1` em escala logarítmica para incluir zero |
| **Status da busca** | Quantidades de instâncias `OPTIMAL` e `TIME_LIMIT` |

Cada PNG contém **título, eixos, unidades, legenda, fonte e quantidade de instâncias**. Cruzes vermelhas indicam tempos parciais de timeout; linhas tracejadas identificam referências teóricas. Não há preenchimento manual, interpolação ou deslocamento artificial dos pontos.

<a id="como-interpretar-os-resultados"></a>
## 📊 Como interpretar os resultados

### 🚦 Entenda o status antes de comparar

| Status | Interpretação |
| --- | --- |
| **`OPTIMAL`** | Todos os ramos relevantes foram explorados ou podados; o mínimo foi comprovado |
| **`TIME_LIMIT`** | A busca foi interrompida; a cobertura é válida, mas é apenas a melhor conhecida |
| **`APPROXIMATE`** | Resultado do algoritmo aproximado, mesmo quando coincide com o ótimo |

1. Consulte [`benchmark.csv`](data/results/benchmark.csv) para comparar os algoritmos por instância.
2. Use [`benchmark-runs.csv`](data/results/benchmark-runs.csv) para examinar **todas** as repetições e identificar timeouts que possam não aparecer na mediana.
3. Compare **tempo**, **tamanho da cobertura**, **nós explorados** e **podas** separadamente.
4. Calcule razão `aprox/OPT` e gap `100 × (aprox−OPT)/OPT` **somente com ótimo comprovado e positivo**.
5. Para ótimo zero, razão e gap são `NA`; verifique separadamente que a aproximação também é vazia.

> ⚠️ **Não use a incumbente de um timeout como se fosse o ótimo.** Os campos de ótimo, razão e gap permanecem `NA`. O tempo parcial de uma interrupção não representa o tempo necessário para resolver a instância.

### 🧾 Registro da execução entregue

| Evidência | Resultado |
| --- | ---: |
| **Grupos de testes aprovados (versão atual)** | 16 de 16 |
| **Instâncias avaliadas** | 230 |
| **Medições por algoritmo** | 690 |
| **Ótimos comprovados no resumo pelas medianas** | 228 |
| **Timeouts no resumo pelas medianas** | 2 |
| **Timeouts em todas as execuções individuais do exato** | 6 |
| **Gráficos PNG gerados** | 6 |

As contagens e os tempos do benchmark acima pertencem ao experimento original; as melhorias de interface não regravaram esses dados.
Os resultados detalhados estão no [relatório experimental](docs/experimental-results.md), e os comandos efetivamente executados estão no [registro de validação](docs/validation.md).

> 🧠 **Limites da análise:** o Branch and Bound continua exponencial no pior caso. O timeout é cooperativo e pode ser ligeiramente excedido por preparação, operações em andamento ou GC. Com limite zero, grafos com arestas retornam `TIME_LIMIT`; grafos sem arestas têm ótimo zero demonstrável imediatamente. Tempos dependem de hardware, JVM, JIT e carga do sistema; os gráficos não são uma prova de complexidade assintótica ou de significância estatística.

<a id="estrutura-do-projeto"></a>
## 🏗️ Estrutura do projeto

```text
vertex-cover/
├── README.md
├── COMECE-AQUI.md                    Guia para quem está começando
├── executar.cmd                     Compila e abre o menu no Windows
├── .gitignore
├── src/
│   ├── Main.java                       Interface de terminal
│   ├── InteractiveMenu.java            Menu guiado e explicações
│   ├── ConsoleView.java                Tabelas, títulos e mensagens
│   ├── ExperimentWorkflow.java         Fluxo automático em uma nova pasta
│   ├── Edge.java                       Aresta normalizada
│   ├── Graph.java                      Arestas, adjacência e validação
│   ├── VertexCoverSolution.java        Cobertura imutável
│   ├── SolverResult.java               Status, tempo e estatísticas
│   ├── ExactBranchAndBound.java        Algoritmo exato
│   ├── MatchingTwoApproximation.java   Matching e aproximação
│   ├── GraphFileReader.java            Leitura e escrita das instâncias
│   ├── GraphGenerator.java             G(n,p), famílias e manifesto
│   ├── BenchmarkRunner.java            Aquecimento, medições e ambiente
│   ├── CsvExporter.java                Leitura e escrita CSV
│   ├── ChartGenerator.java             Gráficos Java2D
│   └── ExperimentReport.java           Relatório Markdown dos dados reais
├── tests/
│   └── TestRunner.java                 Testes sem dependências
├── data/
│   ├── instances/
│   └── results/
│       └── figures/
└── docs/
    ├── algorithm-notes.md
    ├── experimental-methodology.md
    ├── experimental-results.md
    └── validation.md
```

A separação mantém **modelo**, **algoritmos**, **experimentos** e **exportação** em classes simples, usando apenas a biblioteca padrão do JDK. Não existe estrutura `src/main/java`, gerenciador de dependências ou configuração obrigatória de IDE.

<a id="documentacao-e-escopo-da-entrega"></a>
## 📚 Documentação e escopo da entrega

| Documento | Conteúdo |
| --- | --- |
| [🚀 Comece aqui](COMECE-AQUI.md) | Abrir o programa, entender a demonstração e localizar os resultados |
| [🧠 Notas teóricas](docs/algorithm-notes.md) | Definição formal, NP-Completude, aplicações, provas, complexidade e referências |
| [🧪 Metodologia experimental](docs/experimental-methodology.md) | Hardware, sementes, métricas, esquema CSV e reprodução |
| [📊 Resultados medidos](docs/experimental-results.md) | Tabelas dos dados reais, timeouts e hashes dos CSVs |
| [✅ Validação](docs/validation.md) | Comandos executados, testes e critérios de aceite |

Este projeto reúne **código, testes, instâncias, resultados e figuras** para apoiar o relatório e a apresentação do trabalho. A publicação no GitHub e no Overleaf fica a cargo do grupo.
