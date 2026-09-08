import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a persistent SQLite database of all organize operations.
 * Each organize run is stored as a "session" containing individual file moves.
 * This allows undoing any past session, even after closing and reopening the app.
 */
public class HistoryManager {

    private static final String DB_NAME = "organizer_history.db";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String dbUrl;

    public HistoryManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[HistoryManager] SQLite JDBC Driver not found: " + e.getMessage());
        }
        // Store the DB next to the running jar/class files
        Path dbPath = Paths.get(System.getProperty("user.dir"), DB_NAME);
        this.dbUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        initDatabase();
    }

    // ── Schema ────────────────────────────────────────────────────────

    private void initDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS sessions ("
                    + "  id           INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "  timestamp    TEXT    NOT NULL,"
                    + "  target_dir   TEXT    NOT NULL,"
                    + "  files_moved  INTEGER NOT NULL DEFAULT 0,"
                    + "  files_skipped INTEGER NOT NULL DEFAULT 0,"
                    + "  undone       INTEGER NOT NULL DEFAULT 0"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS file_moves ("
                    + "  id           INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "  session_id   INTEGER NOT NULL,"
                    + "  original_path TEXT   NOT NULL,"
                    + "  moved_path   TEXT    NOT NULL,"
                    + "  category     TEXT    NOT NULL,"
                    + "  file_name    TEXT    NOT NULL,"
                    + "  FOREIGN KEY(session_id) REFERENCES sessions(id)"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS created_folders ("
                    + "  id           INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "  session_id   INTEGER NOT NULL,"
                    + "  folder_path  TEXT    NOT NULL,"
                    + "  FOREIGN KEY(session_id) REFERENCES sessions(id)"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS content_rules ("
                    + "  id           INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "  folder_name  TEXT    NOT NULL,"
                    + "  keywords     TEXT    NOT NULL"
                    + ")");

        } catch (SQLException e) {
            System.err.println("[HistoryManager] Failed to initialize database: " + e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    // ── Write operations ──────────────────────────────────────────────

    /**
     * Creates a new session and returns its ID.
     */
    public int startSession(String targetDir) {
        String sql = "INSERT INTO sessions(timestamp, target_dir) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, LocalDateTime.now().format(FMT));
            ps.setString(2, targetDir);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[HistoryManager] startSession error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Records a single file move within a session.
     */
    public void recordMove(int sessionId, Path originalPath, Path movedPath, String category) {
        String sql = "INSERT INTO file_moves(session_id, original_path, moved_path, category, file_name) "
                + "VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setString(2, originalPath.toAbsolutePath().toString());
            ps.setString(3, movedPath.toAbsolutePath().toString());
            ps.setString(4, category);
            ps.setString(5, originalPath.getFileName().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] recordMove error: " + e.getMessage());
        }
    }

    /**
     * Records a created category folder within a session.
     */
    public void recordCreatedFolder(int sessionId, Path folderPath) {
        String sql = "INSERT INTO created_folders(session_id, folder_path) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setString(2, folderPath.toAbsolutePath().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] recordCreatedFolder error: " + e.getMessage());
        }
    }

    /**
     * Finalizes a session with the count of moved and skipped files.
     */
    public void finalizeSession(int sessionId, int moved, int skipped) {
        String sql = "UPDATE sessions SET files_moved = ?, files_skipped = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, moved);
            ps.setInt(2, skipped);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] finalizeSession error: " + e.getMessage());
        }
    }

    /**
     * Marks a session as undone.
     */
    public void markUndone(int sessionId) {
        String sql = "UPDATE sessions SET undone = 1 WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] markUndone error: " + e.getMessage());
        }
    }

    // ── Read operations ───────────────────────────────────────────────

    /**
     * Returns all sessions (most recent first).
     */
    public List<SessionRecord> getAllSessions() {
        List<SessionRecord> list = new ArrayList<>();
        String sql = "SELECT id, timestamp, target_dir, files_moved, files_skipped, undone "
                + "FROM sessions ORDER BY id DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new SessionRecord(
                        rs.getInt("id"),
                        rs.getString("timestamp"),
                        rs.getString("target_dir"),
                        rs.getInt("files_moved"),
                        rs.getInt("files_skipped"),
                        rs.getInt("undone") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("[HistoryManager] getAllSessions error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all file moves for a given session.
     */
    public List<MoveRecord> getMovesForSession(int sessionId) {
        List<MoveRecord> list = new ArrayList<>();
        String sql = "SELECT original_path, moved_path, category, file_name "
                + "FROM file_moves WHERE session_id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MoveRecord(
                            rs.getString("original_path"),
                            rs.getString("moved_path"),
                            rs.getString("category"),
                            rs.getString("file_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[HistoryManager] getMovesForSession error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all created folders for a given session.
     */
    public List<String> getCreatedFoldersForSession(int sessionId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT folder_path FROM created_folders WHERE session_id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("folder_path"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[HistoryManager] getCreatedFoldersForSession error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Deletes a session and its related records.
     */
    public void deleteSession(int sessionId) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(
                         "DELETE FROM file_moves WHERE session_id = ?");
                 PreparedStatement ps2 = conn.prepareStatement(
                         "DELETE FROM created_folders WHERE session_id = ?");
                 PreparedStatement ps3 = conn.prepareStatement(
                         "DELETE FROM sessions WHERE id = ?")) {

                ps1.setInt(1, sessionId);
                ps1.executeUpdate();
                ps2.setInt(1, sessionId);
                ps2.executeUpdate();
                ps3.setInt(1, sessionId);
                ps3.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[HistoryManager] deleteSession error: " + e.getMessage());
        }
    }

    // ── Content rule operations ────────────────────────────────────────

    /**
     * Adds a new content-matching rule.
     */
    public void addRule(String folderName, String keywords) {
        String sql = "INSERT INTO content_rules(folder_name, keywords) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, folderName);
            ps.setString(2, keywords);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] addRule error: " + e.getMessage());
        }
    }

    /**
     * Updates an existing content rule.
     */
    public void updateRule(int ruleId, String folderName, String keywords) {
        String sql = "UPDATE content_rules SET folder_name = ?, keywords = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, folderName);
            ps.setString(2, keywords);
            ps.setInt(3, ruleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] updateRule error: " + e.getMessage());
        }
    }

    /**
     * Deletes a content rule by ID.
     */
    public void deleteRule(int ruleId) {
        String sql = "DELETE FROM content_rules WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ruleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[HistoryManager] deleteRule error: " + e.getMessage());
        }
    }

    /**
     * Returns all content rules.
     */
    public List<ContentScanner.ContentRule> getAllRules() {
        List<ContentScanner.ContentRule> list = new ArrayList<>();
        String sql = "SELECT id, folder_name, keywords FROM content_rules ORDER BY id";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new ContentScanner.ContentRule(
                        rs.getInt("id"),
                        rs.getString("folder_name"),
                        rs.getString("keywords")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[HistoryManager] getAllRules error: " + e.getMessage());
        }
        return list;
    }

    // ── Data records ──────────────────────────────────────────────────

    public static class SessionRecord {
        public final int    id;
        public final String timestamp;
        public final String targetDir;
        public final int    filesMoved;
        public final int    filesSkipped;
        public final boolean undone;

        public SessionRecord(int id, String timestamp, String targetDir,
                             int filesMoved, int filesSkipped, boolean undone) {
            this.id           = id;
            this.timestamp    = timestamp;
            this.targetDir    = targetDir;
            this.filesMoved   = filesMoved;
            this.filesSkipped = filesSkipped;
            this.undone       = undone;
        }
    }

    public static class MoveRecord {
        public final String originalPath;
        public final String movedPath;
        public final String category;
        public final String fileName;

        public MoveRecord(String originalPath, String movedPath,
                          String category, String fileName) {
            this.originalPath = originalPath;
            this.movedPath    = movedPath;
            this.category     = category;
            this.fileName     = fileName;
        }
    }
}
