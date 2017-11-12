package eu.guy.miniapps.rss;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Utils {

    public static void main(String[] args) {
    }

    public static String getFileContent(File filePath) throws
            FileNotFoundException {
        StringBuilder content = new StringBuilder();
        Scanner scanner = new Scanner(filePath);
        while (scanner.hasNext()) {
            content.append(scanner.nextLine()).append("\n");
        }
        return content.toString();
    }

    public static String safeQuoteSQLLiteral(String s) {
//        negative lookahead
        Pattern ptrn = Pattern.compile("'(?!'|$)");
        return "'" + ptrn.matcher(s).replaceAll("''") + "'";
    }

    public static Long getEpochMillis(LocalDate date) {
        LocalDateTime dt = date.atTime(0, 0);
        ZonedDateTime zdt = dt.atZone(ZoneId.systemDefault());
        return Instant.from(zdt).toEpochMilli();
    }

    public static void printRSSItems(ResultSet items) throws SQLException {
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
}
