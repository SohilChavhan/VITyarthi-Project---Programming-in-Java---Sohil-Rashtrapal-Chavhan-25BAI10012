import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

public class GuiOrganizer extends JFrame {

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

    private static final Color COLOR_PRIMARY    = new Color(59, 130, 246);
    private static final Color COLOR_PRIMARY_HV = new Color(37, 99, 235);
    private static final Color COLOR_BG_DARK    = new Color(30, 32, 40);
    private static final Color COLOR_BG_PANEL   = new Color(39, 42, 55);
    private static final Color COLOR_BG_INPUT   = new Color(49, 53, 68);
    private static final Color COLOR_TEXT       = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_DIM   = new Color(148, 163, 184);
    private static final Color COLOR_SUCCESS    = new Color(34, 197, 94);
    private static final Color COLOR_WARNING    = new Color(250, 204, 21);
    private static final Color COLOR_ERROR      = new Color(239, 68, 68);
    private static final Color COLOR_INFO       = new Color(96, 165, 250);
    private static final Color COLOR_BORDER     = new Color(55, 65, 81);

    private JTextField pathField;
    private JTextPane  logPane;
    private StyledDocument logDocument;
    private JButton    selectButton;
    private JButton    organizeButton;
    private JButton    undoButton;
    private JLabel     statusLabel;
    private JProgressBar progressBar;
    private Path       selectedDirectory;

    private final List<Path[]> moveHistory = new ArrayList<>();
    private final Set<Path> createdFolders = new LinkedHashSet<>();

    public GuiOrganizer() {
        super("Smart Directory Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 620);
        setMinimumSize(new Dimension(600, 480));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_BG_DARK);
        setContentPane(root);

        root.add(spawnTop(),  BorderLayout.NORTH);
        root.add(spawnMid(),  BorderLayout.CENTER);
        root.add(spawnBot(),  BorderLayout.SOUTH);
    }

