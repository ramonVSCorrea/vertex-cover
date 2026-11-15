import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Menu numérico. O leitor é injetável para permitir testes sem teclado nem interface gráfica. */
public final class InteractiveMenu {
    private final BufferedReader reader;
    private final Path root;

    public InteractiveMenu(BufferedReader reader, Path root) {
        this.reader = reader;
        this.root = root.toAbsolutePath().normalize();
    }

    public void run() throws IOException {
        while (true) {
            ConsoleView.section("TP 2 - PAA | COBERTURA POR VÉRTICES", "Escolha pelo número. Para começar, use a opção 1.");
            System.out.println("""
                      1  Ver uma demonstração explicada                    [COMECE AQUI]
                      2  Resolver um arquivo com os dois algoritmos
                      3  Executar os testes de corretude
                      4  Experimento rápido: dados + gráficos + relatório   [8 grafos]
                      5  Experimento completo do trabalho                   [230 grafos]
                      6  Gerar gráficos e relatório de um CSV existente
                      7  Entender o projeto e os arquivos de saída
                      0  Sair
                    """);
            String choice = ask("Sua escolha", null);
            if (choice == null || choice.equals("0")) {
                ConsoleView.success("Aplicação encerrada. Seus arquivos foram preservados.");
                return;
            }
            try {
                switch (choice) {
                    case "1" -> Main.demo();
                    case "2" -> solveFile();
                    case "3" -> Main.runTests();
                    case "4" -> experiment(false);
                    case "5" -> experiment(true);
                    case "6" -> analyzeCsv();
                    case "7" -> explainProject();
                    default -> {
                        ConsoleView.warning("Opção inválida. Digite um número de 0 a 7.");
                        continue;
                    }
                }
            } catch (IllegalArgumentException | IOException | IllegalStateException error) {
                ConsoleView.error(error.getMessage());
                ConsoleView.info("Confira o caminho e os dados informados. Você pode tentar novamente.");
            }
            if (ask("Pressione Enter para voltar ao menu", null) == null) {
                return;
            }
        }
    }

    private void solveFile() throws IOException {
        ConsoleView.section("RESOLVER UM ARQUIVO", "Enter usa o exemplo incluído; você também pode colar um caminho completo.");
        String input = ask("Arquivo", "data/instances/exemplo.txt");
        if (input == null) {
            return;
        }
        Long timeout = askTimeout(5000);
        if (timeout == null) {
            return;
        }
        Path file = resolve(input);
        if (!Files.isRegularFile(file)) {
            throw new IOException("Arquivo de grafo não encontrado: " + file);
        }
        Main.compare(GraphFileReader.read(file), file.toString(), timeout);
    }

    private void experiment(boolean full) throws IOException {
        ConsoleView.section(full ? "EXPERIMENTO COMPLETO" : "EXPERIMENTO RÁPIDO",
                full ? "230 instâncias; três medições por algoritmo e timeout de 5 segundos por medição."
                        : "8 grafos pequenos; três medições. Ideal para conhecer o fluxo sem uma espera longa.");
        if (full) {
            ConsoleView.warning("Pode levar vários minutos. As três medições de uma instância podem levar até ~15 s.");
            ConsoleView.info("No pior cenário de 230 grafos com timeout, só o exato pode levar cerca de 58 minutos.");
            if (!confirmed("Iniciar o experimento completo? [s/N]")) {
                ConsoleView.info("Experimento cancelado. Nenhum arquivo foi alterado.");
                return;
            }
        }
        List<GraphGenerator.Instance> instances = full ? GraphGenerator.defaultInstances(GraphGenerator.DEFAULT_SEED)
                : ExperimentWorkflow.quickInstances();
        Path output = ExperimentWorkflow.newOutputPath(root.resolve("data/results/execucoes"), full ? "completo" : "rapido");
        ExperimentWorkflow.run(instances, output, full ? 5000 : 1000, 3, full ? 100 : 20,
                full ? "Completo (230 grafos padrão)" : "Rápido e didático (8 grafos)");
    }

