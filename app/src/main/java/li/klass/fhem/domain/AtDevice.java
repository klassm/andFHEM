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

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.util.StringUtil;

import static li.klass.fhem.util.NumberUtil.toTwoDecimalDigits;

public class AtDevice extends Device<AtDevice> {

    public static final Pattern FHEM_PATTERN = Pattern.compile("fhem\\(\"set ([\\w\\-,\\\\.]+) ([\\w%-]+)(?: ([0-9.:]+))?\"\\)(.*)");
    public static final Pattern PREFIX_PATTERN = Pattern.compile("([+*]{0,2})([0-9:]+)(.*)");
    public static final Pattern DEFAULT_PATTERN = Pattern.compile("set ([\\w-]+) ([\\w\\-,%]+)(?: ([0-9:]+))?");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");

    private int hours;
    private int minutes;
    private int seconds;
    private String nextTrigger;
    private boolean isActive = true;
    private String targetDevice;
    private String targetState;
    private String targetStateAddtionalInformation;
    private AtRepetition repetition = AtRepetition.ONCE;
    private TimerType timerType = TimerType.ABSOLUTE;

    @Override
    public void readDEF(String value) {
        super.readDEF(value);
        definition = parseDefinition(value) ? value : "";
    }

    boolean parseDefinition(String nodeContent) {
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(nodeContent);

        if (!prefixMatcher.matches()) return false;

        String prefix = prefixMatcher.group(1);
        handlePrefix(prefix);

        String dateContent = prefixMatcher.group(2);
        parseDateContent(dateContent);

        String rest = prefixMatcher.group(3).trim();

        return parseDeviceSwitchContent(rest);
    }

    private void handlePrefix(String prefix) {
        if (prefix.contains("+")) {
            timerType = TimerType.RELATIVE;
        }

        if (prefix.contains("*")) {
            repetition = AtRepetition.EVERY_DAY;
        }
    }

    private void parseDateContent(String dateContent) {
        if (dateContent.length() < "00:00:00".length()) {
            dateContent += ":00";
        }
        try {
            DateTime date = DATE_TIME_FORMAT.parseDateTime(dateContent);
            hours = date.getHourOfDay();
            minutes = date.getMinuteOfHour();
            seconds = date.getSecondOfMinute();

        } catch (Exception e) {
            Log.e(AtDevice.class.getName(), "cannot parse dateContent " + dateContent);
        }
    }

    private boolean parseDeviceSwitchContent(String rest) {
        rest = rest.replaceAll("[{}]", "").trim();
        if (rest.startsWith("fhem")) {
            Matcher fhemMatcher = FHEM_PATTERN.matcher(rest);

            if (!fhemMatcher.matches()) return false;

            targetDevice = fhemMatcher.group(1);
            targetState = fhemMatcher.group(2);
            targetStateAddtionalInformation = fhemMatcher.group(3);

            String fhemRest = fhemMatcher.group(4).trim().toLowerCase(Locale.getDefault());
            Pattern ifPattern = Pattern.compile("if[ ]?\\(([^\\)]+)\\)");
            Matcher ifMatcher = ifPattern.matcher(fhemRest);

            if (ifMatcher.find()) {
                String ifContent = ifMatcher.group(1);
                String[] parts = ifContent.split("&&");

                for (String part : parts) {
                    part = part.trim();
                    handleIfPart(part);
                }
            }
        } else {
            Matcher matcher = DEFAULT_PATTERN.matcher(rest);

            if (!matcher.matches()) return false;

            targetDevice = matcher.group(1);
            targetState = matcher.group(2);
            targetStateAddtionalInformation = matcher.group(3);
        }
        return true;
    }

    private void handleIfPart(String part) {
        if (part.equals("$we")) {
            repetition = AtRepetition.WEEKEND;
        } else if (part.matches("(NOT|not|!)[ ]?\\$we")) {
            repetition = AtRepetition.WEEKDAY;
        } else if (part.equals("0")) {
            isActive = false;
        } else if (part.matches("\\$wday[ ]?==[ ]?[0-6]")) {
            int weekdayOrdinate = Integer.parseInt(part.substring(part.length() - 1));
            repetition = AtRepetition.getRepetitionForWeekdayOrdinate(weekdayOrdinate);
        }
    }

