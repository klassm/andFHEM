/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class CULEMDevice extends Device<CULEMDevice> {

    private String currentUsage;
    private String dayUsage;
    private String monthUsage;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
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
