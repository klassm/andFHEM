package li.klass.fhem.domain.fht;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;

import java.io.Serializable;

public class FHTDayControl implements Serializable {
    public static final String OFF_TIME = "24:00";
    private int dayId;

    private String from1 = OFF_TIME;
    private String from1Changed = OFF_TIME;
    private String from2 = OFF_TIME;
    private String from2Changed = OFF_TIME;
    private String to1 = OFF_TIME;
    private String to1Changed = OFF_TIME;
    private String to2 = OFF_TIME;
    private String to2Changed = OFF_TIME;

    public FHTDayControl(int dayId) {
        this.dayId = dayId;
    }

    public int getDayId() {
        return dayId;
    }

    public String getFrom1Current() {
        if (from1Changed != null) return from1Changed;
        return from1;
    }

    public void setFrom1(String from1) {
        this.from1 = from1;
        this.from1Changed = from1;
    }

    public String getFrom1Changed() {
        return from1Changed;
    }

    public void setFrom1Changed(String newValue) {
        String changedValueToSet = getChangedValueToSet(from1, newValue);
        if (changedValueToSet != null) from1Changed = changedValueToSet;
    }

    public String getFrom2Current() {
        if (from2Changed != null) return from2Changed;
        return from2;
    }

    public void setFrom2(String from2) {
        this.from2 = from2;
        this.from2Changed = from2;
    }

    public String getFrom2Changed() {
        return from2Changed;
    }

    public void setFrom2Changed(String newValue) {
        String changedValueToSet = getChangedValueToSet(from2, newValue);
        if (changedValueToSet != null) from2Changed = changedValueToSet;
    }

    public String getTo1Current() {
        if (to1Changed != null) return to1Changed;
        return to1;
    }

    public void setTo1(String to1) {
        this.to1 = to1;
        this.to1Changed = to1;
    }

    public String getTo1Changed() {
        return to1Changed;
    }

    public void setTo1Changed(String newValue) {
        String changedValueToSet = getChangedValueToSet(to1, newValue);
        if (changedValueToSet != null) to1Changed = changedValueToSet;
    }

    public String getTo2Current() {
        if (to2Changed != null) return to2Changed;
        return to2;
    }

    public void setTo2(String to2) {
        this.to2 = to2;
        this.to2Changed = to2;
    }

    public String getTo2Changed() {
        return to2Changed;
    }

    public void setTo2Changed(String newValue) {
        String changedValueToSet = getChangedValueToSet(to2, newValue);
        if (changedValueToSet != null) to2Changed = changedValueToSet;
    }

    public String getFrom1() {
        return from1;
    }

    public String getFrom2() {
        return from2;
    }

    public String getTo1() {
        return to1;
    }

    public String getTo2() {
        return to2;
    }

    public boolean hasChangedValues() {
        return ! from1Changed.equals(from1) || ! from2Changed.equals(from2) || ! to1Changed.equals(to1) || ! to2Changed.equals(to2);
    }

    private String getChangedValueToSet(String originalValue, String changedValue) {
        if (changedValue == null || changedValue.equals("00:00")) {
            return OFF_TIME;
        } else {
            return changedValue;
        }
    }

    /**
     * Format the given text. If null or time equals 24:00, return off
     * @param time time to check
     * @return formatted time
     */
    public static String formatTime(String time) {
        if (time == null || OFF_TIME.equals(time)) {
            return AndFHEMApplication.getContext().getResources().getString(R.string.off);
        } else {
            return time;
        }
    }

    public void reset() {
        to1Changed = to1;
        to2Changed = to2;
        from1Changed = from1;
        from2Changed = from2;
    }

    public void setChangedAsCurrent() {
        to1 = to1Changed;
        to2 = to2Changed;
        from1 = from1Changed;
        from2 = from2Changed;
    }
}
