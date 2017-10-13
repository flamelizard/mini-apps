package eu.guy.miniapps.rss.persistence;

import eu.guy.miniapps.rss.RSSFeed;
import eu.guy.miniapps.rss.RSSItem;
import eu.guy.miniapps.rss.Utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static eu.guy.miniapps.rss.Utils.safeQuoteSQLLiteral;


public class PersistRSS {
    public static String RSS_DB = "rss";
    public static String FEED_DB = "feed";
    public static String DB_URL = "jdbc:sqlite://D:/projects/miniapps/src/sqlite" +
            "/rss.db";
    final String INSERT_RSS = "insert into " + RSS_DB +
            " values (?, ?, ?, ?, ?, null)";
    private Path folderDDL = Paths.get("D:\\projects\\miniapps\\src\\sqlite");
    private String DRIVER = "org.sqlite.JDBC";
    private Statement stmt;
    private PreparedStatement prepStmt;
    private Date feedCreated;

    public PersistRSS() throws SQLException, ClassNotFoundException {
        setupConnection();
    }

    public static void main(String[] args) throws Exception {
        PersistRSS persist = new PersistRSS();
        persist.recreateTables();
    }

    private void setupConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);  // load DB driver
        Connection conn = DriverManager.getConnection(DB_URL);
        DriverManager.setLogWriter(new PrintWriter(System.err));

        prepStmt = conn.prepareStatement(INSERT_RSS);
        stmt = conn.createStatement();
    }

    public Integer insertRss(RSSItem rss, Integer feeId) throws SQLException {
        prepStmt.setInt(1, feeId);
        prepStmt.setDate(2, feedCreated);
        prepStmt.setString(3, rss.getTitle());
        prepStmt.setString(4, rss.getLink());
        prepStmt.setDate(5, rss.getDate());
        return prepStmt.executeUpdate();
    }

    //    id is set automatically by SQLite, therefore send NULL
    public Integer insertFeed(RSSFeed feed) throws SQLException {
        if (getFeedId(feed) == -1) {
            String sql = String.format(
                    "insert into %s values (NULL, '%s', '%s', '%s')",
                    FEED_DB, feed.getTitle(), feed.getLink(), feed.getCategory());
            return stmt.executeUpdate(sql);
        }
        return 0;
    }

    public Integer getFeedId(RSSFeed feed) throws SQLException {
        ResultSet rs = query(
                "select id from feed where title = " +
                        safeQuoteSQLLiteral(feed.getTitle()));
        if (!rs.next()) {
            return -1;  // id not found
        }
        return rs.getInt(1);
    }

    public ResultSet query(String selectString) throws SQLException {
        System.out.println("[query " + selectString);
        return stmt.executeQuery(selectString);
    }

    public void recreateTables() throws SQLException, FileNotFoundException {
//        no need to delete all rows
        stmt.executeUpdate("drop table if exists " + FEED_DB);
        stmt.executeUpdate("drop table if exists " + RSS_DB);
        stmt.executeUpdate(Utils.getFileContent(
                folderDDL.resolve("feed.sql").toFile()));
        stmt.executeUpdate(Utils.getFileContent(
                folderDDL.resolve("rss.sql").toFile()));
    }

    private boolean isDuplicate(RSSItem item) throws SQLException {
        String select = String.format(
                "SELECT FEED_ID FROM %s WHERE UPPER(TITLE) = UPPER('%s')",
                PersistRSS.RSS_DB, item.getTitle()
        );
        ResultSet rs = query(select);
        boolean hasRow = rs.next();
        rs.close();
        return hasRow;
    }

    public void persistFeed(RSSFeed feed) throws SQLException {
        feedCreated = Date.valueOf(LocalDate.now());
        insertFeed(feed);
        Integer feedId = getFeedId(feed);
        if (feedId == -1)
            throw new SQLException("Feed ID not found for existing feed");

        List<RSSItem> rssItems = feed.getItems();
        if (isDuplicate(rssItems.get(0))) {
            System.out.println("==> No persist - feed saved already");
            return;
        }
        for (RSSItem rss : rssItems) {
            insertRss(rss, feedId);
        }
    }
}
