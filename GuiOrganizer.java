import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

/**
 * GuiOrganizer — A full-featured Swing GUI for the Smart Directory Organizer.
 *
 * Why Swing?
 *   Swing is chosen for its absolute stability and guaranteed compatibility
 *   across all operating systems without requiring any user setup.
 *
 * Zero Dependencies:
 *   This entire application requires nothing but a standard JDK to compile
 *   and run.  No Maven, no Gradle, no external libraries.
 *
 * Usage:
 *   javac GuiOrganizer.java
 *   java  GuiOrganizer
 */
public class GuiOrganizer extends JFrame {

    // ──────────────────────────────────────────────
    //  Extension → Category mapping
    // ──────────────────────────────────────────────
    private static final Map<String, String> EXTENSION_MAP = new HashMap<>();

    static {
        // Documents
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

        // Images
        EXTENSION_MAP.put("jpg",  "Images");
        EXTENSION_MAP.put("jpeg", "Images");
        EXTENSION_MAP.put("png",  "Images");
        EXTENSION_MAP.put("gif",  "Images");
        EXTENSION_MAP.put("bmp",  "Images");
        EXTENSION_MAP.put("svg",  "Images");
        EXTENSION_MAP.put("webp", "Images");
        EXTENSION_MAP.put("ico",  "Images");
        EXTENSION_MAP.put("tiff", "Images");

        // Videos
        EXTENSION_MAP.put("mp4",  "Videos");
        EXTENSION_MAP.put("mkv",  "Videos");
        EXTENSION_MAP.put("avi",  "Videos");
        EXTENSION_MAP.put("mov",  "Videos");
        EXTENSION_MAP.put("wmv",  "Videos");
        EXTENSION_MAP.put("flv",  "Videos");
        EXTENSION_MAP.put("webm", "Videos");

        // Audio
        EXTENSION_MAP.put("mp3",  "Audio");
        EXTENSION_MAP.put("wav",  "Audio");
        EXTENSION_MAP.put("flac", "Audio");
        EXTENSION_MAP.put("aac",  "Audio");
        EXTENSION_MAP.put("ogg",  "Audio");
        EXTENSION_MAP.put("wma",  "Audio");

        // Archives
        EXTENSION_MAP.put("zip",  "Archives");
        EXTENSION_MAP.put("rar",  "Archives");
        EXTENSION_MAP.put("7z",   "Archives");
        EXTENSION_MAP.put("tar",  "Archives");
        EXTENSION_MAP.put("gz",   "Archives");
        EXTENSION_MAP.put("bz2",  "Archives");

        // Code / Scripts
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

        // Executables / Installers
        EXTENSION_MAP.put("exe",  "Executables");
        EXTENSION_MAP.put("msi",  "Executables");
        EXTENSION_MAP.put("dmg",  "Executables");
        EXTENSION_MAP.put("deb",  "Executables");
        EXTENSION_MAP.put("rpm",  "Executables");
        EXTENSION_MAP.put("apk",  "Executables");
        EXTENSION_MAP.put("jar",  "Executables");
    }

    // ──────────────────────────────────────────────
    //  Theme colors
    // ──────────────────────────────────────────────
    private static final Color COLOR_PRIMARY    = new Color(59, 130, 246);   // Blue-500
    private static final Color COLOR_PRIMARY_HV = new Color(37, 99, 235);   // Blue-600
    private static final Color COLOR_BG_DARK    = new Color(30, 32, 40);    // Dark slate
    private static final Color COLOR_BG_PANEL   = new Color(39, 42, 55);    // Panel bg
    private static final Color COLOR_BG_INPUT   = new Color(49, 53, 68);    // Input bg
    private static final Color COLOR_TEXT       = new Color(226, 232, 240);  // Slate-200
    private static final Color COLOR_TEXT_DIM   = new Color(148, 163, 184);  // Slate-400
    private static final Color COLOR_SUCCESS    = new Color(34, 197, 94);    // Green-500
    private static final Color COLOR_WARNING    = new Color(250, 204, 21);   // Yellow-400
    private static final Color COLOR_ERROR      = new Color(239, 68, 68);    // Red-500
    private static final Color COLOR_INFO       = new Color(96, 165, 250);   // Blue-400
    private static final Color COLOR_BORDER     = new Color(55, 65, 81);     // Gray-700

    // ──────────────────────────────────────────────
    //  GUI components
    // ──────────────────────────────────────────────
    private JTextField pathField;
    private JTextPane  logPane;
    private StyledDocument logDocument;
    private JButton    selectButton;
    private JButton    organizeButton;
    private JLabel     statusLabel;
    private JProgressBar progressBar;
    private Path       selectedDirectory;

    // ──────────────────────────────────────────────
    //  Constructor — build the UI
    // ──────────────────────────────────────────────
    public GuiOrganizer() {
        super("Smart Directory Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 620);
        setMinimumSize(new Dimension(600, 480));
        setLocationRelativeTo(null);

        // Main content pane
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_BG_DARK);
        setContentPane(root);

        root.add(createHeaderPanel(),  BorderLayout.NORTH);
        root.add(createCenterPanel(),  BorderLayout.CENTER);
        root.add(createFooterPanel(),  BorderLayout.SOUTH);
    }