    @XmllistAttribute("STATE")
    public void setState(String value) {
        nextTrigger = value.replaceAll("Next: ", "");
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && definition != null && targetDevice != null;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getTargetDevice() {
        return targetDevice;
    }

    public void setTargetDevice(String targetDevice) {
        this.targetDevice = targetDevice;
    }

    public String getTargetState() {
        return targetState;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public String getTargetStateAddtionalInformation() {
        return targetStateAddtionalInformation;
    }

    public void setTargetStateAddtionalInformation(String targetStateAddtionalInformation) {
        this.targetStateAddtionalInformation = targetStateAddtionalInformation;
    }

    public AtRepetition getRepetition() {
        return repetition;
    }

    public void setRepetition(AtRepetition repetition) {
        this.repetition = repetition;
    }

    public TimerType getTimerType() {
        return timerType;
    }

    public void setTimerType(TimerType timerType) {
        this.timerType = timerType;
    }

    public String getNextTrigger() {
        return nextTrigger;
    }

    public void setHour(int hours) {
        this.hours = hours;
    }

    public void setMinute(int minutes) {
        this.minutes = minutes;
    }

    public void setSecond(int seconds) {
        this.seconds = seconds;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String toFHEMDefinition() {
        String command = "";
        if (timerType == TimerType.RELATIVE) {
            command += "+";
        }
        if (repetition != AtRepetition.ONCE) {
            command += "*";
        }

        command += getFormattedSwitchTime();

        command += " { fhem(\"set " + targetDevice + " " + targetState;
        if (targetStateAddtionalInformation != null) {
            command += " " + targetStateAddtionalInformation;
        }
        command += "\")";

        if (repetition != null || !isActive) {
            String ifContent = "";
            if (repetition == AtRepetition.WEEKEND) {
                ifContent = addToIf(ifContent, "$we");
            } else if (repetition == AtRepetition.WEEKDAY) {
                ifContent = addToIf(ifContent, "!$we");
            } else if (repetition != null && repetition.weekdayOrdinate != -1) {
                ifContent = addToIf(ifContent, "$wday == " + repetition.weekdayOrdinate);
            }

            if (!isActive) {
                ifContent = addToIf(ifContent, "0");
            }

            if (!StringUtil.isBlank(ifContent)) {
                command += " if (" + ifContent + ")";
            }
        }

        command += " }";

        return command;
    }

    public String getFormattedSwitchTime() {
        return toTwoDecimalDigits(hours) + ":" + toTwoDecimalDigits(minutes)
                + ":" + toTwoDecimalDigits(seconds);
    }

    private String addToIf(String ifContent, String newPart) {
        if (StringUtil.isBlank(ifContent)) {
            return newPart;
        }
        return ifContent + " && " + newPart;
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

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.FHEM;
    }

    public enum AtRepetition {
        ONCE(R.string.timer_overview_once), EVERY_DAY(R.string.timer_overview_every_day),
        WEEKEND(R.string.timer_overview_weekend), WEEKDAY(R.string.timer_overview_weekday),
        MONDAY(R.string.monday, 1), TUESDAY(R.string.tuesday, 2), WEDNESDAY(R.string.wednesday, 3),
        THURSDAY(R.string.thursday, 4), FRIDAY(R.string.friday, 5), SATURDAY(R.string.saturday, 6), SUNDAY(R.string.sunday, 0);

        private int stringId;
        private int weekdayOrdinate;

        AtRepetition(int stringId) {
            this.stringId = stringId;
            this.weekdayOrdinate = -1;
        }

        AtRepetition(int stringId, int weekdayOrdinate) {
            this.stringId = stringId;
            this.weekdayOrdinate = weekdayOrdinate;
        }

        public static AtRepetition getRepetitionForWeekdayOrdinate(int ordinate) {
            for (AtRepetition atRepetition : values()) {
                if (atRepetition.weekdayOrdinate == ordinate) {
                    return atRepetition;
                }
            }
            return null;
        }

        public int getText() {
            return stringId;
        }
    }

    public enum TimerType {
        RELATIVE(R.string.timer_overview_every), ABSOLUTE(R.string.timer_overview_at);

        private int stringId;

        TimerType(int stringId) {
            this.stringId = stringId;
        }

        public int getText() {
            return stringId;
        }
    }
}
