package li.klass.fhem.util;

public class StringEscapeUtils {

    public static String unescapeHtml(String content) {

        content = content.replaceAll("#fc;", "ü");
        content = content.replaceAll("#e4;", "ä");
        content = content.replaceAll("#f6;", "ö");
        content = content.replaceAll("#df;", "ß");

        return content;
    }
}
