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
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static li.klass.fhem.util.NumberUtil.toTwoDecimalDigits;

public class AtDevice extends Device<AtDevice> {

    public enum AtRepetition {
        ONCE(R.string.timer_overview_once), EVERY_DAY(R.string.timer_overview_every_day),
        WEEKEND(R.string.timer_overview_weekend), WEEKDAY(R.string.timer_overview_weekday);

        private int stringId;

        AtRepetition(int stringId) {
            this.stringId = stringId;
        }

        public String getText() {
            return AndFHEMApplication.getContext().getString(stringId);
        }
    }

    public enum TimerType {
        RELATIVE(R.string.timer_overview_every), ABSOLUTE(R.string.timer_overview_at);

        private int stringId;

        TimerType(int stringId) {
            this.stringId = stringId;
        }

        public String getText() {
            return AndFHEMApplication.getContext().getString(stringId);
        }
    }

    public static final Pattern FHEM_PATTERN = Pattern.compile("fhem\\(\"set ([\\w\\-,]+) ([\\w%-]+)(?: ([0-9:]+))?\"\\)(.*)");
    public static final Pattern PREFIX_PATTERN = Pattern.compile("([+*]{0,2})([0-9:]+)(.*)");
    public static final Pattern DEFAULT_PATTERN = Pattern.compile("set ([\\w-]+) ([\\w\\-,%]+)(?: ([0-9:]+))?");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private int hours;
    private int minutes;
    private int seconds;
    private String nextTrigger;

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

    @Override
    public void readSTATE(String value) {
        super.readSTATE(value);
        nextTrigger = value.replaceAll("Next: ", "");
    }

    @Override
    public boolean isSupported() {
        return definition != null && targetDevice != null;
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

    public String getTargetState() {
        return targetState;
    }

    public String getTargetStateAddtionalInformation() {
        return targetStateAddtionalInformation;
    }

    public AtRepetition getRepetition() {
        return repetition;
    }

    public TimerType getTimerType() {
        return timerType;
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

    public void setTargetDevice(String targetDevice) {
        this.targetDevice = targetDevice;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public void setTargetStateAddtionalInformation(String targetStateAddtionalInformation) {
        this.targetStateAddtionalInformation = targetStateAddtionalInformation;
    }

    public void setRepetition(AtRepetition repetition) {
        this.repetition = repetition;
    }

    public void setTimerType(TimerType timerType) {
        this.timerType = timerType;
    }

    public String getFormattedSwitchTime() {
        return toTwoDecimalDigits(hours) + ":" + toTwoDecimalDigits(minutes)
                + ":" + toTwoDecimalDigits(seconds);
    }

    private void parseDateContent(String dateContent) {
        if (dateContent.length() < "00:00:00".length()) {
            dateContent += ":00";
        }
        try {
            Date date = dateFormat.parse(dateContent);
            hours = date.getHours();
            minutes = date.getMinutes();
            seconds = date.getSeconds();

        } catch (ParseException e) {
            Log.e(AtDevice.class.getName(), "cannot parse dateContent " + dateContent);
        }
    }

    boolean parseDefinition(String nodeContent) {
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
        rest = rest.replaceAll("[{}]", "").trim();
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
                repetition = AtRepetition.WEEKDAY;
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

    private void handlePrefix(String prefix) {
        if (prefix.contains("+")) {
            timerType = TimerType.RELATIVE;
        }

        if (prefix.contains("*")) {
            repetition = AtRepetition.EVERY_DAY;
        }
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

        if (repetition == AtRepetition.WEEKEND) {
            command += " if($we)";
        } else if (repetition == AtRepetition.WEEKDAY) {
            command += " if (!$we)";
        }
        command += " }";

        return command;
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
}
