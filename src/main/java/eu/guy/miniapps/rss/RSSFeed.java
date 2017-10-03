package eu.guy.miniapps.rss;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 8/26/2017.
 */
public class RSSFeed {
    private String title;
    private String link;
    private List<RSSItem> items = new ArrayList<>();

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void add(RSSItem item) {
        items.add(item);
    }

    public List<RSSItem> getItems() {
        return items;
    }

    public String toString() {
        return String.format("FEED [%s] %s", title, link);
    }
}