    private void analyzeCsv() throws IOException {
        ConsoleView.section("ANALISAR UM CSV", "Os arquivos existentes serão apenas lidos; as novas saídas ficam em outra pasta.");
        String input = ask("CSV", "data/results/benchmark.csv");
        if (input == null) {
            return;
        }
        Path csv = resolve(input);
        if (!Files.isRegularFile(csv)) {
            throw new IOException("CSV não encontrado: " + csv);
        }
        Path output = ExperimentWorkflow.newOutputPath(root.resolve("data/results/execucoes"), "analise");
        Files.createDirectories(output.getParent());
        Files.createDirectory(output);
        ChartGenerator.generate(csv, output.resolve("figures"));
        ExperimentReport.generate(csv, output.resolve("relatorio.md"));
        ConsoleView.success("Análise concluída sem modificar o CSV original.");
        ConsoleView.file("Relatório", output.resolve("relatorio.md"));
        ConsoleView.file("Gráficos", output.resolve("figures"));
    }

    static void explainProject() {
        ConsoleView.section("ENTENDA O PROJETO", "Pense em pontos (vértices) ligados por linhas (arestas).");
        System.out.println("""
                      OBJETIVO
                      Escolher o menor grupo de pontos que encoste em todas as linhas.
                      Uma linha já está coberta quando pelo menos uma ponta foi escolhida.

                      DOIS CAMINHOS PARA RESOLVER
                      Exato       : procura o menor grupo e tenta provar que ele é mínimo.
                      Aproximação : encontra rapidamente um grupo válido, de até 2 vezes o ótimo.
                      Timeout     : o exato parou pelo tempo; sua solução NÃO é um ótimo comprovado.

                      O QUE CADA OPÇÃO FAZ
                      Demonstração : explica um grafo pequeno e compara as duas soluções.
                      Testes       : verifica se as implementações estão corretas.
                      Experimento  : mede os algoritmos em vários grafos e salva os resultados.

                      POR ONDE COMEÇAR NOS ARQUIVOS
                      relatorio.md : tabelas legíveis e resumo do experimento.
                      figures/     : imagens PNG; abra para ver as comparações.
                      benchmark.csv: dados resumidos, uma linha por grafo.
                      *-runs.csv   : todas as medições, inclusive interrupções.
                      NA           : desconhecido ou não aplicável; não significa zero.

                      SUGESTÃO
                      Primeiro opção 1; depois opção 4. A opção 5 é o experimento mais demorado.
                    """);
    }

    private Long askTimeout(long defaultValue) throws IOException {
        while (true) {
            String value = ask("Tempo limite do exato em ms (0 interrompe sem buscar)", "" + defaultValue);
            if (value == null) {
                return null;
            }
            try {
                long timeout = Long.parseLong(value);
                if (timeout >= 0 && timeout <= Long.MAX_VALUE / 1_000_000L) {
                    return timeout;
                }
            } catch (NumberFormatException ignored) {
                // A mensagem abaixo trata tanto formato inválido como valor fora do intervalo.
            }
            ConsoleView.warning("Informe um número inteiro não negativo; por exemplo, 5000.");
        }
    }

    private boolean confirmed(String message) throws IOException {
        String answer = ask(message, null);
        return answer != null && (answer.equalsIgnoreCase("s") || answer.equalsIgnoreCase("sim"));
    }

    private String ask(String message, String defaultValue) throws IOException {
        System.out.print("  " + message + (defaultValue == null ? "" : " [" + defaultValue + "]") + ": ");
        System.out.flush();
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        line = line.strip();
        return line.isEmpty() && defaultValue != null ? defaultValue : line;
    }

    private Path resolve(String input) {
        String text = input.strip();
        if (text.length() >= 2 && ((text.startsWith("\"") && text.endsWith("\""))
                || (text.startsWith("'") && text.endsWith("'")))) {
            text = text.substring(1, text.length() - 1);
        }
        Path path = Path.of(text);
        return path.isAbsolute() ? path : root.resolve(path);
    }
}
