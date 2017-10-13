package eu.guy.miniapps.rss;

import eu.guy.miniapps.rss.persistence.PersistRSS;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static eu.guy.miniapps.rss.Utils.safeQuoteSQLLiteral;

/**
 * Created by Tom on 8/6/2017.
 */
/*
Start 6.8, building simple app

Learnt

== Class over primitives - Big one !!
Passing classes between methods gives flexibility when additional data is
necessary.
If I pass individual arguments, any change to method signature requires to
update code in many places. Passing classes around eliminates this problem
since a class encapsulates individual pieces of information and any change
will occur only inside the class.

TODO
categorize feeds - it, social, fun (practice sql table design)
timed check for updates - run for an hour, show new content, highlight
add JPA/Hibernate access to db
very basic unit test with a mock maybe
--> FREEZE <--
NO MORE FEATURES SO I CAN DO SOMETHING ELSE

SOMEDAY MIGHT FIX
RSS date is saved with time info which is never used, refactor date related
code to a class

DONE
search by date
search by keyword
do not save duplicate RSS item
display latest feed
handle unreachable sites, https://nova.cz timeouts

https://www.linux.com/feeds/tutorials/rss
https://www.root.cz/rss/clanky/
http://www.economist.com/sections/business-finance/rss.xml - HTTP error?

Got ya!!
Feed pubData set only on root.cz. Other feeds have date only on messages alone.
 */
public class Aggregator {
    private PersistRSS persistence;
    private List<RSSUrl> feeds = new ArrayList<>();

    public Aggregator() throws SQLException, ClassNotFoundException {
        persistence = new PersistRSS();
    }

    public static void main(String[] args) throws Exception {
        Aggregator hub = new Aggregator();
        hub.subscribe("https://www.root.cz/rss/clanky/", "IT");
//        hub.subscribe("https://www.linux.com/feeds/tutorials/rss");
//        hub.subscribe("https://www.zdrojak.cz/feed/", "IT");
//        hub.aggregate(true);
//        hub.displayLatestRSS();
//        hub.searchRSSByKeyword("java");
//        hub.searchRSSByDate("2017-09-18");
        hub.pollFeed(10, 60);
    }

    public void aggregate(boolean persistFeeds) throws
            IOException, XMLStreamException, SQLException {
        System.out.println("Gathering RSS feeds... ");
        for (RSSUrl url : feeds) {
            RSSReader reader = new RSSReader(url.getUrl());
            RSSFeed feed = reader.getFeed();
            feed.setCategory(url.getCategory());

            System.out.println("Persisting to database... ");
            if (persistFeeds) {
                persistence.persistFeed(feed);
            } else {
                System.out.println("/\\/\\ Skipped");
            }
        }
    }

    public void subscribe(String url, String category) {
        RSSUrl rssUrl = new RSSUrl(url);
        rssUrl.setCategory(category);
        feeds.add(rssUrl);
    }

    public void displayLatestRSS() throws SQLException {
        System.out.println("Printing latest RSS ... ");
        ResultSet rs;
        rs = persistence.query("SELECT MAX(CREATED) FROM " + PersistRSS.RSS_DB);
        rs.next();
//        Date in SQLite saved as string or integer
        rs = persistence.query(String.format(
                "SELECT * FROM %s WHERE CREATED = %s",
                PersistRSS.RSS_DB, rs.getString(1))
        );
        printRSSItems(rs);
        rs.close();
    }

    public void searchRSSByKeyword(String word) throws SQLException {
        System.out.println("Search by keyword ... " + word);
        String select = String.format(
                "SELECT * FROM %s WHERE UPPER(TITLE) LIKE %s",
                PersistRSS.RSS_DB,
                safeQuoteSQLLiteral("%" + word.toUpperCase() + "%"));
        printRSSItems(persistence.query(select));
    }

    public void searchRSSByDate(String dateIsoFormat) throws SQLException {
        System.out.println("Search by date ... " + dateIsoFormat);
        LocalDate date = DateTimeFormatter.ISO_LOCAL_DATE.
                parse(dateIsoFormat, LocalDate::from);
        String select = String.format(
                "SELECT * FROM %s WHERE PUBLISHED >= %s",
                PersistRSS.RSS_DB, getEpochMillis(date)
        );
        printRSSItems(persistence.query(select));
    }

    private Long getEpochMillis(LocalDate date) {
        LocalDateTime dt = date.atTime(0, 0);
        ZonedDateTime zdt = dt.atZone(ZoneId.systemDefault());
        return Instant.from(zdt).toEpochMilli();
    }

    public void printRSSItems(ResultSet items) throws SQLException {
        while (items.next()) {
            RSSItem item = new RSSItem();
            item.setTitle(items.getString(3));
            item.setLink(items.getString(4));
            item.setDate(items.getDate(5));
            item.setAuthor(items.getString(6));
            System.out.println(item);
        }
        items.close();
    }

    //    TODO finish off
    public void pollFeed(Integer period, Integer stopAfter) throws
            XMLStreamException, IOException, SQLException, InterruptedException {
        System.out.println(
                String.format("Polling feed [every %ds over %ds] ... ",
                        period, stopAfter));
        Integer sumTime = 0;
        period = period * 1000;
        stopAfter = stopAfter * 1000;   // -1 to run indefinitely
        while (stopAfter < 0 || sumTime < stopAfter) {
            aggregate(true);
            Thread.sleep(period);
            sumTime += period;
        }
    }
}
