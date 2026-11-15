import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/** Somente os valores lidos do CSV alimentam os pontos e as contagens dos gráficos. */
public final class ChartGenerator {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private static final int LEFT = 115;
    private static final int RIGHT = 925;
    private static final int TOP = 110;
    private static final int BOTTOM = 640;
    private static final Color BLUE = new Color(30, 100, 190);
    private static final Color GREEN = new Color(0, 135, 95);
    private static final Color RED = new Color(205, 60, 55);
    private static final Color GRAY = new Color(100, 100, 100);

    private record Point(double x, double y) { }
    private record Series(String name, Color color, List<Point> points, boolean cross) { }
    private record Reference(String name, Color color, double slope, double intercept) { }

    private ChartGenerator() { }

    public static void generate(Path input, Path output) throws IOException {
        List<Map<String, String>> rows = CsvExporter.read(input);
        Files.createDirectories(output);
        for (Map<String, String> row : rows) {
            validate(row);
        }
        List<Point> timeExact = new ArrayList<>();
        List<Point> timeTimeout = new ArrayList<>();
        List<Point> timeApprox = new ArrayList<>();
        List<Point> densityExact = new ArrayList<>();
        List<Point> densityTimeout = new ArrayList<>();
        List<Point> densityApprox = new ArrayList<>();
        List<Point> sizes = new ArrayList<>();
        List<Point> ratios = new ArrayList<>();
        List<Point> nodesExact = new ArrayList<>();
        List<Point> nodesTimeout = new ArrayList<>();
        int optimal = 0;
        int timeouts = 0;
        int zeroTimes = 0;
        for (Map<String, String> row : rows) {
            boolean proven = Boolean.parseBoolean(row.get("exact_optimality_proven"));
            double n = number(row, "n");
            double density = number(row, "density");
            double exactMs = number(row, "exact_runtime_ns") / 1_000_000.0;
            double approxMs = number(row, "approx_runtime_ns") / 1_000_000.0;
            if (proven) {
                optimal++;
            } else {
                timeouts++;
            }
            if (exactMs > 0) {
                (proven ? timeExact : timeTimeout).add(new Point(n, exactMs));
                (proven ? densityExact : densityTimeout).add(new Point(density, exactMs));
            } else {
                zeroTimes++;
            }
            if (approxMs > 0) {
                timeApprox.add(new Point(n, approxMs));
                densityApprox.add(new Point(density, approxMs));
            } else {
                zeroTimes++;
            }
            if (proven) {
                sizes.add(new Point(number(row, "exact_optimal_size"), number(row, "approx_solution_size")));
                if (!missing(row.get("approximation_ratio"))) {
                    ratios.add(new Point(n, number(row, "approximation_ratio")));
                }
            }
            (proven ? nodesExact : nodesTimeout).add(new Point(n, number(row, "exact_nodes_explored") + 1));
        }
        String source = "Fonte: " + input.getFileName() + "; linhas/instâncias = " + rows.size()
                + ". Pontos podem se sobrepor; não há dados interpolados.";
        List<Series> timeSeries = List.of(new Series("Exato: OPTIMAL", BLUE, timeExact, false),
                new Series("Exato: TIME_LIMIT", RED, timeTimeout, true),
                new Series("Aproximação", GREEN, timeApprox, false));
        plot(output.resolve("01-runtime-vs-n.png"), "Tempo de execução versus número de vértices",
                "Número de vértices (n)", "Tempo (ms; escala log10)", timeSeries, List.of(), true,
                rows.size(), source, "Tempos <= 0 omitidos da escala log: " + zeroTimes
                        + "; TIME_LIMIT mede tempo até interrupção, não tempo para provar o ótimo.");
        plot(output.resolve("02-approx-vs-optimum.png"), "Cobertura aproximada versus cobertura ótima",
                "Tamanho ótimo comprovado (vértices)", "Tamanho aproximado (vértices)",
                List.of(new Series("Coberturas medidas", BLUE, sizes, false)),
                List.of(new Reference("Referência: y = x", GRAY, 1, 0),
                        new Reference("Limite teórico: 2x", RED, 2, 0)), false, sizes.size(), source,
                "Excluídas " + (rows.size() - sizes.size()) + " linhas sem prova de ótimo; ótimo zero incluído.");
        plot(output.resolve("03-ratio-vs-n.png"), "Razão de aproximação versus número de vértices",
                "Número de vértices (n)", "Razão |C_aprox| / OPT (adimensional)",
                List.of(new Series("Razão medida", BLUE, ratios, false)),
                List.of(new Reference("Garantia teórica: 2", RED, 0, 2)), false, ratios.size(), source,
                "Excluídas " + (rows.size() - ratios.size()) + " linhas: timeout, ótimo zero ou razão NA.");
        plot(output.resolve("04-runtime-vs-density.png"), "Tempo de execução por densidade observada",
                "Densidade 2m / [n(n-1)] (adimensional)", "Tempo (ms; escala log10)",
                List.of(new Series("Exato: OPTIMAL", BLUE, densityExact, false),
                        new Series("Exato: TIME_LIMIT", RED, densityTimeout, true),
                        new Series("Aproximação", GREEN, densityApprox, false)),
                List.of(), true, rows.size(), source,
                "Densidade observada, não parâmetro p; tempos <= 0 omitidos: " + zeroTimes + ".");
        plot(output.resolve("05-explored-nodes.png"), "Nós explorados pelo Branch and Bound",
                "Número de vértices (n)", "Nós explorados + 1 (escala log10)",
                List.of(new Series("OPTIMAL: medido", BLUE, nodesExact, false),
                        new Series("TIME_LIMIT: parcial", RED, nodesTimeout, true)),
                List.of(), true, rows.size(), source,
                "Soma-se 1 para representar zero na escala log; contagens de timeout são parciais.");
        statusChart(output.resolve("06-status.png"), optimal, timeouts, source);
        Files.writeString(output.resolve("README.md"), """
                # Figuras geradas do CSV real

                Fonte: `%s`; %d linhas. Não há valores experimentais preenchidos manualmente.
                Cada linha representa uma instância na execução selecionada (mediana superior se houver repetições).
                OPTIMAL e TIME_LIMIT distinguem prova de ótimo e resultado parcial.
                Os tempos parciais são mostrados como cruzes vermelhas e não estimam tempo de solução.
                Razão e comparação com ótimo usam exclusivamente ótimo comprovado; razão exclui ótimo zero.
                Referências teóricas tracejadas não são medições. Pontos sobrepostos não foram deslocados.
                O gráfico de nós usa nós + 1 para permitir escala logarítmica.
                Os seis PNGs têm 1280 x 800 pixels e foram renderizados exclusivamente com Java2D/ImageIO.
                """.formatted(input.toAbsolutePath(), rows.size()), java.nio.charset.StandardCharsets.UTF_8);
        ConsoleView.success("Seis gráficos PNG gerados a partir do CSV real.");
        ConsoleView.file("Abra as imagens nesta pasta", output);
    }

