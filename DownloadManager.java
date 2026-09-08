import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DownloadManager — Downloads a file from a URL with progress tracking.
 * Optionally supports multi-threaded chunked downloading for faster speeds.
 */
public class DownloadManager {

    public interface ProgressListener {
        void onProgress(long bytesDownloaded, long totalBytes);
    }

    private final String       urlString;
    private final Path         targetDirectory;
    private ProgressListener   listener;
    private volatile boolean   cancelled = false;
    private boolean            multiThreaded = false;
    private static final int   THREADS = 4;

    public DownloadManager(String urlString, Path targetDirectory) {
        this.urlString       = urlString;
        this.targetDirectory = targetDirectory;
    }

    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    public void setMultiThreaded(boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
    }

    /**
     * Signals the download loop to stop and clean up.
     */
    public void cancel() {
        this.cancelled = true;
    }

    private HttpURLConnection createConnection(String targetUrl) throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.setRequestProperty("User-Agent", "SmartOrganizerDownloader/2.0");
        return conn;
    }

    private String handleRedirects(String targetUrl) throws IOException {
        int redirects = 0;
        while (redirects < 5) {
            HttpURLConnection conn = createConnection(targetUrl);
            conn.setRequestMethod("HEAD");
            conn.setInstanceFollowRedirects(false);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == 307 || responseCode == 308) {
                String redirect = conn.getHeaderField("Location");
                conn.disconnect();
                if (redirect != null) {
                    targetUrl = redirect;
                    redirects++;
                    continue;
                }
            }
            conn.disconnect();
            break;
        }
        return targetUrl;
    }

    /**
     * Downloads the file and returns the local Path where it was saved.
     */
    public Path download() throws IOException {
        String finalUrl = handleRedirects(urlString);

        // Probe for info
        HttpURLConnection conn = createConnection(finalUrl);
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
            conn.disconnect();
            throw new IOException("Server returned HTTP " + responseCode + " for: " + finalUrl);
        }

        long totalBytes = conn.getContentLengthLong();
        String fileName = extractFileName(conn, finalUrl);
        boolean acceptRanges = conn.getHeaderField("Accept-Ranges") != null 
                               && conn.getHeaderField("Accept-Ranges").equalsIgnoreCase("bytes");
        conn.disconnect();

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

        if (multiThreaded && totalBytes > 0 && acceptRanges) {
            downloadMultiThreaded(finalUrl, outputPath, totalBytes);
        } else {
            downloadSingleThreaded(finalUrl, outputPath, totalBytes);
        }

        if (cancelled) {
            try { Files.deleteIfExists(outputPath); } catch (IOException ignored) {}
            throw new IOException("Download cancelled by user.");
        }

        return outputPath;
    }

    private void downloadSingleThreaded(String finalUrl, Path outputPath, long totalBytes) throws IOException {
        HttpURLConnection conn = createConnection(finalUrl);
        conn.setRequestMethod("GET");
        
        try (InputStream in = new BufferedInputStream(conn.getInputStream(), 128 * 1024);
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputPath), 128 * 1024)) {

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
        } catch (IOException e) {
            try { Files.deleteIfExists(outputPath); } catch (IOException ignored) {}
            throw e;
        } finally {
            conn.disconnect();
        }
    }

    private void downloadMultiThreaded(String finalUrl, Path outputPath, long totalBytes) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        AtomicLong downloadedBytes = new AtomicLong(0);

        try (RandomAccessFile raf = new RandomAccessFile(outputPath.toFile(), "rw")) {
            raf.setLength(totalBytes);
        }

        long chunkSize = totalBytes / THREADS;
        CountDownLatch latch = new CountDownLatch(THREADS);
        
        // Timer to periodically update listener instead of every byte read (reduces lock contention)
        ScheduledExecutorService progressTimer = Executors.newSingleThreadScheduledExecutor();
        progressTimer.scheduleAtFixedRate(() -> {
            if (listener != null && !cancelled) {
                listener.onProgress(downloadedBytes.get(), totalBytes);
            }
        }, 0, 100, java.util.concurrent.TimeUnit.MILLISECONDS);

        for (int i = 0; i < THREADS; i++) {
            final long startByte = i * chunkSize;
            final long endByte = (i == THREADS - 1) ? totalBytes - 1 : (startByte + chunkSize - 1);

            executor.execute(() -> {
                try {
                    HttpURLConnection conn = createConnection(finalUrl);
                    conn.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
                    conn.setRequestMethod("GET");

                    try (InputStream in = new BufferedInputStream(conn.getInputStream(), 64 * 1024);
                         RandomAccessFile raf = new RandomAccessFile(outputPath.toFile(), "rw")) {
                        
                        raf.seek(startByte);
                        byte[] buffer = new byte[64 * 1024];
                        int bytesRead;
                        while (!cancelled && (bytesRead = in.read(buffer)) != -1) {
                            raf.write(buffer, 0, bytesRead);
                            downloadedBytes.addAndGet(bytesRead);
                        }
                    } finally {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    cancelled = true; // Abort all threads on failure
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            cancelled = true;
            Thread.currentThread().interrupt();
        } finally {
            progressTimer.shutdownNow();
            executor.shutdownNow();
        }

        if (cancelled) {
            try { Files.deleteIfExists(outputPath); } catch (IOException ignored) {}
        }
    }

    private static String extractFileName(HttpURLConnection conn, String urlString) {
        String disposition = conn.getHeaderField("Content-Disposition");
        if (disposition != null && disposition.contains("filename")) {
            String[] parts = disposition.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith("filename=")) {
                    String name = trimmed.substring("filename=".length()).replace("\"", "").trim();
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

        try {
            String path = new URI(urlString).getPath();
            if (path != null && !path.isEmpty()) {
                String name = path.substring(path.lastIndexOf('/') + 1);
                if (name.contains("?")) name = name.substring(0, name.indexOf('?'));
                if (!name.isEmpty()) return sanitize(name);
            }
        } catch (Exception ignored) { }

        return "downloaded_file";
    }

    private static String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
