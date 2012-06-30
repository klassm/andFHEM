/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.domain;

import android.util.Log;
import org.w3c.dom.NamedNodeMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtDevice extends Device<AtDevice> {

    public static final Pattern FHEM_PATTERN = Pattern.compile("fhem\\('set ([\\w-]+) ([\\w-]+)(?: ([0-9:]+))?'\\)(.*)");
    public static final Pattern PREFIX_PATTERN = Pattern.compile("([+*]{0,2})([0-9:]+)(.*)");
    public static final Pattern DEFAULT_PATTERN = Pattern.compile("set ([\\w-]+) ([\\w-]+)(?: ([0-9:]+))?");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private int hours;
    private int minutes;
    private int seconds;

    public enum AtRepetition {
        ONCE, EVERY_DAY, WEEKEND, NOT_WEEKEND

    }
    public enum TimerType {
        RELATIVE, ABSOLUTE

    }
    private String targetDevice;
    private String targetState;
    private String targetStateAddtionalInformation;
    private AtRepetition repetition = AtRepetition.ONCE;
    private TimerType timerType = TimerType.ABSOLUTE;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equalsIgnoreCase("DEF")) {
            definition = parseDefinition(nodeContent) ? nodeContent : "";
        }
    }

    private boolean parseDefinition(String nodeContent) {
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(nodeContent);

        if (! prefixMatcher.matches()) return false;

        String prefix = prefixMatcher.group(1);
        handlePrefix(prefix);

        String dateContent = prefixMatcher.group(2);
        parseDateContent(dateContent);

        String rest = prefixMatcher.group(3).trim();

        return parseDeviceSwitchContent(rest);
    }

    private boolean parseDeviceSwitchContent(String rest) {
        if (rest.startsWith("fhem")) {
            Matcher fhemMatcher = FHEM_PATTERN.matcher(rest);

            if (! fhemMatcher.matches()) return false;

            targetDevice = fhemMatcher.group(1);
            targetState = fhemMatcher.group(2);
            targetStateAddtionalInformation = fhemMatcher.group(3);

            String fhemRest = fhemMatcher.group(4).trim();
            if (fhemRest.matches("if[ ]?\\(\\$we\\)")) {
                repetition = AtRepetition.WEEKEND;
            } else if (fhemRest.matches("if[ ]?\\((NOT|not|!)[ ]?\\$we\\)")) {
                repetition = AtRepetition.NOT_WEEKEND;
            }
        } else {
            Matcher matcher = DEFAULT_PATTERN.matcher(rest);

            if (! matcher.matches()) return false;

            targetDevice = matcher.group(1);
            targetState = matcher.group(2);
            targetStateAddtionalInformation = matcher.group(3);
        }
        return true;
    }

    private void parseDateContent(String dateContent) {
        try {
            Date date = dateFormat.parse(dateContent);
            hours = date.getHours();
            minutes = date.getMinutes();
            seconds = date.getSeconds();

        } catch (ParseException e) {
            Log.e(AtDevice.class.getName(), "cannot parse dateContent " + dateContent);
        }
    }

    private void handlePrefix(String prefix) {
        if (prefix.contains("+")) {
            timerType = TimerType.RELATIVE;
        }

        if (prefix.contains("*")) {
            repetition = AtRepetition.EVERY_DAY;
        }
    }

    @Override
    public boolean isSupported() {
        return definition != null;
    }

    @Override
    public String toString() {
        return "AtDevice{" +
                "definition=" + definition +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                ", targetDevice='" + targetDevice + '\'' +
                ", targetState='" + targetState + '\'' +
                ", targetStateAddtionalInformation='" + targetStateAddtionalInformation + '\'' +
                ", repetition=" + repetition +
                ", timerType=" + timerType +
                '}';
    }

    public static void main(String[] args) {

        parse("17:00:00 set lamp on");
        parse("*23:00:00 fhem('set lamp off') if ($we)");
        parse("+*23:00:00 fhem('set lamp off-for-timer 200') if (not $we)");
        parse("*23:00:00 fhem('set lamp off-for-timer 200') if(NOT $we)");
        parse("*23:00:00 fhem('set lamp off-for-timer 200') if (!$we)");
    }

    public static void parse(String def) {
        AtDevice device = new AtDevice();
        device.parseDefinition(def);
        System.out.println(device.toString());
    }
}
