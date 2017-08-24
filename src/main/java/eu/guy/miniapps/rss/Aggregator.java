package eu.guy.miniapps.rss;

import eu.guy.miniapps.rss.persistence.PersistRSS;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 8/6/2017.
 */
/*
Start 6.8, building simple app

Learnt

TODO
items - feed info, message, link in db
do not save duplicate RSS item
handle unreachable sites - done but does not work
show news for a particular day/author/keyword
add JPA/Hibernate access to db
categorize feeds - it, social, fun (practice sql table design)
 */
public class Aggregator {
    private final PersistRSS persistence;
    private List<String> feeds = new ArrayList<>();

    public Aggregator() throws SQLException, ClassNotFoundException {
        persistence = new PersistRSS();
    }

    public static void main(String[] args) throws Exception {
        Aggregator hub = new Aggregator();
//        hub.subscribe("https://www.root.cz/rss/clanky/");
        hub.subscribe("file://localhost/c:/temp/root_rss.xml");
        hub.aggregate(false);
        hub.displayFeedAll();
    }

    public void aggregate(boolean persistFeeds) throws
            IOException, XMLStreamException, SQLException {
        System.out.println("Gathering RSS feeds... ");
        for (String feedUrl : feeds) {
            RSSReader reader = new RSSReader(feedUrl);
            List<RSSItem> rssItems = reader.getFeed();

            if (persistFeeds) {
                System.out.println("Persisting to database... ");
                for (RSSItem rss : rssItems) {
                    persist(rss);
                }
            } else {
                System.out.println("/\\/\\ Skipped");
            }
        }
    }

    public void subscribe(String url) {
        feeds.add(url);
    }

    public void displayFeed(Integer id) {

    }

    public void displayFeedAll() throws SQLException {
        System.out.println("Printing RSS feeds... ");
        ResultSet rs = persistence.query("select * from " + PersistRSS.DB_NAME);
        while (rs.next()) {
            RSSItem item = new RSSItem();
            item.setTitle(rs.getString(2));
            item.setLink(rs.getString(3));
            item.setDate(rs.getDate(4));
            item.setAuthor(rs.getString(5));
            System.out.println(item);
        }
    }

    private void checkForUpdates() {

    }

    public void persist(RSSItem rss) throws SQLException {
        persistence.insertRss(rss);
    }
}
