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
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FS20Device extends Device<FS20Device> implements Comparable<FS20Device>, Serializable {

    private List<Integer> dimStates = Arrays.asList(0, 6, 100, 12, 18, 25, 31, 37, 43, 50, 56, 62, 68, 75, 81, 87, 93);
    private static final List<String> dimModels = Arrays.asList("FS20DI", "FS20DI10", "FS20DU");
    private static final List<String> offStates = Arrays.asList("off", "off-for-timer", "reset", "timer");
    
    private String model;
    private List<String> setOptions = Collections.emptyList();

    public enum FS20State {
        ON, OFF
    }

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("STATE") && tagName.equals("INT")) {
            setState(nodeContent);
        } else if (keyValue.equals("STATE") && tagName.equals("STATE")) {
            Node measured = attributes.getNamedItem("measured");
            if (measured != null) {
                this.measured = measured.getNodeValue();
            }
        } else if (keyValue.equalsIgnoreCase("MODEL")) {
            this.model = nodeContent.toUpperCase();
        } else if (keyValue.equals("DEF")) {
            String[] parts = nodeContent.split(" ");
            if (parts.length == 2 && parts[0].length() == 4 && parts[1].length() == 2) {
                definition = transformHexTo4System(parts[0]) + " " + transformHexTo4System(parts[1]);
            }
        }
    }

    @Override
    protected void onAttributeRead(String attributeKey, String attributeValue) {
        super.onAttributeRead(attributeKey, attributeValue);

        if (attributeKey.equals("SETS")) {
            setOptions = Arrays.asList(attributeValue.split(" "));
            Collections.sort(setOptions);
        }
    }

    public boolean isOn() {
        return getFs20State() == FS20State.ON;
    }

    public boolean isDimDevice() {
        return dimModels.contains(model);
    }
    
    public int getBestDimMatchFor(int dimProgress) {
        int bestMatch = -1;
        int smallestDiff = 100;
        for (Integer dimState : dimStates) {
            int diff = dimProgress - dimState;
            if (diff < 0) diff *= -1;

            if (bestMatch == -1 || diff < smallestDiff ) {
                bestMatch = dimState;
                smallestDiff = diff;
            }
        }

        return bestMatch;
    }

    public FS20State getFs20State() {
        for (String offState : offStates) {
            if (getInternalState().equals(offState) || getInternalState().equals(eventMap.get(offState))) {
                return FS20State.OFF;
            }
        }
        return FS20State.ON;
    }

    public int getFS20DimState() {
        if (getFs20State() == FS20State.OFF) {
            return 0;
        }
        
        String internalState = getInternalState();
        
        if (internalState.startsWith("dim") && internalState.endsWith("%")) {
            String dimProgress = internalState.substring("dim".length(), internalState.length() - 1);
            return Integer.valueOf(dimProgress);
        }

        return 100;
    }

    public List<String> getSetOptions() {
        return setOptions;
    }
    
    private String transformHexTo4System(String input) {
        int value = Integer.decode("0x" + input);
        StringBuilder result = new StringBuilder();
        do {
            value = value  / 4;
            result.append((value % 4) + 1);
        } while (value > 0);

        while (result.length() < 4) {
            result.insert(0, "1");
        }
        return result.reverse().toString();
    }
}
