import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * DownloadManager — Downloads a file from a URL with progress tracking.
 *
 * Usage:
 *   DownloadManager dm = new DownloadManager(url, targetDir);
 *   dm.setProgressListener((downloaded, total) -> ...);
 *   Path result = dm.download();
 */
public class DownloadManager {

    public interface ProgressListener {
        void onProgress(long bytesDownloaded, long totalBytes);
    }

    private final String       urlString;
    private final Path         targetDirectory;
    private ProgressListener   listener;
    private volatile boolean   cancelled = false;

    public DownloadManager(String urlString, Path targetDirectory) {
        this.urlString       = urlString;
        this.targetDirectory = targetDirectory;
    }

    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    /**
     * Signals the download loop to stop and clean up.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Downloads the file and returns the local Path where it was saved.
     */
    public Path download() throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.setRequestProperty("User-Agent",
                "SmartOrganizerDownloader/1.0");
        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();

        // Handle redirects (301/302/307/308) manually if needed
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                || responseCode == 307 || responseCode == 308) {

            String redirect = conn.getHeaderField("Location");
            if (redirect != null) {
                conn.disconnect();
                conn = (HttpURLConnection) new URL(redirect).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15_000);
                conn.setReadTimeout(30_000);
                conn.setRequestProperty("User-Agent",
                        "SmartOrganizerDownloader/1.0");
                responseCode = conn.getResponseCode();
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            throw new IOException("Server returned HTTP " + responseCode
                    + " for: " + urlString);
        }

        long totalBytes = conn.getContentLengthLong();
        String fileName = extractFileName(conn, urlString);

        // Ensure unique file name
        Path outputPath = targetDirectory.resolve(fileName);
        int counter = 1;
        String baseName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String ext = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.')) : "";
        while (Files.exists(outputPath)) {
            outputPath = targetDirectory.resolve(baseName + " (" + counter + ")" + ext);
            counter++;
        }

        // Stream the download (optimized 128KB buffer for faster speeds)
        try (InputStream in = new BufferedInputStream(conn.getInputStream(), 128 * 1024);
             OutputStream out = new BufferedOutputStream(
                     Files.newOutputStream(outputPath), 128 * 1024)) {

            byte[] buffer = new byte[128 * 1024];
            long downloaded = 0;
            int bytesRead;

            while (!cancelled && (bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloaded += bytesRead;
                if (listener != null) {
                    listener.onProgress(downloaded, totalBytes);
                }
            }

            if (cancelled) {
                throw new IOException("Download cancelled by user.");
            }
            
        } catch (IOException e) {
            // Clean up partial file on failure or cancellation
            try { Files.deleteIfExists(outputPath); } catch (IOException ignored) {}
            throw e;
        } finally {
            conn.disconnect();
        }

        return outputPath;
    }

    /**
     * Extracts a sensible file name from Content-Disposition header or the URL path.
     */
    private static String extractFileName(HttpURLConnection conn, String urlString) {
        // 1) Try Content-Disposition header
        String disposition = conn.getHeaderField("Content-Disposition");
        if (disposition != null && disposition.contains("filename")) {
            // filename="report.pdf"  or  filename*=UTF-8''report.pdf
            String[] parts = disposition.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith("filename=")) {
                    String name = trimmed.substring("filename=".length())
                            .replace("\"", "").trim();
                    if (!name.isEmpty()) return sanitize(name);
                }
                if (trimmed.startsWith("filename*=")) {
                    String raw = trimmed.substring(trimmed.indexOf("'") + 1);
                    raw = raw.substring(raw.indexOf("'") + 1);
                    try {
                        String name = URLDecoder.decode(raw, "UTF-8");
                        if (!name.isEmpty()) return sanitize(name);
                    } catch (Exception ignored) { }
                }
            }
        }

        // 2) Fall back to the URL path
        try {
            String path = new URI(urlString).getPath();
            if (path != null && !path.isEmpty()) {
                String name = path.substring(path.lastIndexOf('/') + 1);
                // Strip query parameters if present
                if (name.contains("?")) name = name.substring(0, name.indexOf('?'));
                if (!name.isEmpty()) return sanitize(name);
            }
        } catch (URISyntaxException ignored) { }

        // 3) Last resort
        return "downloaded_file";
    }

    /**
     * Removes characters that are illegal in Windows file names.
     */
    private static String sanitize(String name) {
        return name.replaceAll("[<>:\"/\\\\|?*]", "_").trim();
    }
}
