package li.klass.fhem.domain.log;

import java.util.regex.Pattern;

import li.klass.fhem.update.backend.xmllist.XmlListDevice;

public class ConcernsDevicePredicate {

    private final Pattern concerningDeviceRegexp;

    private ConcernsDevicePredicate(String concerningDeviceRegexp) {
        this.concerningDeviceRegexp = Pattern.compile(concerningDeviceRegexp);
    }

    public boolean apply(XmlListDevice xmlListDevice) {
        if (xmlListDevice == null) {
            return false;
        }
        String name = xmlListDevice.getName();
        if (".".equals(name)) {
            name = ".*";
        }
        return concerningDeviceRegexp.matcher(name).matches();
    }

    public static ConcernsDevicePredicate forPattern(String regexpAttributeFromLogDevice) {
        return new ConcernsDevicePredicate(extractConcerningDeviceRegexpFromDefinition(regexpAttributeFromLogDevice));
    }

    /**
     * We extract the device names from the current log regexp. As the regexp always concerns
     * device name and reading, we have to skip the reading.
     * <p/>
     * The default format is <i>deviceName:reading </i>, so we have to skip the reading part and
     * the colon. In addition, we have to make sure that we can still write regexp style expressions,
     * including OR expressions on different levels.
     *
     * @param definition regexp definition for the log device (including device events).
     * @return regexp definition for the log device names.
     */
    static String extractConcerningDeviceRegexpFromDefinition(String definition) {
        definition = definition.replaceAll(":\\|", "\\|");

        boolean isName = true;
        StringBuilder out = new StringBuilder();
        boolean firstCharFound = false;
        int level = 0;
        int baseLevel = 0;

        for (int i = 0; i < definition.length(); i++) {

            char c = definition.charAt(i);

            if (c == '(' || c == ')') {
                if (c == '(') level++;
                if (c == ')') level--;
            }

            if (!firstCharFound && c != '(') {
                baseLevel = level;
                firstCharFound = true;
            }

            if (!firstCharFound || c == '(' || c == ')') continue;

            if (level <= baseLevel) {
                if (isName && c != '|' && c != ':') {
                    out.append(c);
                    continue;
                }

                if (c == ':' && isName) {
                    isName = false;
                    continue;
                }

                if (c == '|' && !isName) {
                    isName = true;
                }

                if (c == '|') {
                    out.append('|');
                }
            } else if (isName) {
                out.append(c);
            }
        }

        String result = out.toString();
        return result
                .replaceAll("\\.\\|", ".*|")
                .replaceAll("\\.$", ".*");
    }
}
