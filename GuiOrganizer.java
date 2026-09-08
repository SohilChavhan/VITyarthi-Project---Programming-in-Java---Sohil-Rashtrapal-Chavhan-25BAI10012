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
import javax.swing.table.*;
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
    private static final Color COLOR_HISTORY    = new Color(139, 92, 246);
    private static final Color COLOR_HISTORY_HV = new Color(124, 58, 237);
    private static final Color COLOR_SMART      = new Color(236, 72, 153);
    private static final Color COLOR_SMART_HV   = new Color(219, 39, 119);
    private static final Color COLOR_DOWNLOAD   = new Color(6, 182, 212);
    private static final Color COLOR_DOWNLOAD_HV = new Color(8, 145, 178);

    private JTextField pathField;
    private JTextPane  logPane;
    private StyledDocument logDocument;
    private JButton    selectButton;
    private JButton    organizeButton;
    private JButton    undoButton;
    private JButton    historyButton;
    private JButton    smartRulesButton;
    private JButton    downloadButton;
    private JLabel     statusLabel;
    private JProgressBar progressBar;
    private Path       selectedDirectory;

    private final List<Path[]> moveHistory = new ArrayList<>();
    private final Set<Path> createdFolders = new LinkedHashSet<>();

    private final HistoryManager historyManager = new HistoryManager();
    private int currentSessionId = -1;

    public GuiOrganizer() {
        super("Smart Directory Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setMinimumSize(new Dimension(650, 500));
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

        JLabel title = new JLabel("Smart Directory Organizer");
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

        selectButton = makeBtn("Select Folder", COLOR_PRIMARY, COLOR_PRIMARY_HV);
        selectButton.addActionListener(e -> grabFolder());

        organizeButton = makeBtn("Organize Now", new Color(22, 163, 74), new Color(21, 128, 61));
        organizeButton.setEnabled(false);
        organizeButton.addActionListener(e -> smashDo());

        undoButton = makeBtn("Undo", new Color(220, 120, 30), new Color(190, 100, 20));
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> rollbackUnDo());

        historyButton = makeBtn("History", COLOR_HISTORY, COLOR_HISTORY_HV);
        historyButton.addActionListener(e -> showHistoryDialog());

        smartRulesButton = makeBtn("Smart Rules", COLOR_SMART, COLOR_SMART_HV);
        smartRulesButton.addActionListener(e -> showSmartRulesDialog());

        downloadButton = makeBtn("Download & Sort", COLOR_DOWNLOAD, COLOR_DOWNLOAD_HV);
        downloadButton.setEnabled(false);
        downloadButton.addActionListener(e -> showDownloadDialog());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(wrapWithInfo(selectButton, "Selects the folder you want to organize or download files into."));
        btnPanel.add(wrapWithInfo(organizeButton, "Scans the selected folder and moves files into subfolders based on their extension or smart rules."));
        btnPanel.add(wrapWithInfo(downloadButton, "Downloads a file from a URL, scans it, and automatically sorts it into the right folder."));
        btnPanel.add(wrapWithInfo(undoButton, "Reverts the last organize or download action, moving files back to where they were."));
        btnPanel.add(wrapWithInfo(historyButton, "Shows a history of all your organizing sessions, allowing you to undo past actions."));
        btnPanel.add(wrapWithInfo(smartRulesButton, "Lets you define keywords to automatically sort files based on their content (e.g., 'invoice' -> 'Finance/')."));

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

        JLabel brand = new JLabel("SQLite History \u2022 Pure JDK \u2022 Cross-Platform");
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 14, 6, 14));

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

    private JPanel wrapWithInfo(JButton mainBtn, String infoText) {
        JPanel wrap = new JPanel(new BorderLayout(4, 0));
        wrap.setOpaque(false);

        JButton infoBtn = new JButton("(?)"); 
        infoBtn.setFont(new Font("Consolas", Font.BOLD, 14));
        infoBtn.setMargin(new Insets(0, 2, 0, 2));
        infoBtn.setFocusPainted(false);
        infoBtn.setContentAreaFilled(false);
        infoBtn.setBorderPainted(false);
        infoBtn.setForeground(COLOR_TEXT_DIM);
        infoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        infoBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                infoBtn.setForeground(COLOR_INFO);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                infoBtn.setForeground(COLOR_TEXT_DIM);
            }
        });

        infoBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainBtn, infoText, "Information", JOptionPane.INFORMATION_MESSAGE);
        });

        wrap.add(mainBtn, BorderLayout.CENTER);
        wrap.add(infoBtn, BorderLayout.EAST);
        return wrap;
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
            downloadButton.setEnabled(true);
            statusLabel.setText("Folder selected \u2014 ready to organize");
            printLog("Selected folder: " + selectedDirectory, "info");
        }
    }

    private void smashDo() {
        if (selectedDirectory == null) return;

        selectButton.setEnabled(false);
        organizeButton.setEnabled(false);
        undoButton.setEnabled(false);
        historyButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Organizing...");

        moveHistory.clear();
        createdFolders.clear();
        try {
            logDocument.remove(0, logDocument.getLength());
        } catch (BadLocationException ignored) { }

        // Start a new DB session
        currentSessionId = historyManager.startSession(selectedDirectory.toString());

        SwingWorker<Void, String[]> worker = new SwingWorker<>() {

            private int movedCount   = 0;
            private int skippedCount = 0;

            @Override
            protected Void doInBackground() {
                // Load content rules from the database
                java.util.List<ContentScanner.ContentRule> contentRules =
                        historyManager.getAllRules();
                boolean hasSmartRules = !contentRules.isEmpty();

                publish(new String[]{"\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510", "header"});
                publish(new String[]{"\u2502   Scanning: " + selectedDirectory.getFileName(), "header"});
                if (hasSmartRules) {
                    publish(new String[]{"\u2502   \uD83E\uDDE0 Smart Rules active: " + contentRules.size() + " rule(s)", "header"});
                }
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

                        // ── Smart content-based sorting (takes priority) ──
                        String category = null;
                        boolean smartMatched = false;

                        if (hasSmartRules && ContentScanner.isScannable(extension)) {
                            publish(new String[]{
                                    "  [SCAN]  Analyzing content: " + fileName, "info"});
                            String text = ContentScanner.extractText(entry);
                            String matchedFolder = ContentScanner.matchRule(text, contentRules);
                            if (matchedFolder != null) {
                                category = matchedFolder;
                                smartMatched = true;
                            }
                        }

                        // ── Fallback: extension-based sorting ──
                        if (category == null) {
                            category = EXTENSION_MAP.get(extension.toLowerCase());
                        }

                        if (category == null) {
                            publish(new String[]{
                                    "  [SKIP]  " + fileName + "  (unknown: ."
                                            + extension + ")", "warning"});
                            skippedCount++;
                            continue;
                        }

                        Path categoryDir = selectedDirectory.resolve(category);
                        boolean newFolder = false;
                        if (!Files.exists(categoryDir)) {
                            Files.createDirectories(categoryDir);
                            newFolder = true;
                            publish(new String[]{
                                    "  [NEW]   Created folder: " + category + "/",
                                    "info"});
                        }

                        Path destination = categoryDir.resolve(fileName);
                        moveHistory.add(new Path[]{entry, destination});
                        createdFolders.add(categoryDir);
                        Files.move(entry, destination,
                                StandardCopyOption.REPLACE_EXISTING);

                        // Persist to database
                        if (currentSessionId != -1) {
                            historyManager.recordMove(currentSessionId, entry, destination, category);
                            if (newFolder) {
                                historyManager.recordCreatedFolder(currentSessionId, categoryDir);
                            }
                        }

                        String tag = smartMatched ? "info" : "success";
                        String prefix = smartMatched ? "  [\uD83E\uDDE0 SMART] " : "  [MOVED] ";
                        publish(new String[]{
                                prefix + fileName + "  \u2192  " + category + "/",
                                tag});
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

                // Finalize DB session
                if (currentSessionId != -1) {
                    historyManager.finalizeSession(currentSessionId, movedCount, skippedCount);
                }

                statusLabel.setText("Complete \u2014 " + movedCount + " files organized");
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                selectButton.setEnabled(true);
                organizeButton.setEnabled(true);
                undoButton.setEnabled(!moveHistory.isEmpty());
                historyButton.setEnabled(true);
            }
        };

        worker.execute();
    }

    private void rollbackUnDo() {
        if (moveHistory.isEmpty()) return;

        selectButton.setEnabled(false);
        organizeButton.setEnabled(false);
        undoButton.setEnabled(false);
        historyButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Undoing...");

        try {
            logDocument.remove(0, logDocument.getLength());
        } catch (BadLocationException ignored) { }

        List<Path[]> historySnapshot = new ArrayList<>(moveHistory);
        Set<Path> foldersSnapshot = new LinkedHashSet<>(createdFolders);
        final int sessionToUndo = currentSessionId;

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

                // Mark session as undone in DB
                if (sessionToUndo != -1) {
                    historyManager.markUndone(sessionToUndo);
                }

                moveHistory.clear();
                createdFolders.clear();
                currentSessionId = -1;
                statusLabel.setText("Undo complete \u2014 " + restoredCount + " files restored");
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                selectButton.setEnabled(true);
                organizeButton.setEnabled(true);
                undoButton.setEnabled(false);
                historyButton.setEnabled(true);
            }
        };

        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HISTORY DIALOG — shows all past organize sessions from the DB
    // ═══════════════════════════════════════════════════════════════════

    private void showHistoryDialog() {
        JDialog dialog = new JDialog(this, "Organize History", true);
        dialog.setSize(900, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_BG_DARK);
        dialog.setContentPane(root);

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel hTitle = new JLabel("\uD83D\uDCDC  Organize History");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hTitle.setForeground(COLOR_TEXT);
        JLabel hSub = new JLabel("  All past file-organization sessions stored in the database");
        hSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hSub.setForeground(COLOR_TEXT_DIM);
        header.add(hTitle, BorderLayout.WEST);
        header.add(hSub, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Table ──
        String[] columns = {"#", "Date & Time", "Target Directory", "Files Moved", "Skipped", "Status"};
        List<HistoryManager.SessionRecord> sessions = historyManager.getAllSessions();

        Object[][] data = new Object[sessions.size()][6];
        for (int i = 0; i < sessions.size(); i++) {
            HistoryManager.SessionRecord s = sessions.get(i);
            data[i][0] = s.id;
            data[i][1] = s.timestamp;
            data[i][2] = s.targetDir;
            data[i][3] = s.filesMoved;
            data[i][4] = s.filesSkipped;
            data[i][5] = s.undone ? "\u21A9 Undone" : "\u2705 Active";
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setBackground(new Color(22, 24, 30));
        table.setForeground(COLOR_TEXT);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(59, 130, 246, 60));
        table.setSelectionForeground(COLOR_TEXT);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        // Style the header
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(COLOR_BG_PANEL);
        tableHeader.setForeground(COLOR_TEXT);
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        tableHeader.setReorderingAllowed(false);

        // Custom renderer for status column coloring
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setBackground(isSelected ? new Color(59, 130, 246, 60) : new Color(22, 24, 30));
                String status = value != null ? value.toString() : "";
                if (status.contains("Undone")) {
                    setForeground(COLOR_WARNING);
                } else {
                    setForeground(COLOR_SUCCESS);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        // Center-align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setBackground(isSelected ? new Color(59, 130, 246, 60) : new Color(22, 24, 30));
                setForeground(COLOR_TEXT);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(22, 24, 30));
        root.add(scrollPane, BorderLayout.CENTER);

        // ── Detail panel (shows files for selected session) ──
        JTextPane detailPane = new JTextPane();
        detailPane.setEditable(false);
        detailPane.setFont(new Font("Consolas", Font.PLAIN, 11));
        detailPane.setBackground(new Color(22, 24, 30));
        detailPane.setForeground(COLOR_TEXT);
        detailPane.setBorder(new EmptyBorder(8, 12, 8, 12));
        StyledDocument detailDoc = detailPane.getStyledDocument();

        Style defStyle = detailPane.addStyle("det_default", null);
        StyleConstants.setForeground(defStyle, COLOR_TEXT);
        StyleConstants.setFontFamily(defStyle, "Consolas");
        StyleConstants.setFontSize(defStyle, 11);

        Style catStyle = detailPane.addStyle("det_category", null);
        StyleConstants.setForeground(catStyle, COLOR_INFO);
        StyleConstants.setFontFamily(catStyle, "Consolas");
        StyleConstants.setFontSize(catStyle, 11);
        StyleConstants.setBold(catStyle, true);

        Style pathStyle = detailPane.addStyle("det_path", null);
        StyleConstants.setForeground(pathStyle, COLOR_TEXT_DIM);
        StyleConstants.setFontFamily(pathStyle, "Consolas");
        StyleConstants.setFontSize(pathStyle, 11);

        JScrollPane detailScroll = new JScrollPane(detailPane);
        detailScroll.setPreferredSize(new Dimension(900, 140));
        detailScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder()
        ));
        detailScroll.getViewport().setBackground(new Color(22, 24, 30));

        // Populate detail pane on row selection
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) return;

            int sessionId = (int) tableModel.getValueAt(selectedRow, 0);
            List<HistoryManager.MoveRecord> moves = historyManager.getMovesForSession(sessionId);

            try {
                detailDoc.remove(0, detailDoc.getLength());
                detailDoc.insertString(detailDoc.getLength(),
                        "  Session #" + sessionId + " \u2014 " + moves.size() + " file(s):\n\n",
                        detailPane.getStyle("det_category"));
                for (HistoryManager.MoveRecord m : moves) {
                    detailDoc.insertString(detailDoc.getLength(),
                            "    " + m.fileName, detailPane.getStyle("det_default"));
                    detailDoc.insertString(detailDoc.getLength(),
                            "  \u2192  " + m.category + "/", detailPane.getStyle("det_category"));
                    detailDoc.insertString(detailDoc.getLength(),
                            "    (" + m.movedPath + ")\n", detailPane.getStyle("det_path"));
                }
            } catch (BadLocationException ignored) {}
        });

        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setBackground(COLOR_BG_DARK);
        bottomSection.add(detailScroll, BorderLayout.CENTER);

        // ── Action buttons ──
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionBar.setBackground(COLOR_BG_PANEL);
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton undoSelectedBtn = makeBtn("Undo Selected Session",
                new Color(220, 120, 30), new Color(190, 100, 20));
        JButton deleteBtn = makeBtn("Delete Record",
                new Color(180, 50, 50), new Color(150, 40, 40));
        JButton closeBtn = makeBtn("Close", COLOR_BG_INPUT, new Color(70, 75, 90));

        undoSelectedBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a session first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int sessionId = (int) tableModel.getValueAt(selectedRow, 0);
            String status = tableModel.getValueAt(selectedRow, 5).toString();
            if (status.contains("Undone")) {
                JOptionPane.showMessageDialog(dialog, "This session was already undone.",
                        "Already Undone", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Undo session #" + sessionId + "?\nThis will move all files back to their original locations.",
                    "Confirm Undo", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // Perform the undo from DB records
            undoFromDatabase(sessionId, dialog, tableModel, selectedRow);
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a session first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int sessionId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Delete history record for session #" + sessionId + "?\n"
                    + "This only removes the record, not the actual files.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            historyManager.deleteSession(sessionId);
            tableModel.removeRow(selectedRow);
            try {
                detailDoc.remove(0, detailDoc.getLength());
            } catch (BadLocationException ignored) {}
            statusLabel.setText("History record #" + sessionId + " deleted");
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        actionBar.add(wrapWithInfo(undoSelectedBtn, "Reverts all files organized during this specific session to their original locations."));
        actionBar.add(wrapWithInfo(deleteBtn, "Deletes the session record from the database. (Does NOT move any files)"));
        actionBar.add(wrapWithInfo(closeBtn, "Closes this dialog without making changes."));

        bottomSection.add(actionBar, BorderLayout.SOUTH);
        root.add(bottomSection, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Undoes a session using records stored in the database.
     * Works even after the app has been restarted.
     */
    private void undoFromDatabase(int sessionId, JDialog parentDialog,
                                  DefaultTableModel tableModel, int tableRow) {
        List<HistoryManager.MoveRecord> moves = historyManager.getMovesForSession(sessionId);
        List<String> folders = historyManager.getCreatedFoldersForSession(sessionId);

        if (moves.isEmpty()) {
            JOptionPane.showMessageDialog(parentDialog, "No file moves found for this session.",
                    "Nothing to Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        statusLabel.setText("Undoing session #" + sessionId + "...");
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);

        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int restored = 0, errors = 0;

                // Restore files in reverse order
                for (int i = moves.size() - 1; i >= 0; i--) {
                    HistoryManager.MoveRecord m = moves.get(i);
                    Path movedPath = Paths.get(m.movedPath);
                    Path originalPath = Paths.get(m.originalPath);

                    try {
                        if (Files.exists(movedPath)) {
                            Files.move(movedPath, originalPath,
                                    StandardCopyOption.REPLACE_EXISTING);
                            restored++;
                        } else {
                            errors++;
                        }
                    } catch (IOException ex) {
                        errors++;
                    }
                }

                // Remove empty folders
                for (String folderStr : folders) {
                    try {
                        Path folder = Paths.get(folderStr);
                        if (Files.exists(folder) && isHollowDir(folder)) {
                            Files.delete(folder);
                        }
                    } catch (IOException ignored) {}
                }

                return new int[]{restored, errors};
            }

            @Override
            protected void done() {
                try {
                    int[] result = get();
                    historyManager.markUndone(sessionId);
                    tableModel.setValueAt("\u21A9 Undone", tableRow, 5);
                    statusLabel.setText("Session #" + sessionId + " undone \u2014 "
                            + result[0] + " restored, " + result[1] + " errors");
                    printLog("  [HISTORY UNDO] Session #" + sessionId
                            + " \u2014 Restored: " + result[0] + " | Errors: " + result[1], "info");
                } catch (Exception ex) {
                    statusLabel.setText("Undo failed: " + ex.getMessage());
                }
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
            }
        };

        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SMART RULES DIALOG — manage content-based keyword rules
    // ═══════════════════════════════════════════════════════════════════

    private void showSmartRulesDialog() {
        JDialog dialog = new JDialog(this, "Smart Content Rules", true);
        dialog.setSize(720, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_BG_DARK);
        dialog.setContentPane(root);

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel hTitle = new JLabel("Smart Content Rules");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hTitle.setForeground(COLOR_TEXT);
        JLabel hSub = new JLabel("<html>Map keywords to folders. Files containing matching keywords<br>will be sorted by content instead of extension.</html>");
        hSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hSub.setForeground(COLOR_TEXT_DIM);
        header.add(hTitle, BorderLayout.WEST);
        header.add(hSub, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Rules table ──
        String[] columns = {"#", "Folder / Subject", "Keywords (comma-separated)"};
        java.util.List<ContentScanner.ContentRule> rules = historyManager.getAllRules();

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (ContentScanner.ContentRule r : rules) {
            tableModel.addRow(new Object[]{r.id, r.folderName, r.keywords});
        }

        JTable table = new JTable(tableModel);
        table.setBackground(new Color(22, 24, 30));
        table.setForeground(COLOR_TEXT);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(236, 72, 153, 50));
        table.setSelectionForeground(COLOR_TEXT);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(COLOR_BG_PANEL);
        tableHeader.setForeground(COLOR_TEXT);
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        tableHeader.setReorderingAllowed(false);

        // Center-align ID column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setBackground(isSelected ? new Color(236, 72, 153, 50) : new Color(22, 24, 30));
                setForeground(COLOR_TEXT);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(22, 24, 30));
        root.add(scrollPane, BorderLayout.CENTER);

        // ── Add-rule panel + action buttons ──
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(COLOR_BG_PANEL);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        // Input row for adding new rules
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(COLOR_BG_PANEL);
        inputRow.setBorder(new EmptyBorder(12, 20, 8, 20));

        JLabel addLabel = new JLabel("Add Rule:");
        addLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addLabel.setForeground(COLOR_TEXT);

        JTextField folderField = new JTextField();
        folderField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        folderField.setForeground(COLOR_TEXT);
        folderField.setBackground(COLOR_BG_INPUT);
        folderField.setCaretColor(COLOR_TEXT);
        folderField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        folderField.setToolTipText("Folder / subject name (e.g. Mathematics, Invoices)");
        folderField.setColumns(12);

        JTextField keywordsField = new JTextField();
        keywordsField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        keywordsField.setForeground(COLOR_TEXT);
        keywordsField.setBackground(COLOR_BG_INPUT);
        keywordsField.setCaretColor(COLOR_TEXT);
        keywordsField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        keywordsField.setToolTipText("Comma-separated keywords (e.g. algebra, calculus, integration)");

        // Add placeholder text
        folderField.setText("Folder name...");
        folderField.setForeground(COLOR_TEXT_DIM);
        folderField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (folderField.getText().equals("Folder name...")) {
                    folderField.setText("");
                    folderField.setForeground(COLOR_TEXT);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (folderField.getText().trim().isEmpty()) {
                    folderField.setText("Folder name...");
                    folderField.setForeground(COLOR_TEXT_DIM);
                }
            }
        });

        keywordsField.setText("keyword1, keyword2, ...");
        keywordsField.setForeground(COLOR_TEXT_DIM);
        keywordsField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (keywordsField.getText().equals("keyword1, keyword2, ...")) {
                    keywordsField.setText("");
                    keywordsField.setForeground(COLOR_TEXT);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (keywordsField.getText().trim().isEmpty()) {
                    keywordsField.setText("keyword1, keyword2, ...");
                    keywordsField.setForeground(COLOR_TEXT_DIM);
                }
            }
        });

        JPanel fieldsPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        fieldsPanel.setOpaque(false);
        fieldsPanel.add(folderField);
        fieldsPanel.add(keywordsField);

        inputRow.add(addLabel, BorderLayout.WEST);
        inputRow.add(fieldsPanel, BorderLayout.CENTER);

        // Action buttons row
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionBar.setBackground(COLOR_BG_PANEL);

        JButton addBtn = makeBtn("Add Rule", COLOR_SMART, COLOR_SMART_HV);
        JButton deleteBtn = makeBtn("Delete Selected",
                new Color(180, 50, 50), new Color(150, 40, 40));
        JButton closeBtn = makeBtn("Close", COLOR_BG_INPUT, new Color(70, 75, 90));

        addBtn.addActionListener(e -> {
            String folder = folderField.getText().trim();
            String keywords = keywordsField.getText().trim();

            if (folder.isEmpty() || folder.equals("Folder name...")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a folder/subject name.",
                        "Missing Folder", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (keywords.isEmpty() || keywords.equals("keyword1, keyword2, ...")) {
                JOptionPane.showMessageDialog(dialog, "Please enter at least one keyword.",
                        "Missing Keywords", JOptionPane.WARNING_MESSAGE);
                return;
            }

            historyManager.addRule(folder, keywords);

            // Refresh the table
            tableModel.setRowCount(0);
            for (ContentScanner.ContentRule r : historyManager.getAllRules()) {
                tableModel.addRow(new Object[]{r.id, r.folderName, r.keywords});
            }

            // Reset input fields
            folderField.setText("Folder name...");
            folderField.setForeground(COLOR_TEXT_DIM);
            keywordsField.setText("keyword1, keyword2, ...");
            keywordsField.setForeground(COLOR_TEXT_DIM);

            statusLabel.setText("Smart rule added: " + folder);
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a rule first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int ruleId = (int) tableModel.getValueAt(selectedRow, 0);
            String ruleName = tableModel.getValueAt(selectedRow, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Delete rule '" + ruleName + "'?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            historyManager.deleteRule(ruleId);
            tableModel.removeRow(selectedRow);
            statusLabel.setText("Smart rule deleted: " + ruleName);
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        actionBar.add(wrapWithInfo(addBtn, "Creates a new smart rule mapping keywords to a folder name."));
        actionBar.add(wrapWithInfo(deleteBtn, "Deletes the currently selected smart rule."));
        actionBar.add(wrapWithInfo(closeBtn, "Closes this dialog."));

        bottomPanel.add(inputRow, BorderLayout.NORTH);
        bottomPanel.add(actionBar, BorderLayout.SOUTH);
        root.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DOWNLOAD & SORT — download a file and auto-organize it
    // ═══════════════════════════════════════════════════════════════════

    private void showDownloadDialog() {
        if (selectedDirectory == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a target folder first.",
                    "No Folder Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Build dialog ──
        JDialog dialog = new JDialog(this, "Download & Sort", true);
        dialog.setSize(600, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_BG_DARK);
        dialog.setContentPane(root);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel hTitle = new JLabel("Download & Sort");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hTitle.setForeground(COLOR_TEXT);
        JLabel hSub = new JLabel("<html>Paste a file URL. It will be downloaded,<br>scanned, and sorted automatically.</html>");
        hSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hSub.setForeground(COLOR_TEXT_DIM);
        header.add(hTitle, BorderLayout.WEST);
        header.add(hSub, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center — URL input + progress
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(COLOR_BG_DARK);
        center.setBorder(new EmptyBorder(16, 20, 8, 20));

        JLabel urlLabel = new JLabel("File URL:");
        urlLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        urlLabel.setForeground(COLOR_TEXT);
        urlLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField urlField = new JTextField();
        urlField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        urlField.setForeground(COLOR_TEXT);
        urlField.setBackground(COLOR_BG_INPUT);
        urlField.setCaretColor(COLOR_TEXT);
        urlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        urlField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel destLabel = new JLabel("Destination: " + selectedDirectory);
        destLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        destLabel.setForeground(COLOR_TEXT_DIM);
        destLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        destLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JProgressBar dlProgress = new JProgressBar(0, 100);
        dlProgress.setStringPainted(true);
        dlProgress.setString("Ready");
        dlProgress.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dlProgress.setForeground(COLOR_DOWNLOAD);
        dlProgress.setBackground(COLOR_BG_INPUT);
        dlProgress.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        dlProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        dlProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        dlProgress.setVisible(false);

        center.add(urlLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(urlField);
        center.add(destLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(dlProgress);
        root.add(center, BorderLayout.CENTER);

        // Bottom — action buttons
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionBar.setBackground(COLOR_BG_PANEL);
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton goBtn = makeBtn("Download & Sort", COLOR_DOWNLOAD, COLOR_DOWNLOAD_HV);
        JButton cancelBtn = makeBtn("Cancel", COLOR_BG_INPUT, new Color(70, 75, 90));

        cancelBtn.addActionListener(e -> dialog.dispose());

        goBtn.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a URL.", "No URL",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
                urlField.setText(url);
            }

            goBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            urlField.setEditable(false);
            dlProgress.setVisible(true);
            dlProgress.setIndeterminate(true);
            dlProgress.setString("Connecting...");

            final String finalUrl = url;

            SwingWorker<Path, int[]> worker = new SwingWorker<>() {
                @Override
                protected Path doInBackground() {
                    try {
                        DownloadManager dm = new DownloadManager(
                                finalUrl, selectedDirectory);

                        dm.setProgressListener((downloaded, total) -> {
                            if (total > 0) {
                                int pct = (int) ((downloaded * 100) / total);
                                publish(new int[]{pct, (int)(downloaded/1024),
                                        (int)(total/1024)});
                            } else {
                                publish(new int[]{-1, (int)(downloaded/1024), -1});
                            }
                        });

                        return dm.download();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                protected void process(java.util.List<int[]> chunks) {
                    int[] latest = chunks.get(chunks.size() - 1);
                    if (latest[0] >= 0) {
                        dlProgress.setIndeterminate(false);
                        dlProgress.setValue(latest[0]);
                        dlProgress.setString(latest[0] + "%  ("
                                + latest[1] + " KB / " + latest[2] + " KB)");
                    } else {
                        dlProgress.setIndeterminate(true);
                        dlProgress.setString("Downloading... " + latest[1] + " KB");
                    }
                }

                @Override
                protected void done() {
                    try {
                        Path downloadedFile = get();
                        dlProgress.setIndeterminate(false);
                        dlProgress.setValue(100);
                        dlProgress.setString("Downloaded! Sorting...");

                        // Sort the downloaded file
                        sortDownloadedFile(downloadedFile);

                        dlProgress.setString("\u2714 Complete!");
                        dlProgress.setForeground(COLOR_SUCCESS);

                        // Close dialog after a short delay
                        Timer closeTimer = new Timer(1500, evt -> dialog.dispose());
                        closeTimer.setRepeats(false);
                        closeTimer.start();

                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null
                                ? ex.getCause() : ex;
                        dlProgress.setIndeterminate(false);
                        dlProgress.setString("\u2718 Failed: " + cause.getMessage());
                        dlProgress.setForeground(COLOR_ERROR);

                        printLog("  [ERROR] Download failed: "
                                + cause.getMessage(), "error");
                        statusLabel.setText("Download failed");

                        goBtn.setEnabled(true);
                        cancelBtn.setEnabled(true);
                        urlField.setEditable(true);
                    }
                }
            };

            printLog("", "default");
            printLog("\u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510", "header");
            printLog("\u2502  \uD83D\uDCE5 Downloading: " + finalUrl, "header");
            printLog("\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518", "header");
            statusLabel.setText("Downloading...");

            worker.execute();
        });

        actionBar.add(wrapWithInfo(goBtn, "Starts downloading the file and automatically organizes it."));
        actionBar.add(wrapWithInfo(cancelBtn, "Cancels the download operation and closes this dialog."));
        root.add(actionBar, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Takes a freshly downloaded file, scans its content with smart rules,
     * and moves it to the appropriate category folder.
     */
    private void sortDownloadedFile(Path file) {
        String fileName  = file.getFileName().toString();
        String extension = ripTail(fileName);

        // Load smart rules
        java.util.List<ContentScanner.ContentRule> contentRules =
                historyManager.getAllRules();

        // Try content-based match first
        String category = null;
        boolean smartMatched = false;

        if (!contentRules.isEmpty() && !extension.isEmpty()
                && ContentScanner.isScannable(extension)) {
            printLog("  [SCAN]  Analyzing content: " + fileName, "info");
            String text = ContentScanner.extractText(file);
            String matchedFolder = ContentScanner.matchRule(text, contentRules);
            if (matchedFolder != null) {
                category = matchedFolder;
                smartMatched = true;
            }
        }

        // Fallback: extension-based
        if (category == null && !extension.isEmpty()) {
            category = EXTENSION_MAP.get(extension.toLowerCase());
        }

        if (category == null) {
            printLog("  [DONE]  Downloaded " + fileName
                    + " (no matching rule \u2014 kept in place)", "warning");
            statusLabel.setText("Downloaded: " + fileName + " (unsorted)");
            return;
        }

        try {
            Path categoryDir = selectedDirectory.resolve(category);
            boolean newFolder = false;
            if (!Files.exists(categoryDir)) {
                Files.createDirectories(categoryDir);
                newFolder = true;
                printLog("  [NEW]   Created folder: " + category + "/", "info");
            }

            Path destination = categoryDir.resolve(fileName);

            // Start a new session for this download-sort
            int sessionId = historyManager.startSession(
                    selectedDirectory.toString());

            moveHistory.add(new Path[]{file, destination});
            createdFolders.add(categoryDir);
            Files.move(file, destination,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Record in history
            if (sessionId != -1) {
                historyManager.recordMove(sessionId, file, destination, category);
                if (newFolder) {
                    historyManager.recordCreatedFolder(sessionId, categoryDir);
                }
                historyManager.finalizeSession(sessionId, 1, 0);
            }

            String prefix = smartMatched
                    ? "  [\uD83E\uDDE0 SMART] " : "  [SORTED] ";
            printLog(prefix + fileName + "  \u2192  "
                    + category + "/", smartMatched ? "info" : "success");
            printLog("  \u2714 Download + Sort complete!", "success");

            statusLabel.setText("Downloaded & sorted: " + fileName
                    + " \u2192 " + category + "/");
            undoButton.setEnabled(true);

        } catch (IOException ex) {
            printLog("  [ERROR] Failed to sort: " + ex.getMessage(), "error");
            statusLabel.setText("Download OK, sort failed: " + ex.getMessage());
        }
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