    // ──────────────────────────────────────────────
    //  Header — title and description
    // ──────────────────────────────────────────────
    private JPanel createHeaderPanel() {
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

    // ──────────────────────────────────────────────
    //  Center — folder selector + log area
    // ──────────────────────────────────────────────
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(COLOR_BG_DARK);
        center.setBorder(new EmptyBorder(16, 24, 8, 24));

        center.add(createSelectorPanel(), BorderLayout.NORTH);
        center.add(createLogPanel(),      BorderLayout.CENTER);
        return center;
    }

    /**
     * Folder selector row: [path field] [Select] [Organize]
     */
    private JPanel createSelectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(COLOR_BG_DARK);

        // Path field (read-only)
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

        // Buttons
        selectButton = createStyledButton("\uD83D\uDCC2  Select Folder", COLOR_PRIMARY, COLOR_PRIMARY_HV);
        selectButton.addActionListener(e -> onSelectFolder());

        organizeButton = createStyledButton("\u26A1  Organize Now", new Color(22, 163, 74), new Color(21, 128, 61));
        organizeButton.setEnabled(false);
        organizeButton.addActionListener(e -> onOrganize());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(selectButton);
        btnPanel.add(organizeButton);

        panel.add(pathField,  BorderLayout.CENTER);
        panel.add(btnPanel,   BorderLayout.EAST);
        return panel;
    }

    /**
     * Log output area with styled text.
     */
    private JPanel createLogPanel() {
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
        logPane.setCaretColor(new Color(22, 24, 30)); // hide caret
        logPane.setBorder(new EmptyBorder(12, 14, 12, 14));
        logDocument = logPane.getStyledDocument();

        // Pre-define text styles
        addStyle("default", COLOR_TEXT);
        addStyle("info",    COLOR_INFO);
        addStyle("success", COLOR_SUCCESS);
        addStyle("warning", COLOR_WARNING);
        addStyle("error",   COLOR_ERROR);
        addStyle("header",  COLOR_PRIMARY, true);

        JScrollPane scroll = new JScrollPane(logPane);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        scroll.getViewport().setBackground(new Color(22, 24, 30));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ──────────────────────────────────────────────
    //  Footer — status bar + progress
    // ──────────────────────────────────────────────
    private JPanel createFooterPanel() {
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

    // ──────────────────────────────────────────────
    //  Button factory with hover effect
    // ──────────────────────────────────────────────
    private JButton createStyledButton(String text, Color bg, Color bgHover) {
        JButton btn = new JButton(text) {
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
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bgHover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    // ──────────────────────────────────────────────
    //  Style helpers for the log pane
    // ──────────────────────────────────────────────
    private void addStyle(String name, Color color) {
        addStyle(name, color, false);
    }

    private void addStyle(String name, Color color, boolean bold) {
        Style style = logPane.addStyle(name, null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontFamily(style, "Consolas");
        StyleConstants.setFontSize(style, 12);
        if (bold) StyleConstants.setBold(style, true);
    }

    private void log(String message, String styleName) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = logPane.getStyle(styleName);
                logDocument.insertString(logDocument.getLength(), message + "\n", style);
                logPane.setCaretPosition(logDocument.getLength());
            } catch (BadLocationException ignored) {
                // Swallow — should never happen
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Action: Select Folder
    // ──────────────────────────────────────────────
    private void onSelectFolder() {
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
            log("Selected folder: " + selectedDirectory, "info");
        }
    }

    // ──────────────────────────────────────────────
    //  Action: Organize
    // ──────────────────────────────────────────────
    private void onOrganize() {
        if (selectedDirectory == null) return;

        // Disable buttons during operation
        selectButton.setEnabled(false);
        organizeButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Organizing...");

        // Clear previous logs
        try {
            logDocument.remove(0, logDocument.getLength());
        } catch (BadLocationException ignored) { }

        // Run the heavy work on a background thread to keep the UI responsive
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
                        // Skip sub-directories
                        if (Files.isDirectory(entry)) continue;

                        String fileName  = entry.getFileName().toString();
                        String extension = extractExtension(fileName);

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

                        // Create category folder if needed
                        Path categoryDir = selectedDirectory.resolve(category);
                        if (!Files.exists(categoryDir)) {
                            Files.createDirectories(categoryDir);
                            publish(new String[]{
                                    "  [NEW]   Created folder: " + category + "/",
                                    "info"});
                        }

                        // Move file
                        Path destination = categoryDir.resolve(fileName);
                        Files.move(entry, destination,
                                StandardCopyOption.REPLACE_EXISTING);

                        publish(new String[]{
                                "  [MOVED] " + fileName + "  \u2192  " + category + "/",
                                "success"});
                        movedCount++;

                        // Tiny pause so the UI visually shows progress
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
                    log(entry[0], entry[1]);
                }
            }

            @Override
            protected void done() {
                log("", "default");
                log("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", "header");
                log("  Done!  Moved: " + movedCount
                        + "  |  Skipped: " + skippedCount, "success");
                log("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", "header");

                statusLabel.setText("Complete — " + movedCount + " files organized");
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                selectButton.setEnabled(true);
                organizeButton.setEnabled(true);
            }
        };

        worker.execute();
    }

    // ──────────────────────────────────────────────
    //  Utility: extract file extension (without dot)
    // ──────────────────────────────────────────────
    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    // ──────────────────────────────────────────────
    //  Entry point
    // ──────────────────────────────────────────────
    public static void main(String[] args) {
        // Apply the system native Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            GuiOrganizer app = new GuiOrganizer();
            app.setVisible(true);
        });
    }
}
