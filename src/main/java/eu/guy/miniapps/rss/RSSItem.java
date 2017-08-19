package eu.guy.miniapps.rss;

/**
 * Created by Tom on 8/8/2017.
 */
public class RSSItem {
    private String title;
    private String link;
    private String author;
    private String date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String toString() {
        return String.format("[%s]%n%s%n%s-%s%n", title, link, date, author);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
