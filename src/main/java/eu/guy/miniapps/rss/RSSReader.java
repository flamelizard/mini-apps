package eu.guy.miniapps.rss;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Probably does not work code below
Convert from LocalDateTime to legacy Date, took a while to figure out
java.util.Date = Date.from(Instant.from(<LocalDateTime instance>));
*/

public class RSSReader {
    private final String ITEM = "item";
    private final String TITLE = "title";
    private final String LINK = "link";
    private final String AUTHOR = "author";
    private final String DATE = "pubDate";
    private URL url;
    private InputStream in;

    public RSSReader(String url) throws IOException {
        try {
//            Needed to add url SSL certificate through java keytool
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        connectToUrl();
    }

    private void connectToUrl() throws IOException {
        URLConnection conn = this.url.openConnection();
        conn.setConnectTimeout(0);
        conn.setReadTimeout(0);
        in = conn.getInputStream();
    }

    public List<RSSItem> getFeed() throws XMLStreamException {
//        factory pattern for more types of readers
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlReader = inputFactory.createXMLEventReader(in);

        RSSItem item = null;
        List<RSSItem> items = new ArrayList<>();
        while (xmlReader.hasNext()) {
            XMLEvent event = xmlReader.nextEvent();
            if (item == null) {
                item = new RSSItem();
            } else if (event.isEndElement() &&
                    event.asEndElement().getName().getLocalPart().equals(ITEM)) {
//                System.out.println("[feed " + item);
                items.add(item);
                item = null;
            } else if (event.isStartElement()) {
                switch (event.asStartElement().getName().getLocalPart()) {
                    case TITLE:
                        item.setTitle(getValue(xmlReader.nextEvent()));
                        break;
                    case LINK:
                        item.setLink(getValue(xmlReader.nextEvent()));
                        break;
                    case AUTHOR:
                        item.setAuthor(getValue(xmlReader.nextEvent()));
                        break;
                    case DATE:
                        item.setDate(toDate(getValue(xmlReader.nextEvent())));
                        break;
                }
            }
        }
        return items;
    }

    private String getValue(XMLEvent tag) {
        if (!tag.isCharacters())
            return "";
        return trimNewLines(tag.asCharacters().getData());
    }

    private String trimNewLines(String s) {
        Pattern ptrn = Pattern.compile("^\n?(.+?)\n?$", Pattern.DOTALL);
        Matcher match = ptrn.matcher(s);
        if (match.find())
            return match.group(1);
        return s;
    }

    //    LocalDateTime, Instant etc. are modern Date/Time API
//    Date, Calendar are legacy
    private Date toDate(String date) {
        String dateFmt = "EEE, dd MMM yyyy HH:mm:ss zzz";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFmt);
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
        return java.sql.Date.valueOf(dateTime.toLocalDate());
    }
}
