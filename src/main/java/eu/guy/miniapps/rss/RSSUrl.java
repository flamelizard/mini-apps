package eu.guy.miniapps.rss;

/**
 * Created by Tom on 10/3/2017.
 */
public class RSSUrl {
    private String url;
    private String category;

    public RSSUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
