package eu.guy.miniapps.rss.persistence;

import eu.guy.miniapps.rss.RSSItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Scanner;

/*
SQLite will create db file upon access if it does not exist
Connection string "jdbc:sqlite://path/to/file"
Prepared statement - compiled and parsed only once, fast for many similar
queries
 */
public class PersistRSS {
    public static String DB_NAME = "rss";
    public static String DB_URL = "jdbc:sqlite://D:/projects/miniapps/src/sqlite" +
            "/rss.db";
    private static File RSS_DDL = new File
            ("D:\\projects\\miniapps\\src\\sqlite\\rss.sql");
    final String INSERT_RSS = "insert into " + DB_NAME + " " +
            "values (?, ?, ?, ?, null)";
    private String DRIVER = "org.sqlite.JDBC";
    private Statement stmt;
    private PreparedStatement prepStmt;
    private Connection conn;
    private Integer nextRowId;

    public PersistRSS() throws SQLException, ClassNotFoundException {
        setupConnection();
    }

    public static void main(String[] args) throws Exception {
//        PersistRSS persist = new PersistRSS();
        PersistRSS._createTable();
    }

    static String getFileContent(File filePath) throws
            FileNotFoundException {
        StringBuilder content = new StringBuilder();
        Scanner scanner = new Scanner(filePath);
        while (scanner.hasNext()) {
            content.append(scanner.nextLine() + "\n");
        }
        return content.toString();
    }

    static void _createTable() throws FileNotFoundException,
            SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(getFileContent(RSS_DDL));
    }

    private void setupConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);  // load DB driver
        conn = DriverManager.getConnection(DB_URL);
        DriverManager.setLogWriter(new PrintWriter(System.err));

        prepStmt = conn.prepareStatement(INSERT_RSS);
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select max(id) from " + DB_NAME);
        if (rs.next()) {
            nextRowId = rs.getInt(1);
        } else {
            nextRowId = 1;
        }
    }

    public Integer insertRss(RSSItem rss) throws SQLException {
        prepStmt.setInt(1, nextRowId++);
        prepStmt.setString(2, rss.getTitle());
        prepStmt.setString(3, rss.getLink());
        prepStmt.setDate(4, (java.sql.Date) rss.getDate());
        return prepStmt.executeUpdate();
    }

    public ResultSet query(String selectString) throws SQLException {
        return stmt.executeQuery(selectString);
    }
}
