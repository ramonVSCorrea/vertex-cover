# 📄 Relatório acadêmico — Vertex Cover

Projeto LaTeX **autocontido**, no **template oficial SBC v2017**, sobre Branch and Bound e matching maximal 2-aproximado em Java 17 puro.

**Autores:** Ian Pereira Pinto Bomfim · João Miguel de Abreu Constâncio · Ramon Vinícius Silva Corrêa  
**Instituição:** Pontifícia Universidade Católica de Minas Gerais (PUC Minas)  
**Disciplina:** Projeto e Análise de Algoritmos — Trabalho Prático 2  
**Professor:** Walisson Ferreira de Carvalho

## 🚀 Abrir e importar no Overleaf

1. Para ler o documento pronto, abra **`main.pdf`**.
2. No Overleaf, selecione **New Project → Upload Project**.
3. Envie **`relatorio-overleaf.zip`**, que fica nesta pasta.
4. Defina **`main.tex` da raiz** como documento principal.
5. Selecione o compilador **pdfLaTeX** e clique em **Recompile**.

O Overleaf executa o BibTeX automaticamente pelo fluxo de compilação. Não são necessários Java, shell escape, acesso à aplicação ou caminhos externos para gerar o PDF: tabelas, imagens, fontes das listagens e bibliografia já estão no ZIP.

A compilação foi verificada **localmente**, com pdfLaTeX/BibTeX do MiKTeX; o projeto também é compilado após extrair o ZIP em outra pasta. Não houve publicação nem compilação dentro de uma conta Overleaf.

## 🧾 Base experimental escolhida

O relatório analisa exclusivamente a execução original registrada em **15/11/2026, 12:21:50 UTC**, nos arquivos:

- `dados/benchmark.csv`: **230 instâncias**, uma linha por grafo.
- `dados/benchmark-runs.csv`: **690 medições por algoritmo**, três por instância.
- `dados/benchmark-environment.md`: ambiente e parâmetros daquela execução.
- `dados/instancias/`: **200 G(n,p) + 30 estruturadas**, com manifesto e sementes.
- `figures/`: os **seis PNGs originais** gerados pela aplicação.

O resumo tem **228 provas de ótimo e dois timeouts**; todas as repetições contêm **seis timeouts do exato**. Razão/gap usam somente as **223 instâncias com ótimo comprovado e positivo**. Não se misturam execuções rápidas, outros experimentos completos ou medições temporárias dos testes.

As tabelas foram calculadas em Java a partir dos CSVs copiados, com mediana superior e quartis explicitados no texto. Consulte `evidencias/auditoria-dados.txt`. Nenhum benchmark foi reexecutado para preencher ou substituir os tempos originais.

## 📁 Organização

```text
relatorio/
├── main.tex, main.pdf               Artigo e PDF compilado
├── references.bib                  Seis referências acadêmicas verificadas
├── sbc-template.sty, sbc.bst         Arquivos oficiais, sem edição
├── sections/                       As seis seções principais do artigo
├── tables/                         Tabelas derivadas dos CSVs reais
├── figures/                        Seis PNGs copiados sem alteração
├── appendices/                     Código-fonte e reprodutibilidade
├── codigo/
│   ├── src/                        Cópias literais de todos os fontes Java
│   └── tests/                      TestRunner.java completo
├── dados/                          CSVs, ambiente, grafos e manifesto
├── evidencias/                     Auditoria, testes, hashes e checklist
├── ferramentas/
│   ├── ReportStats.java            Pós-processamento do relatório
│   └── validar.ps1                 Verificações de integridade e LaTeX
├── template-original/              Template fornecido pelo usuário
├── compilar.cmd                    Compilação local simplificada
└── relatorio-overleaf.zip           Pacote autocontido para importar
```

`build/` e arquivos auxiliares locais não são necessários no Overleaf e não entram no ZIP.

## 🔧 Compilação local

Com uma distribuição LaTeX instalada, dê dois cliques em **`compilar.cmd`** ou execute, dentro de `relatorio`:

