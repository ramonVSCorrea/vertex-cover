# 🚀 Comece por aqui

## 1. Abra o programa

No Windows, abra a pasta **vertex-cover** e dê **dois cliques em `executar.cmd`**.

Ele compila os arquivos Java e abre o menu. Você não precisa compilar manualmente.
O único requisito é o **JDK 17** instalado.

Se preferir o PowerShell, estando em `Projetos`, execute:

```powershell
.\vertex-cover\executar.cmd
```

Se você já estiver dentro de `vertex-cover`, use:

```powershell
.\executar.cmd
```

> O script entra automaticamente na pasta correta, mesmo quando é chamado de outro diretório.

## 2. Escolha a opção 1

A **demonstração** explica o problema e mostra uma tabela com os dois algoritmos.

Imagine pontos ligados por linhas. Você quer escolher o menor grupo de pontos
para que **toda linha tenha pelo menos uma ponta escolhida**.

- **Exato:** procura o mínimo e tenta provar que não existe solução menor.
- **Aproximação:** encontra rapidamente uma solução válida, de até duas vezes o mínimo.
- **Timeout:** o exato parou pelo tempo. A solução é válida, mas não tem prova de mínimo.

Na demonstração, o exato encontra **3 vértices** e a aproximação usa **4**.
As duas soluções cobrem todas as arestas.

## 3. Escolha a opção 4

O **experimento rápido** executa os algoritmos em **8 grafos pequenos** e faz tudo:

```text
Gerar grafos → medir algoritmos → salvar CSVs → criar gráficos → escrever relatório
```

Ao terminar, o programa mostra a pasta criada em:

```text
data/results/execucoes/rapido-DATA-HORA/
```

Comece abrindo:

1. **`relatorio.md`**: tabelas e resumo dos resultados.
2. **`figures/`**: seis imagens PNG com as comparações.
3. **`benchmark.csv`**: dados para consulta em uma planilha, se quiser.

Cada execução cria uma pasta nova; **os resultados anteriores não são apagados**.
`CONCLUIDO.txt` aparece somente quando o fluxo termina com sucesso.

## O que fazem as outras opções?

| Opção | Quando usar |
| --- | --- |
| **2 — Resolver arquivo** | Comparar os dois algoritmos em um grafo seu; Enter usa o exemplo |
| **3 — Testes** | Verificar se o código está correto; não é o experimento de desempenho |
| **5 — Experimento completo** | Medir as **230 instâncias** do trabalho; pode levar vários minutos |
| **6 — Analisar CSV** | Criar novos gráficos e relatório a partir de dados já existentes |
| **7 — Entender o projeto** | Rever a explicação e o significado dos arquivos |
| **0 — Sair** | Encerrar o programa |

> O modo rápido é didático e **não substitui o experimento completo** do trabalho.
> Tempos variam entre execuções; o timeout não deve ser confundido com ótimo.

Para aprofundar, consulte o [README](README.md) e a [metodologia](docs/experimental-methodology.md).