    private JPanel spawnTop() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(COLOR_BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(18, 24, 14, 24)
        ));

        JLabel title = new JLabel("\u2728  Smart Directory Organizer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Automatically categorize messy files into organized sub-folders.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(COLOR_TEXT_DIM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        header.add(title);
        header.add(subtitle);
        return header;
    }

    private JPanel spawnMid() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(COLOR_BG_DARK);
        center.setBorder(new EmptyBorder(16, 24, 8, 24));

        center.add(makePickerBar(), BorderLayout.NORTH);
        center.add(makeLogBox(),    BorderLayout.CENTER);
        return center;
    }

    private JPanel makePickerBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(COLOR_BG_DARK);

        pathField = new JTextField("No folder selected");
        pathField.setEditable(false);
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pathField.setForeground(COLOR_TEXT_DIM);
        pathField.setBackground(COLOR_BG_INPUT);
        pathField.setCaretColor(COLOR_TEXT);
        pathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        selectButton = makeBtn("\uD83D\uDCC2  Select Folder", COLOR_PRIMARY, COLOR_PRIMARY_HV);
        selectButton.addActionListener(e -> grabFolder());

        organizeButton = makeBtn("\u26A1  Organize Now", new Color(22, 163, 74), new Color(21, 128, 61));
        organizeButton.setEnabled(false);
        organizeButton.addActionListener(e -> smashDo());

        undoButton = makeBtn("\u21A9  Undo", new Color(220, 120, 30), new Color(190, 100, 20));
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> rollbackUnDo());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(selectButton);
        btnPanel.add(organizeButton);
        btnPanel.add(undoButton);

        panel.add(pathField,  BorderLayout.CENTER);
        panel.add(btnPanel,   BorderLayout.EAST);
        return panel;
    }

    private JPanel makeLogBox() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG_DARK);

        JLabel logLabel = new JLabel("Activity Log");
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logLabel.setForeground(COLOR_TEXT_DIM);
        logLabel.setBorder(new EmptyBorder(0, 2, 6, 0));
        wrapper.add(logLabel, BorderLayout.NORTH);

        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setFont(new Font("Consolas", Font.PLAIN, 12));
        logPane.setBackground(new Color(22, 24, 30));
        logPane.setForeground(COLOR_TEXT);
        logPane.setCaretColor(new Color(22, 24, 30));
        logPane.setBorder(new EmptyBorder(12, 14, 12, 14));
        logDocument = logPane.getStyledDocument();

        paintTag("default", COLOR_TEXT);
        paintTag("info",    COLOR_INFO);
        paintTag("success", COLOR_SUCCESS);
        paintTag("warning", COLOR_WARNING);
        paintTag("error",   COLOR_ERROR);
        paintTag("header",  COLOR_PRIMARY, true);

        JScrollPane scroll = new JScrollPane(logPane);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        scroll.getViewport().setBackground(new Color(22, 24, 30));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel spawnBot() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(COLOR_BG_PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(10, 24, 10, 24)
        ));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(COLOR_TEXT_DIM);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 6));
        progressBar.setBorderPainted(false);
        progressBar.setForeground(COLOR_PRIMARY);
        progressBar.setBackground(COLOR_BG_INPUT);
        progressBar.setVisible(false);

        JLabel brand = new JLabel("Zero Dependencies \u2022 Pure JDK \u2022 Cross-Platform");
        brand.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        brand.setForeground(new Color(100, 110, 130));

        footer.add(statusLabel,  BorderLayout.WEST);
        footer.add(progressBar,  BorderLayout.CENTER);
        footer.add(brand,        BorderLayout.EAST);
        return footer;
    }

    private JButton makeBtn(String txt, Color c1, Color c2) {
        JButton btn = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(c1);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(c2);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(c1);
            }
        });
        return btn;
    }

    private void paintTag(String key, Color col) {
        paintTag(key, col, false);
    }

    private void paintTag(String key, Color col, boolean fat) {
        Style style = logPane.addStyle(key, null);
        StyleConstants.setForeground(style, col);
        StyleConstants.setFontFamily(style, "Consolas");
        StyleConstants.setFontSize(style, 12);
        if (fat) StyleConstants.setBold(style, true);
    }

    private void printLog(String msg, String tag) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = logPane.getStyle(tag);
                logDocument.insertString(logDocument.getLength(), msg + "\n", style);
                logPane.setCaretPosition(logDocument.getLength());
            } catch (BadLocationException ignored) {
            }
        });
    }

    private void grabFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a Folder to Organize");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile().toPath()
                    .toAbsolutePath().normalize();
            pathField.setText(selectedDirectory.toString());
            pathField.setForeground(COLOR_TEXT);
            organizeButton.setEnabled(true);
            statusLabel.setText("Folder selected — ready to organize");
            printLog("Selected folder: " + selectedDirectory, "info");
        }
    }

    private void smashDo() {
        if (selectedDirectory == null) return;

        selectButton.setEnabled(false);
        organizeButton.setEnabled(false);
        undoButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Organizing...");

        moveHistory.clear();
        createdFolders.clear();
        try {
            logDocument.remove(0, logDocument.getLength());
        } catch (BadLocationException ignored) { }

        SwingWorker<Void, String[]> worker = new SwingWorker<>() {

            private int movedCount   = 0;
            private int skippedCount = 0;

            @Override
            protected Void doInBackground() {
                publish(new String[]{"\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510", "header"});
                publish(new String[]{"\u2502   Scanning: " + selectedDirectory.getFileName(), "header"});
                publish(new String[]{"\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518", "header"});

                try (DirectoryStream<Path> stream =
                             Files.newDirectoryStream(selectedDirectory)) {

                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) continue;

                        String fileName  = entry.getFileName().toString();
                        String extension = ripTail(fileName);

                        if (extension.isEmpty()) {
                            publish(new String[]{
                                    "  [SKIP]  " + fileName + "  (no extension)", "warning"});
                            skippedCount++;
                            continue;
                        }

                        String category = EXTENSION_MAP.get(extension.toLowerCase());

                        if (category == null) {
                            publish(new String[]{
                                    "  [SKIP]  " + fileName + "  (unknown: ."
                                            + extension + ")", "warning"});
                            skippedCount++;
                            continue;
                        }

                        Path categoryDir = selectedDirectory.resolve(category);
                        if (!Files.exists(categoryDir)) {
                            Files.createDirectories(categoryDir);
                            publish(new String[]{
                                    "  [NEW]   Created folder: " + category + "/",
                                    "info"});
                        }

                        Path destination = categoryDir.resolve(fileName);
                        moveHistory.add(new Path[]{entry, destination});
                        createdFolders.add(categoryDir);
                        Files.move(entry, destination,
                                StandardCopyOption.REPLACE_EXISTING);

                        publish(new String[]{
                                "  [MOVED] " + fileName + "  \u2192  " + category + "/",
                                "success"});
                        movedCount++;

                        Thread.sleep(40);
                    }

                } catch (NoSuchFileException e) {
                    publish(new String[]{
                            "  [ERROR] File not found: " + e.getMessage(), "error"});
                } catch (AccessDeniedException e) {
                    publish(new String[]{
                            "  [ERROR] Access denied: " + e.getMessage(), "error"});
                } catch (IOException e) {
                    publish(new String[]{
                            "  [ERROR] I/O error: " + e.getMessage(), "error"});
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }

                return null;
            }

            @Override
            protected void process(java.util.List<String[]> chunks) {
                for (String[] entry : chunks) {
                    printLog(entry[0], entry[1]);
                }
            }

            @Override
            protected void done() {
                printLog("", "default");
                printLog("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", "header");
                printLog("  Done!  Moved: " + movedCount
                        + "  |  Skipped: " + skippedCount, "success");
                printLog("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", "header");

                statusLabel.setText("Complete — " + movedCount + " files organized");
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                selectButton.setEnabled(true);
                organizeButton.setEnabled(true);
                undoButton.setEnabled(!moveHistory.isEmpty());
            }
        };

        worker.execute();
    }

    private void rollbackUnDo() {
        if (moveHistory.isEmpty()) return;

        selectButton.setEnabled(false);
        organizeButton.setEnabled(false);
        undoButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Undoing...");

        try {
            logDocument.remove(0, logDocument.getLength());
        } catch (BadLocationException ignored) { }

        List<Path[]> historySnapshot = new ArrayList<>(moveHistory);
        Set<Path> foldersSnapshot = new LinkedHashSet<>(createdFolders);

        SwingWorker<Void, String[]> worker = new SwingWorker<>() {
            private int restoredCount = 0;
            private int errorCount    = 0;

            @Override
            protected Void doInBackground() {
                publish(new String[]{"\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510", "header"});
                publish(new String[]{"\u2502   Undoing last organize operation", "header"});
                publish(new String[]{"\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518", "header"});

                for (int i = historySnapshot.size() - 1; i >= 0; i--) {
                    Path[] record = historySnapshot.get(i);
                    Path originalPath = record[0];
                    Path movedPath    = record[1];
                    try {
                        Files.move(movedPath, originalPath,
                                StandardCopyOption.REPLACE_EXISTING);
                        publish(new String[]{
                                "  [RESTORED] " + movedPath.getFileName()
                                        + "  \u2192  " + originalPath.getParent().getFileName() + "/",
                                "success"});
                        restoredCount++;
                        Thread.sleep(40);
                    } catch (IOException e) {
                        publish(new String[]{
                                "  [ERROR] Could not restore "
                                        + movedPath.getFileName() + ": " + e.getMessage(),
                                "error"});
                        errorCount++;
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }

                for (Path folder : foldersSnapshot) {
                    try {
                        if (Files.exists(folder) && isHollowDir(folder)) {
                            Files.delete(folder);
                            publish(new String[]{
                                    "  [DEL]   Removed empty folder: "
                                            + folder.getFileName() + "/", "info"});
                        }
                    } catch (IOException e) {
                        publish(new String[]{
                                "  [WARN]  Could not remove folder "
                                        + folder.getFileName() + ": " + e.getMessage(),
                                "warning"});
                    }
                }

                return null;
            }

            @Override
            protected void process(java.util.List<String[]> chunks) {
                for (String[] entry : chunks) {
                    printLog(entry[0], entry[1]);
                }
            }

            @Override
            protected void done() {
                printLog("", "default");
                printLog("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", "header");
                printLog("  Undo complete!  Restored: " + restoredCount
                        + "  |  Errors: " + errorCount, "success");
                printLog("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", "header");

                moveHistory.clear();
                createdFolders.clear();
                statusLabel.setText("Undo complete — " + restoredCount + " files restored");
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                selectButton.setEnabled(true);
                organizeButton.setEnabled(true);
                undoButton.setEnabled(false);
            }
        };

        worker.execute();
    }

    private static boolean isHollowDir(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }

    private static String ripTail(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            GuiOrganizer app = new GuiOrganizer();
            app.setVisible(true);
        });
    }
}