    private static void plot(Path path, String title, String xLabel, String yLabel,
                             List<Series> series, List<Reference> references, boolean logY,
                             int instances, String source, String note) throws IOException {
        BufferedImage image = canvas();
        Graphics2D graphics = graphics(image);
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        int count = 0;
        for (Series item : series) {
            for (Point point : item.points()) {
                minX = Math.min(minX, point.x());
                maxX = Math.max(maxX, point.x());
                minY = Math.min(minY, point.y());
                maxY = Math.max(maxY, point.y());
                count++;
            }
        }
        if (count == 0) {
            minX = 0;
            maxX = 1;
            minY = logY ? 0.1 : 0;
            maxY = 1;
        }
        if (minX == maxX) {
            minX = Math.max(0, minX - 1);
            maxX++;
        }
        for (Reference reference : references) {
            minY = Math.min(minY, Math.min(reference.slope() * minX + reference.intercept(),
                    reference.slope() * maxX + reference.intercept()));
            maxY = Math.max(maxY, Math.max(reference.slope() * minX + reference.intercept(),
                    reference.slope() * maxX + reference.intercept()));
        }
        if (logY) {
            minY = Math.floor(Math.log10(minY));
            maxY = Math.ceil(Math.log10(maxY));
        } else {
            minY = 0;
            maxY *= 1.1;
        }
        if (maxY <= minY) {
            maxY = minY + 1;
        }
        heading(graphics, title, "Instâncias consideradas: " + instances + "; pontos medidos: " + count);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        for (int tick = 0; tick <= 5; tick++) {
            double fraction = tick / 5.0;
            int x = LEFT + (int) ((RIGHT - LEFT) * fraction);
            int y = BOTTOM - (int) ((BOTTOM - TOP) * fraction);
            graphics.setColor(new Color(225, 230, 235));
            graphics.drawLine(x, TOP, x, BOTTOM);
            graphics.drawLine(LEFT, y, RIGHT, y);
            graphics.setColor(Color.DARK_GRAY);
            centered(graphics, format(minX + fraction * (maxX - minX)), x, BOTTOM + 25);
            double labelY = minY + fraction * (maxY - minY);
            String label = format(logY ? Math.pow(10, labelY) : labelY);
            graphics.drawString(label, LEFT - 12 - graphics.getFontMetrics().stringWidth(label), y + 5);
        }
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawRect(LEFT, TOP, RIGHT - LEFT, BOTTOM - TOP);
        centered(graphics, xLabel, (LEFT + RIGHT) / 2, BOTTOM + 65);
        graphics.rotate(-Math.PI / 2);
        centered(graphics, yLabel, -(TOP + BOTTOM) / 2, 27);
        graphics.rotate(Math.PI / 2);
        int legendY = TOP + 18;
        for (Series item : series) {
            graphics.setColor(item.color());
            marker(graphics, RIGHT + 28, legendY - 5, item.cross());
            graphics.drawString(item.name(), RIGHT + 44, legendY);
            legendY += 27;
        }
        for (Reference reference : references) {
            graphics.setColor(reference.color());
            graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] {7, 5}, 0));
            graphics.drawLine(RIGHT + 20, legendY - 5, RIGHT + 38, legendY - 5);
            graphics.drawString(reference.name(), RIGHT + 44, legendY);
            legendY += 27;
        }
        graphics.setClip(LEFT, TOP, RIGHT - LEFT + 1, BOTTOM - TOP + 1);
        for (Reference reference : references) {
            graphics.setColor(reference.color());
            graphics.drawLine(xPixel(minX, minX, maxX),
                    yPixel(reference.slope() * minX + reference.intercept(), minY, maxY, logY),
                    xPixel(maxX, minX, maxX),
                    yPixel(reference.slope() * maxX + reference.intercept(), minY, maxY, logY));
        }
        graphics.setStroke(new BasicStroke(1.6f));
        for (Series item : series) {
            graphics.setColor(new Color(item.color().getRed(), item.color().getGreen(),
                    item.color().getBlue(), 175));
            for (Point point : item.points()) {
                marker(graphics, xPixel(point.x(), minX, maxX), yPixel(point.y(), minY, maxY, logY),
                        item.cross());
            }
        }
        graphics.setClip(null);
        if (count == 0) {
            graphics.setColor(Color.DARK_GRAY);
            centered(graphics, "Sem dados elegíveis no CSV", (LEFT + RIGHT) / 2, (TOP + BOTTOM) / 2);
        }
        footer(graphics, source, note);
        graphics.dispose();
        save(image, path);
    }

    private static void statusChart(Path path, int optimal, int timeouts, String source) throws IOException {
        BufferedImage image = canvas();
        Graphics2D graphics = graphics(image);
        heading(graphics, "Instâncias com otimalidade comprovada e interrompidas",
                "Instâncias consideradas: " + (optimal + timeouts) + "; contagens medidas no CSV");
        int maximum = Math.max(1, Math.max(optimal, timeouts));
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(LEFT, BOTTOM, RIGHT, BOTTOM);
        graphics.drawLine(LEFT, TOP, LEFT, BOTTOM);
        for (int i = 0; i <= 5; i++) {
            int y = BOTTOM - (BOTTOM - TOP) * i / 5;
            graphics.setColor(new Color(225, 230, 235));
            graphics.drawLine(LEFT, y, RIGHT, y);
            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString(format(maximum * i / 5.0), LEFT - 65, y + 5);
        }
        int[] counts = {optimal, timeouts};
        String[] labels = {"OPTIMAL", "TIME_LIMIT"};
        Color[] colors = {BLUE, RED};
        for (int i = 0; i < counts.length; i++) {
            int height = (int) ((BOTTOM - TOP) * counts[i] / (double) maximum);
            int x = LEFT + 140 + i * 350;
            graphics.setColor(colors[i]);
            graphics.fillRect(x, BOTTOM - height, 170, height);
            graphics.setColor(Color.DARK_GRAY);
            centered(graphics, Integer.toString(counts[i]), x + 85, BOTTOM - height - 12);
            centered(graphics, labels[i], x + 85, BOTTOM + 30);
        }
        graphics.setColor(BLUE);
        graphics.fillRect(RIGHT + 20, TOP + 5, 12, 12);
        graphics.drawString("Otimalidade comprovada", RIGHT + 40, TOP + 16);
        graphics.setColor(RED);
        graphics.fillRect(RIGHT + 20, TOP + 35, 12, 12);
        graphics.drawString("Interrupção por timeout", RIGHT + 40, TOP + 46);
        graphics.setColor(Color.DARK_GRAY);
        centered(graphics, "Status da busca exata", (LEFT + RIGHT) / 2, BOTTOM + 65);
        graphics.rotate(-Math.PI / 2);
        centered(graphics, "Quantidade de instâncias", -(TOP + BOTTOM) / 2, 30);
        graphics.rotate(Math.PI / 2);
        footer(graphics, source, "Uma contagem por linha; se houver repetições, consulte também o CSV de todas as execuções.");
        graphics.dispose();
        save(image, path);
    }

    static void validate(Map<String, String> row) throws IOException {
        for (String key : BenchmarkRunner.HEADER.subList(0, 19)) {
            if (!row.containsKey(key)) {
                throw new IOException("Coluna ausente no CSV: " + key);
            }
        }
        String status = row.get("exact_status");
        if (!status.equals("OPTIMAL") && !status.equals("TIME_LIMIT")) {
            throw new IOException("Status exato desconhecido: " + status);
        }
        String proven = row.get("exact_optimality_proven");
        if (!proven.equals(status.equals("OPTIMAL") ? "true" : "false")) {
            throw new IOException("Status e prova de otimalidade inconsistentes.");
        }
        if (!row.get("approx_valid").equals("true")) {
            throw new IOException("O CSV contém aproximação inválida.");
        }
        for (String key : List.of("n", "m", "density", "exact_runtime_ns", "approx_runtime_ns",
                "exact_best_known_size", "approx_solution_size", "exact_nodes_explored", "exact_pruned_nodes")) {
            if (number(row, key) < 0) {
                throw new IOException("Valor negativo em " + key);
            }
        }
        if (status.equals("OPTIMAL")) {
            double optimum = number(row, "exact_optimal_size");
            if (optimum < 0 || optimum != number(row, "exact_best_known_size")) {
                throw new IOException("Ótimo inconsistente no CSV.");
            }
            if (optimum == 0 && (!missing(row.get("approximation_ratio")) || !missing(row.get("gap_percent"))
                    || number(row, "approx_solution_size") != 0)) {
                throw new IOException("Ótimo zero exige aproximação zero, razão/gap NA.");
            }
            if (optimum > 0 && !missing(row.get("approximation_ratio"))) {
                double ratio = number(row, "approximation_ratio");
                if (Math.abs(ratio - number(row, "approx_solution_size") / optimum) > 1e-9) {
                    throw new IOException("Razão inconsistente no CSV.");
                }
            }
        } else if (!missing(row.get("exact_optimal_size")) || !missing(row.get("approximation_ratio"))
                || !missing(row.get("gap_percent"))) {
            throw new IOException("TIME_LIMIT não pode preencher ótimo, razão ou gap.");
        }
    }

    private static double number(Map<String, String> row, String key) throws IOException {
        try {
            double value = Double.parseDouble(row.get(key));
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("não finito");
            }
            return value;
        } catch (NumberFormatException | NullPointerException error) {
            throw new IOException("Valor numérico inválido em " + key + ": " + row.get(key), error);
        }
    }

    private static boolean missing(String value) {
        return value == null || value.isBlank() || value.equals("NA") || value.equals("N/A");
    }

    private static BufferedImage canvas() {
        return new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        return graphics;
    }

    private static void heading(Graphics2D graphics, String title, String subtitle) {
        graphics.setColor(Color.DARK_GRAY);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        graphics.drawString(title, LEFT, 45);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        graphics.drawString(subtitle, LEFT, 77);
    }

    private static void footer(Graphics2D graphics, String source, String note) {
        graphics.setColor(Color.DARK_GRAY);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        graphics.drawString(source, LEFT, 745);
        graphics.drawString(note, LEFT, 770);
    }

    private static int xPixel(double value, double min, double max) {
        return LEFT + (int) Math.round((value - min) / (max - min) * (RIGHT - LEFT));
    }

    private static int yPixel(double value, double min, double max, boolean log) {
        return BOTTOM - (int) Math.round(((log ? Math.log10(value) : value) - min)
                / (max - min) * (BOTTOM - TOP));
    }

    private static void marker(Graphics2D graphics, int x, int y, boolean cross) {
        if (cross) {
            graphics.drawLine(x - 4, y - 4, x + 4, y + 4);
            graphics.drawLine(x - 4, y + 4, x + 4, y - 4);
        } else {
            graphics.fillOval(x - 3, y - 3, 7, 7);
        }
    }

    private static void centered(Graphics2D graphics, String text, int x, int y) {
        graphics.drawString(text, x - graphics.getFontMetrics().stringWidth(text) / 2, y);
    }

    private static String format(double value) {
        if (value != 0 && (Math.abs(value) < 0.001 || Math.abs(value) >= 100000)) {
            return String.format(Locale.ROOT, "%.1e", value);
        }
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("\\.?0+$", "");
    }

    private static void save(BufferedImage image, Path path) throws IOException {
        if (!ImageIO.write(image, "png", path.toFile())) {
            throw new IOException("Codificador PNG indisponível.");
        }
    }
}
