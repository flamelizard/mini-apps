package eu.guy.miniapps.rss;

import eu.guy.miniapps.rss.persistence.PersistRSS;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

import static eu.guy.miniapps.rss.Utils.*;

/**
 * Created by Tom on 8/6/2017.
 */
/*
TODO
add JPA/Hibernate access to db
very basic unit test with a mock maybe, junit scheduler
--> FREEZE <--
NO MORE FEATURES

SOMEDAY MIGHT FIX
RSS date is saved with time info which is never used, refactor date related
code to a class

DONE
scheduler, collect feeds at regular interval
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
    private Queue queue = new LinkedBlockingQueue();
    private boolean threadFailed = false;

    public Aggregator() throws SQLException, ClassNotFoundException {
        persistence = new PersistRSS();
    }

    public static void main(String[] args) throws Exception {
        Aggregator hub = new Aggregator();
        hub.subscribe("https://www.pproot.cz/rss/clanky/", "IT");
//        hub.subscribe("https://www.linux.com/feeds/tutorials/rss");
//        hub.subscribe("https://www.zdrojak.cz/feed/", "IT");
//        hub.aggregate(true);
//        hub.displayLatestRSS();
//        hub.searchRSSByKeyword("java");
//        hub.searchRSSByDate("2017-09-18");
        hub.runScheduler(15, 5);
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

    public void runScheduler(Integer period, Integer stopAfter) {
        ScheduledExecutorService scheduler = Executors.
                newSingleThreadScheduledExecutor();
//        fixed delay respects thread runtime
        ScheduledFuture tracker = scheduler.scheduleWithFixedDelay(
                new AggregateThread(this), 0, period, TimeUnit.SECONDS);
        scheduler.schedule(new Stopper(scheduler), stopAfter, TimeUnit.SECONDS);
        while (!tracker.isDone()) {
            if (isThreadFailed()) {
                System.out.println("Scheduler shutting down...");
                scheduler.shutdown();
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void setThreadFailed() {
        this.threadFailed = true;
    }

    public boolean isThreadFailed() {
        return threadFailed;
    }

    //    Runnable runs in own thread and exception will not propagate to main tr.
    class AggregateThread implements Runnable {
        private Aggregator aggregator;

        AggregateThread(Aggregator aggregator) {
            this.aggregator = aggregator;
        }

        @Override
        public void run() {
            try {
                System.out.println("thread [" + LocalDateTime.now() + "]");
                aggregator.aggregate(true);
            } catch (Exception e) {
                e.printStackTrace();
                setThreadFailed();
            }
        }
    }

    class Stopper implements Callable {
        private ScheduledExecutorService scheduler;

        Stopper(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public Object call() throws Exception {
            System.out.println("Scheduled time reached. Stop...");
            scheduler.shutdown();
            return null;
        }
    }
}
