package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class CULEMDevice extends Device<CULEMDevice> {

    private String currentUsage;
    private String dayUsage;
    private String monthUsage;

    @Override
    protected void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("CURRENT")) {
            currentUsage = nodeContent + " (kwh)";
        } else if (keyValue.equals("CUM_DAY")) {
            dayUsage = extractCumUsage(nodeContent, "CUM_DAY") + " (kwh)";
        } else if (keyValue.equals("CUM_MONTH")) {
            monthUsage = extractCumUsage(nodeContent, "CUM_MONTH") + " (kwh)";
        }
    }

    public String getCurrentUsage() {
        return currentUsage;
    }

    public String getDayUsage() {
        return dayUsage;
    }

    public String getMonthUsage() {
        return monthUsage;
    }
    
    private String extractCumUsage(String cumString, String cumToken) {
        cumToken = cumToken + ": ";
        return cumString.substring(cumToken.length(), cumString.indexOf(" ", cumToken.length() + 1));
    }
}
