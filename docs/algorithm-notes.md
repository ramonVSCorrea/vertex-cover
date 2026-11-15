# Definição, fundamentos e algoritmos

## Problema e aplicações

Seja `G = (V,E)` um grafo simples não direcionado. `C ⊆ V` é uma cobertura por vértices quando, para toda aresta `(u,v) ∈ E`, `u ∈ C` ou `v ∈ C`. A otimização procura `min |C|`.

- **Válida:** cobre todas as arestas.
- **Mínima/ótima:** válida e de menor cardinalidade possível.
- **Melhor conhecida:** cobertura válida mantida pela busca; sem prova de ótimo se a busca não terminar.

Aplicações incluem posicionar monitores em nós para observar todas as conexões de uma rede e selecionar entidades que interceptem cada par conflitante. Essas interpretações dependem de o sistema real poder ser corretamente modelado por arestas binárias não direcionadas; não implicam que todo problema de segurança ou escalonamento seja Vertex Cover.

## NP-Completude da versão de decisão

A decisão pergunta: existe cobertura com tamanho no máximo `k`? Ela pertence a NP: dados os vértices do certificado, verificar o tamanho, os índices e todas as arestas custa `O(|V| + |E|)`.

Para demonstrar NP-dificuldade, reduza **CLIQUE**, um problema NP-completo. Dada uma instância `(G,k)` com `n` vértices e `0 <= k <= n`, construa o grafo complementar `H = complemento(G)` e pergunte se H tem cobertura com tamanho no máximo `n-k`.

1. S é clique em G se e somente se S é conjunto independente em H.
2. S é independente em H se e somente se `V \ S` cobre todas as arestas de H.
3. Logo G tem clique com pelo menos k vértices se e somente se H tem cobertura com no máximo n-k vértices.

A construção do complemento custa `O(n²)`, portanto é polinomial. Com pertinência a NP, **Vertex Cover de decisão é NP-completo**. A versão de otimização implementada é **NP-difícil**; não se deve chamar diretamente o problema de otimização de NP-completo.

## Aproximação por emparelhamento maximal

O algoritmo guloso percorre as arestas em ordem estável. Quando os dois extremos estão livres, acrescenta a aresta ao matching e os dois extremos à cobertura.

**Validade do matching:** seus extremos são marcados quando usados; portanto nenhuma dupla de arestas escolhidas compartilha vértices.

**Maximalidade:** se uma aresta terminasse com ambos os extremos livres, eles também estavam livres quando ela foi visitada, e a aresta teria sido escolhida. Contradição.

**Cobertura:** uma aresta descoberta teria ambos os extremos livres ao final, contradizendo a maximalidade.

**Garantia:** para M arestas disjuntas, toda cobertura precisa de pelo menos M vértices distintos, um por aresta. Portanto `M <= OPT`. A cobertura construída usa `2M` vértices, e assim `|C| = 2M <= 2OPT`. Isso não exige matching **máximo**: maximal é suficiente.

Tempo `O(|V| + |E|)` e espaço adicional `O(|V|)` para marcações e extremos do matching (o grafo é armazenado separadamente).

## Branch and Bound

Estado: conjunto escolhido C e conjunto de arestas residuais R. As arestas de R são precisamente as ainda não cobertas por C.

1. Inicialize a incumbente pela aproximação, garantindo cobertura válida mesmo antes da busca.
2. Se R estiver vazio, C é cobertura; atualize a incumbente se C for menor.
3. Obtenha LB pelo tamanho de um matching maximal guloso em R.
4. Se `|C| + LB >= |melhor|`, pode podar: nenhuma extensão estritamente melhor é possível.
5. Escolha `(u,v)` de R maximizando a soma dos graus residuais. Toda extensão válida contém u ou v.
6. Crie os dois estados filhos, removendo as arestas incidentes ao extremo escolhido. Explore primeiro o maior grau; desempates preservam a ordem de entrada e dos extremos normalizados.

### Corretude

O invariante das arestas residuais vale inicialmente e é preservado pela remoção das arestas incidentes. A ramificação é completa, pois toda cobertura precisa de um dos extremos da aresta selecionada; os ramos podem se sobrepor, mas nenhuma solução é perdida. A poda usa um limite inferior válido e não elimina solução melhor. A incumbente só é substituída por uma cobertura válida menor. Ao esgotar a pilha, não há cobertura melhor e o status é `OPTIMAL`.

O solver usa DFS com pilha explícita (`ArrayDeque`) e cópias de `BitSet`; não precisa desfazer mutações compartilhadas. Um vértice já escolhido não pode reaparecer em uma aresta residual, evitando seleção duplicada.

### Timeout e contadores

`System.nanoTime()` mede tempo transcorrido, inclusive o upper bound e os índices internos. O orçamento é verificado antes dos nós e periodicamente em laços de arestas/preparação. Se for interrompido com trabalho pendente, retorna `TIME_LIMIT`, a incumbente e contadores parciais; nenhuma promessa de otimalidade é feita, mesmo que a incumbente por acaso seja ótima.

`nodesExplored` conta estados retirados da pilha, inclusive folhas e estados podados. `prunedNodes` conta estados descartados por cardinalidade ou limite inferior; folhas que atualizam a incumbente não são podas. O ótimo trivial de grafo sem arestas é demonstrável sem explorar nós.

### Complexidade e representação

O algoritmo tem comportamento exponencial no pior caso: `O(2^|V| · poly(|V|, |E|))`. Uma avaliação de estado percorre vértices/arestas residuais e operações em bitsets. A DFS conserva apenas uma quantidade linear de estados pendentes na profundidade da busca, mas cada estado contém cópias de conjuntos; os bitsets de incidência também consomem memória. A representação base do grafo por lista de arestas e adjacência custa `O(|V| + |E|)`.

Ordenação por grau, qualidade da incumbente, matching residual e estrutura do grafo afetam fortemente o tempo. O projeto não usa regras especializadas para reconhecer completos/bipartidos nem uma fórmula fechada para substituir o algoritmo nos experimentos.

## Pesquisa e referências

- Karp, R. M. **Reducibility Among Combinatorial Problems**. Em *Complexity of Computer Computations*, 1972. Referência clássica para os problemas NP-completos, incluindo clique e cobertura por vértices.
- Garey, M. R.; Johnson, D. S. **Computers and Intractability: A Guide to the Theory of NP-Completeness**. W. H. Freeman, 1979.
- Cormen, T. H.; Leiserson, C. E.; Rivest, R. L.; Stein, C. **Introduction to Algorithms**. MIT Press. Discussão de NP-Completude e da aproximação de Vertex Cover por arestas com extremos livres.
- Williamson, D. P.; Shmoys, D. B. **The Design of Approximation Algorithms**. Cambridge University Press, 2011.

Outras abordagens estudadas conceitualmente incluem enumeração de todos os subconjuntos (usada somente como oráculo nos testes), programação inteira e relaxação linear com arredondamento. Não se introduz um resolvedor externo: os dois algoritmos experimentais são implementados integralmente em Java.
