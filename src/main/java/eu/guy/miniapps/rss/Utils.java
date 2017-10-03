package eu.guy.miniapps.rss;

import java.io.File;
import java.io.FileNotFoundException;
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


}