```powershell
.\compilar.cmd
```

Comandos equivalentes:

```text
pdflatex -interaction=nonstopmode -halt-on-error main.tex
bibtex main
pdflatex -interaction=nonstopmode -halt-on-error main.tex
pdflatex -interaction=nonstopmode -halt-on-error main.tex
```

Se uma compilação modificar a paginação das referências, execute pdfLaTeX mais uma vez. O atalho faz essa passagem adicional. O PDF inclui os anexos de código; não foi informado um limite de páginas para a entrega.

## ☕ Auditoria e regeneração das tabelas (opcional)

**Não é necessário executar estes comandos para compilar o LaTeX.** Eles servem para auditar os dados e reproduzir o pós-processamento, sem alterar a aplicação original.

```text
javac -encoding UTF-8 --release 17 -Xlint:all -d build/java codigo/src/*.java codigo/tests/*.java ferramentas/ReportStats.java
java "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp build/java ReportStats
java "-Dfile.encoding=UTF-8" "-Djava.awt.headless=true" -cp build/java TestRunner
```

As 16 categorias de testes passaram usando as cópias do código, com evidência em `evidencias/testes-java.txt`. A compilação dessa cópia não usa nem altera o `out/` da aplicação.

O utilitário confere contagens, sementes, grafos, tamanhos aproximados, seleções por mediana e campos das execuções individuais. Os CSVs não guardam os vértices escolhidos pelo exato: a auditoria dos CSVs não é uma reconstrução independente de suas coberturas. O benchmark original já validava as duas coberturas antes de exportar.

A auditoria é vinculada aos hashes do corpus original. Para analisar um benchmark novo, é necessário revisar também os números e a redação, não apenas substituir os CSVs e recompilar.

## 🏛️ Fidelidade ao template SBC

Origem: [SBC Conferences Template — updated sbc-template.sty v2017](https://www.overleaf.com/latex/templates/sbc-conferences-template/blbxwjwzdngr), da Sociedade Brasileira de Computação, também fornecido localmente pelo usuário. A página identifica a licença **CC BY 4.0** para o template.

As imagens de exemplo em `template-original/` pertencem somente à cópia do template; não entram no artigo nem são apresentadas como resultados.

- `sbc-template.sty` e `sbc.bst` foram copiados **byte a byte**, sem mudar margens, fontes, título, legendas ou bibliografia.
- Documento A4, coluna única, texto principal em Times 12 pt, margens SBC e páginas sem numeração.
- Abstract e resumo estão na primeira página e têm menos de dez linhas cada.
- O idioma `brazilian` é o nome atual da opção de português brasileiro do Babel, evitando o alias obsoleto `brazil`.
- O `main.tex` contém **um token neutro de compatibilidade em um hook**, porque o estilo v2017 omite o argumento falso de uma verificação de `hyperref`. Isso evita que versões atuais consumam um comando do Babel; **não altera o estilo oficial nem sua aparência**.
- As iniciais intermediárias dos autores mantêm todos na mesma linha. Os nomes completos estão nos metadados do PDF e neste README.
- Não foram inventados e-mails, campus, endereço postal ou links de GitHub/Overleaf do grupo. Adicione esses dados somente se forem fornecidos e confirmados.

## ✅ Rastreabilidade e conferência

```powershell
powershell -NoProfile -File ferramentas/validar.ps1
```

Veja:

- `evidencias/checklist.md`: correspondência entre requisitos e seções.
- `evidencias/referencias-verificadas.md`: fontes das referências e decisões bibliográficas.
- `evidencias/sha256-origens.csv`: hashes das cópias de código, dados e template.
- `evidencias/preservacao-originais.txt`: conferência dos arquivos existentes da aplicação.
- `evidencias/compilacao.txt`: resultado da compilação final e observações do ambiente local.

Os testes e os cálculos do relatório são separados da coleta original. A implementação Java e os resultados existentes fora de `relatorio/` foram preservados.
