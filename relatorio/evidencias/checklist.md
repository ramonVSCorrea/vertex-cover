# Conferência do planejamento e dos requisitos nele transcritos

## Seções obrigatórias

| Requisito | Localização | Conferido |
|---|---|---|
| Introdução, contexto, objetivo geral/específicos e organização | Seção 1 | Sim |
| Fundamentação teórica | Seção 2 | Sim |
| Metodologia fiel ao código | Seção 3 | Sim |
| Resultados reais | Seção 4 | Sim |
| Análise dos resultados | Seção 5 | Sim |
| Conclusão, limitações e trabalhos futuros | Seção 6 | Sim |
| Referências Bibliográficas | Bibliografia SBC após a conclusão, seis obras citadas | Sim |
| Anexos com código-fonte | Anexo A; cópias completas em `codigo/` | Sim |

A bibliografia permanece sem numeração de seção, conforme o mecanismo oficial do template. O PDF tem **36 páginas**: **20 até a bibliografia** e **16 de anexos**. Não foi informado limite de páginas.

## Conteúdo técnico e experimental

- [x] Grafo simples não direcionado, vértice, aresta, grau, completo, bipartido, matching, maximalidade e cobertura.
- [x] Definição formal, otimização, decisão e distinção entre válido, mínimo/minimal e melhor conhecido.
- [x] P, NP, NP-difícil, NP-completo e redução polinomial; P=NP não tratado como problema resolvido.
- [x] Pertencimento de Vertex Cover a NP e redução **Independent Set → Vertex Cover**, com `k'=|V|-k` e os dois sentidos da equivalência.
- [x] Três modelagens de aplicação, com correspondência entre elementos e ressalvas operacionais.
- [x] Pesquisa de força bruta, ramificação/Branch and Bound, matching, gulosa por grau, programação inteira/relaxação e caso bipartido.
- [x] Java 17 puro, pacote padrão, somente JDK, `javac`/`java`, sem gerenciadores/frameworks.
- [x] Ambiente efetivamente registrado: CPU, memória, SO, JDK, heap e argumentos.
- [x] Lista de arestas e adjacência, `BitSet`, normalização, duplicatas, índices base um/zero e densidade.
- [x] Incumbente inicial aproximada, matching residual, poda válida, graus, pilha LIFO, cópias, nós, podas e timeout cooperativo.
- [x] Dois pseudocódigos compatíveis com o Java; justificativa de corretude e complexidades.
- [x] Prova de fator 2 e distinção entre matching maximal e máximo.
- [x] **200 grafos aleatórios e 30 estruturados**, sementes **20261115 a 20261314**, 230 arquivos identificados no manifesto.
- [x] Aquecimento separado, três medições por algoritmo/instância, alternância e seleção por mediana.
- [x] Tempos, cardinalidades, nós, podas, status, validade, razão e gap.
- [x] Quartis explicitados por posto e mediana superior; dispersão entre instâncias, não intervalos de confiança.
- [x] **228 ótimos / dois timeouts no resumo**, **seis timeouts em 690 medições do exato**.
- [x] **223 razões definidas**, cinco ótimos zero, `NA` para ótimo não comprovado e para razão/gap com ótimo zero.
- [x] Comparação de qualidade, tempo, tamanho, densidade, escalabilidade, vantagens e limitações.
- [x] Observação, explicação possível e limitação separadas na análise; sem superioridade universal.
- [x] Seis figuras e sete tabelas numeradas, legendadas e discutidas.
- [x] Sete equações numeradas e referenciadas no texto.
- [x] Testes independentes: 1.110 grafos contra força bruta; 16 grupos atuais aprovados, com log.

## Fontes e integridade

- [x] Planejamento integral lido e código/CSVs/figuras/documentação inspecionados antes da redação dos resultados.
- [x] Corpus explicitamente escolhido: execução original de 15/11/2026 às 12:21:50 UTC; outros diretórios de execução não foram misturados.
- [x] 230 linhas-resumo e 690 individuais auditadas; medianas e campos selecionados conferidos.
- [x] Grafos aleatórios regenerados das sementes e aproximação reproduzida para conferir cardinalidades.
- [x] Flags de validade auditadas; reconhecida a impossibilidade de reconstruir as soluções exatas somente das cardinalidades do CSV.
- [x] Fontes da aplicação preservados; os 17 arquivos Java do pacote correspondem aos existentes.
- [x] Oito arquivos essenciais completos no PDF; trecho dos testes identificado como trecho; todos os fontes e testes completos no ZIP.
- [x] Referências verificadas em fontes acadêmicas/editoriais; nenhum DOI/paginação inventado.
- [x] `.sty` e `.bst` oficiais copiados literalmente e conferidos por SHA-256.
- [x] Conteúdo original de `src/`, `tests/`, `docs/` e `data/` preservado (551 arquivos conferidos).

## Compilação e apresentação

- [x] pdfLaTeX e BibTeX concluíram a compilação.
- [x] A4, coluna única, fonte principal/margens/legendas/bibliografia do SBC e páginas sem numeração.
- [x] Abstract em inglês e resumo em português na primeira página: **oito linhas cada**.
- [x] Referências cruzadas, citações, arquivos locais e rótulos conferidos.
- [x] Sem `Overfull`, caracteres ausentes, erros fatais, referências/citações indefinidas ou rótulos duplicados no log final.
- [x] Revisão visual de páginas com título/resumos, algoritmos, tabelas, gráficos, bibliografia, listagens e reprodução.
- [x] ZIP montado com caminhos relativos e todas as dependências locais; a conferência de extração/compilação é registrada em `compilacao.txt`.

## Ressalvas

- Permanecem avisos tipográficos `Underfull \vbox`, associados à distribuição vertical de páginas com quebras e listagens, sem texto cortado ou fora das margens. O `flushbottom` do template foi preservado.
- O MiKTeX local emite um aviso administrativo de atualizações ainda não verificadas. A distribuição global não foi alterada para produzir o relatório.
- A compilação é validada localmente e a partir do ZIP extraído; não se afirma ter publicado/compilado numa conta Overleaf.
- Dados pessoais não fornecidos (e-mails, campus/endereço) e links de submissão do grupo não foram inventados.
