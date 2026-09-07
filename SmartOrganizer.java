import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class SmartOrganizer {

    private static final Map<String, String> EXTENSION_MAP = new HashMap<>();

    static {
        EXTENSION_MAP.put("pdf",  "Documents");
        EXTENSION_MAP.put("doc",  "Documents");
        EXTENSION_MAP.put("docx", "Documents");
        EXTENSION_MAP.put("txt",  "Documents");
        EXTENSION_MAP.put("odt",  "Documents");
        EXTENSION_MAP.put("rtf",  "Documents");
        EXTENSION_MAP.put("xls",  "Documents");
        EXTENSION_MAP.put("xlsx", "Documents");
        EXTENSION_MAP.put("ppt",  "Documents");
        EXTENSION_MAP.put("pptx", "Documents");
        EXTENSION_MAP.put("csv",  "Documents");

        EXTENSION_MAP.put("jpg",  "Images");
        EXTENSION_MAP.put("jpeg", "Images");
        EXTENSION_MAP.put("png",  "Images");
        EXTENSION_MAP.put("gif",  "Images");
        EXTENSION_MAP.put("bmp",  "Images");
        EXTENSION_MAP.put("svg",  "Images");
        EXTENSION_MAP.put("webp", "Images");
        EXTENSION_MAP.put("ico",  "Images");
        EXTENSION_MAP.put("tiff", "Images");

        EXTENSION_MAP.put("mp4",  "Videos");
        EXTENSION_MAP.put("mkv",  "Videos");
        EXTENSION_MAP.put("avi",  "Videos");
        EXTENSION_MAP.put("mov",  "Videos");
        EXTENSION_MAP.put("wmv",  "Videos");
        EXTENSION_MAP.put("flv",  "Videos");
        EXTENSION_MAP.put("webm", "Videos");

        EXTENSION_MAP.put("mp3",  "Audio");
        EXTENSION_MAP.put("wav",  "Audio");
        EXTENSION_MAP.put("flac", "Audio");
        EXTENSION_MAP.put("aac",  "Audio");
        EXTENSION_MAP.put("ogg",  "Audio");
        EXTENSION_MAP.put("wma",  "Audio");

        EXTENSION_MAP.put("zip",  "Archives");
        EXTENSION_MAP.put("rar",  "Archives");
        EXTENSION_MAP.put("7z",   "Archives");
        EXTENSION_MAP.put("tar",  "Archives");
        EXTENSION_MAP.put("gz",   "Archives");
        EXTENSION_MAP.put("bz2",  "Archives");

        EXTENSION_MAP.put("java", "Code");
        EXTENSION_MAP.put("py",   "Code");
        EXTENSION_MAP.put("js",   "Code");
        EXTENSION_MAP.put("ts",   "Code");
        EXTENSION_MAP.put("c",    "Code");
        EXTENSION_MAP.put("cpp",  "Code");
        EXTENSION_MAP.put("h",    "Code");
        EXTENSION_MAP.put("cs",   "Code");
        EXTENSION_MAP.put("html", "Code");
        EXTENSION_MAP.put("css",  "Code");
        EXTENSION_MAP.put("xml",  "Code");
        EXTENSION_MAP.put("json", "Code");
        EXTENSION_MAP.put("sql",  "Code");
        EXTENSION_MAP.put("sh",   "Code");

        EXTENSION_MAP.put("exe",  "Executables");
        EXTENSION_MAP.put("msi",  "Executables");
        EXTENSION_MAP.put("dmg",  "Executables");
        EXTENSION_MAP.put("deb",  "Executables");
        EXTENSION_MAP.put("rpm",  "Executables");
        EXTENSION_MAP.put("apk",  "Executables");
        EXTENSION_MAP.put("jar",  "Executables");
    }

    private static final String DEFAULT_DIRECTORY = "./MessyFolder";

    public static void main(String[] args) {
        String dirArg = (args.length > 0) ? args[0] : DEFAULT_DIRECTORY;
        Path targetDir = Paths.get(dirArg).toAbsolutePath().normalize();

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║         📂  SmartOrganizer v1.0  📂         ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Target directory : " + targetDir);
        System.out.println();

        if (!Files.exists(targetDir)) {
            System.out.println("[ERROR] Directory does not exist: " + targetDir);
            System.out.println("        Create the directory and add some files, then re-run.");
            return;
        }
        if (!Files.isDirectory(targetDir)) {
            System.out.println("[ERROR] Path is not a directory: " + targetDir);
            return;
        }

        int movedCount   = 0;
        int skippedCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {

            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    continue;
                }

                String fileName  = entry.getFileName().toString();
                String extension = ripTail(fileName);

                if (extension.isEmpty()) {
                    System.out.println("  [SKIP]  " + fileName + "  (no extension)");
                    skippedCount++;
                    continue;
                }

                String category = EXTENSION_MAP.get(extension.toLowerCase());

                if (category == null) {
                    System.out.println("  [SKIP]  " + fileName
                            + "  (unknown extension: ." + extension + ")");
                    skippedCount++;
                    continue;
                }

                Path categoryDir = targetDir.resolve(category);
                if (!Files.exists(categoryDir)) {
                    Files.createDirectories(categoryDir);
                    System.out.println("  [NEW]   Created folder: " + category + "/");
                }

                Path destination = categoryDir.resolve(fileName);
                Files.move(entry, destination, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("  [MOVED] " + fileName + "  →  " + category + "/");
                movedCount++;
            }

        } catch (NoSuchFileException e) {
            System.out.println("[ERROR] File not found during processing: " + e.getMessage());
        } catch (AccessDeniedException e) {
            System.out.println("[ERROR] Access denied: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] I/O error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("──────────────────────────────────────────────");
        System.out.println("  Done!  Files moved: " + movedCount
                + "  |  Skipped: " + skippedCount);
        System.out.println("──────────────────────────────────────────────");
    }

    private static String ripTail(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
}
