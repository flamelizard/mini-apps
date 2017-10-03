package eu.guy.miniapps.rss;


import java.sql.Date;

public class RSSItem {
    private String title;
    private String link;
    private String author;
    private Date date;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

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
        return String.format("[%s]%n%s%n%s, %s%n", title, link, date, author);
    }
}
