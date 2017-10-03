package eu.guy.miniapps.rss;

import jdk.internal.org.xml.sax.Attributes;
import jdk.internal.org.xml.sax.SAXException;
import jdk.internal.org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            System.out.println("Getting URL " + url + "...");
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        connectToUrl();

    }

    private void connectToUrl() throws IOException {
        URLConnection conn = this.url.openConnection();
        conn.setConnectTimeout(5000); // default is infinite, 0
        in = conn.getInputStream();
    }

    public RSSFeed getFeed() throws XMLStreamException {
//        factory pattern for more types of readers
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlReader = inputFactory.createXMLEventReader(in);

        RSSItem item = new RSSItem();
        RSSFeed feed = new RSSFeed();
        boolean isFeedHeader = true;
        while (xmlReader.hasNext()) {
            XMLEvent event = xmlReader.nextEvent();
            if (event.isEndElement() &&
                    event.asEndElement().getName().getLocalPart().equals(ITEM)) {
//                System.out.println("[feed " + item);
                feed.add(item);
                item = new RSSItem();
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
                    case ITEM:
//                        Ugly way to parse out feed info
                        if (isFeedHeader) {
                            feed.setTitle(item.getTitle());
                            feed.setLink(item.getLink());
                            isFeedHeader = false;
                            item = new RSSItem();
                        }
                }
            }
        }
        return feed;
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

    private Date toDate(String date) {
//        String dateFmt = "EEE, dd MMM yyyy HH:mm:ss zzz";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFmt);
        LocalDateTime dateTime = LocalDateTime.parse(
                date, DateTimeFormatter.RFC_1123_DATE_TIME);
        return Date.valueOf(dateTime.toLocalDate());
    }

    //    SAX vs StAX
//    SAX has less flexibility
    class FeedHandler extends DefaultHandler {
        private RSSFeed feed = new RSSFeed();
        private boolean title = false;

        @Override
        public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
            if (s2.equalsIgnoreCase("title"))
                title = true;
        }

        @Override
        public void characters(char[] chars, int i, int i1) throws SAXException {
            if (title)
                feed.setTitle(String.valueOf(chars));
        }
    }
}
