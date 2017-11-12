package eu.guy.miniapps.rss.persistence;

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
public class Sqlite101 {
    private File rssDDL = new File
            ("D:\\projects\\miniapps\\src\\sqlite\\rss.sql");
    private String driver = "org.sqlite.JDBC";
    private String dbURL = "jdbc:sqlite://D:/projects/miniapps/src/sqlite" +
            "/rss.db";
    private Statement stmt;
    private PreparedStatement prepStmt;

    public static void main(String[] args) throws Exception {
        Sqlite101 utils = new Sqlite101();
        utils.useDb();
    }

    void useDb() throws SQLException, FileNotFoundException {
        try {
//            load and register driver
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Connection conn = DriverManager.getConnection(dbURL);
        DriverManager.setLogWriter(new PrintWriter(System.err));
//        System.out.println(conn.getMetaData().getURL());
        stmt = conn.createStatement();
//        createTable(getFileContent(rssDDL));

        final String INSERT_RSS = "insert into rss values (?, ?, ?, ?)";
        prepStmt = conn.prepareStatement(INSERT_RSS);
        prepStmt.setString(1, "Article title");
        prepStmt.setString(2, "http://link/to/article");
        prepStmt.setDate(3, new Date(System.currentTimeMillis()));
        System.out.println("Rows added " + prepStmt.executeUpdate());

        ResultSet rs = stmt.executeQuery("select * from RSS");
//        System.out.println(rs.getMetaData().getColumnName(1));
        while (rs.next()) {
            System.out.println(rs.getString(1) + "\n" + rs.getDate("date"));
        }
    }

    void createTable(String ddl) throws SQLException {
        System.out.println("Rows updated " + stmt.executeUpdate(ddl));
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
