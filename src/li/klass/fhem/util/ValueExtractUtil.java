package li.klass.fhem.util;

public class ValueExtractUtil {
    public static float extractLeadingDouble(String text) {
        text = text.trim();
        int spacePosition = text.indexOf(" ");
        if (spacePosition != -1) {
            text = text.substring(0, spacePosition);
        }

        return Float.valueOf(text);
    }
}
