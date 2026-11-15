import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** CSV UTF-8, vírgula e ponto decimal; suporta campos entre aspas e aspas escapadas. */
public final class CsvExporter {
    private CsvExporter() { }

    public static void write(Path path, List<String> header, List<? extends List<?>> rows) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writeRow(writer, header);
            for (List<?> row : rows) {
                if (row.size() != header.size()) {
                    throw new IOException("Número incorreto de colunas no CSV.");
                }
                writeRow(writer, row);
            }
        }
    }

    private static void writeRow(BufferedWriter writer, List<?> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                writer.write(',');
            }
            String value = values.get(i) == null ? "NA" : values.get(i).toString();
            if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
                writer.write("\"" + value.replace("\"", "\"\"") + "\"");
            } else {
                writer.write(value);
            }
        }
        writer.newLine();
    }

    public static List<Map<String, String>> read(Path path) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (PushbackReader reader = new PushbackReader(Files.newBufferedReader(path, StandardCharsets.UTF_8), 1)) {
            List<String> record = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            boolean closedQuote = false;
            int ch;
            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                if (quoted) {
                    if (c == '"') {
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            quoted = false;
                            closedQuote = true;
                            if (next != -1) {
                                reader.unread(next);
                            }
                        }
                    } else {
                        field.append(c);
                    }
                } else if (c == ',' || c == '\n' || c == '\r') {
                    record.add(field.toString());
                    field.setLength(0);
                    closedQuote = false;
                    if (c != ',') {
                        records.add(List.copyOf(record));
                        record.clear();
                        if (c == '\r') {
                            int next = reader.read();
                            if (next != '\n' && next != -1) {
                                reader.unread(next);
                            }
                        }
                    }
                } else if (c == '"' && field.length() == 0 && !closedQuote) {
                    quoted = true;
                } else {
                    if (closedQuote || c == '"') {
                        throw new IOException("Aspas ou caracteres inesperados em CSV: " + path);
                    }
                    field.append(c);
                }
            }
            if (quoted) {
                throw new IOException("Campo CSV com aspas não fechadas: " + path);
            }
            if (!record.isEmpty() || field.length() != 0 || closedQuote) {
                record.add(field.toString());
                records.add(List.copyOf(record));
            }
        }
        if (records.isEmpty()) {
            throw new IOException("CSV sem cabeçalho: " + path);
        }
        List<String> header = new ArrayList<>(records.get(0));
        if (header.get(0).startsWith("\uFEFF")) {
            header.set(0, header.get(0).substring(1));
        }
        if (header.stream().anyMatch(String::isBlank) || new HashSet<>(header).size() != header.size()) {
            throw new IOException("Cabeçalho CSV vazio ou duplicado.");
        }
        List<Map<String, String>> rows = new ArrayList<>();
        for (int index = 1; index < records.size(); index++) {
            List<String> record = records.get(index);
            if (record.size() != header.size()) {
                throw new IOException("Linha CSV " + (index + 1) + " com número incorreto de colunas.");
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int column = 0; column < header.size(); column++) {
                row.put(header.get(column), record.get(column));
            }
            rows.add(row);
        }
        return rows;
    }
}
