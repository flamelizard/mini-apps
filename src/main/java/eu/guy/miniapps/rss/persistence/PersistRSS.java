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

TODO
if not exists create table?

 */
public class PersistRSS {
    public static String DB_NAME = "rss";
    public static String DB_URL = "jdbc:sqlite://D:/projects/miniapps/src/sqlite" +
            "/rss.db";
    final String INSERT_RSS = "insert into " + DB_NAME + " " +
            "values (?, ?, null, null)";
    private File RSS_DDL = new File
            ("D:\\projects\\miniapps\\src\\sqlite\\rss.sql");
    private String DRIVER = "org.sqlite.JDBC";
    private Statement stmt;
    private PreparedStatement prepStmt;
    private Connection conn;

    public PersistRSS() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        prepareDb();
    }

    public static void main(String[] args) throws Exception {
        PersistRSS utils = new PersistRSS();
    }

    void prepareDb() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        DriverManager.setLogWriter(new PrintWriter(System.err));
        prepStmt = conn.prepareStatement(INSERT_RSS);
        stmt = conn.createStatement();
    }

    public Integer insertRss(RSSItem rss) throws SQLException {
        prepStmt.setString(1, rss.getTitle());
        prepStmt.setString(2, rss.getLink());
        return prepStmt.executeUpdate();
    }

    public ResultSet query(String selectString) throws SQLException {
        return stmt.executeQuery(selectString);
    }

    String getFileContent(File filePath) throws FileNotFoundException {
        StringBuilder content = new StringBuilder();
        Scanner scanner = new Scanner(filePath);
        while (scanner.hasNext()) {
            content.append(scanner.nextLine() + "\n");
        }
        return content.toString();
    }
}
