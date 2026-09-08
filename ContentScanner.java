import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

/**
 * Extracts text content from files (PDF, DOCX, TXT, PPTX, XLSX, etc.)
 * using Apache Tika, then matches the text against user-defined keyword rules.
 */
public class ContentScanner {

    // File extensions we know how to extract text from
    private static final java.util.Set<String> SCANNABLE_EXTENSIONS = java.util.Set.of(
            "pdf", "doc", "docx", "txt", "odt", "rtf",
            "ppt", "pptx", "xls", "xlsx", "csv",
            "html", "xml", "json"
    );

    /**
     * Checks whether a file extension is something we can extract text from.
     */
    public static boolean isScannable(String extension) {
        return SCANNABLE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    /**
     * Extracts text content from a file using Apache Tika.
     * Falls back to plain-text reading for .txt/.csv/.json/.xml files.
     *
     * @param file the path to the file
     * @return the extracted text content (may be empty, never null)
     */
    public static String extractText(Path file) {
        String ext = getExtension(file).toLowerCase(Locale.ROOT);

        // For plain-text formats, just read the file directly (faster)
        if (ext.equals("txt") || ext.equals("csv") || ext.equals("json")
                || ext.equals("xml") || ext.equals("html")) {
            return readPlainText(file);
        }

        // For binary document formats, use Apache Tika
        return extractWithTika(file);
    }

    /**
     * Matches extracted text against a list of content rules.
     * Returns the folder name of the first matching rule, or null if none match.
     *
     * A rule matches if ANY of its keywords appear in the text (case-insensitive).
     *
     * @param text  the extracted text content
     * @param rules the list of content rules to check
     * @return the folder name of the matched rule, or null
     */
    public static String matchRule(String text, List<ContentRule> rules) {
        if (text == null || text.isEmpty() || rules == null || rules.isEmpty()) {
            return null;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        for (ContentRule rule : rules) {
            String[] keywords = rule.keywords.split(",");
            for (String kw : keywords) {
                String trimmed = kw.trim().toLowerCase(Locale.ROOT);
                if (!trimmed.isEmpty() && lowerText.contains(trimmed)) {
                    return rule.folderName;
                }
            }
        }
        return null;
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private static String readPlainText(Path file) {
        try {
            // Read up to 50KB to avoid loading huge files into memory
            byte[] bytes = Files.readAllBytes(file);
            int limit = Math.min(bytes.length, 50 * 1024);
            return new String(bytes, 0, limit, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[ContentScanner] Failed to read plain text: " + e.getMessage());
            return "";
        }
    }

    private static String extractWithTika(Path file) {
        try {
            // Use Tika's AutoDetectParser through the facade
            org.apache.tika.Tika tika = new org.apache.tika.Tika();
            // Limit extraction to 100KB of text
            tika.setMaxStringLength(100 * 1024);
            return tika.parseToString(file.toFile());
        } catch (Exception e) {
            System.err.println("[ContentScanner] Tika extraction failed for "
                    + file.getFileName() + ": " + e.getMessage());
            return "";
        }
    }

    private static String getExtension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1);
    }

    // ── Data model ────────────────────────────────────────────────────

    /**
     * Represents a user-defined content-matching rule.
     * A rule maps a comma-separated list of keywords to a target folder.
     */
    public static class ContentRule {
        public final int    id;
        public final String folderName;
        public final String keywords;

        public ContentRule(int id, String folderName, String keywords) {
            this.id         = id;
            this.folderName = folderName;
            this.keywords   = keywords;
        }

        @Override
        public String toString() {
            return folderName + " → [" + keywords + "]";
        }
    }
}
