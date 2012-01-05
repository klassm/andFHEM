package li.klass.fhem.domain.fht;

import java.io.Serializable;

public class FHTDayControl implements Serializable {
    private int dayId;

    private String from1;
    private String from1Changed;
    private String from2;
    private String from2Changed;
    private String to1;
    private String to1Changed;
    private String to2;
    private String to2Changed;

    public FHTDayControl(int dayId) {
        this.dayId = dayId;
    }

    public int getDayId() {
        return dayId;
    }

    public String getFrom1() {
        if (from1Changed != null) return from1Changed;
        return from1;
    }

    public void setFrom1(String from1) {
        this.from1 = from1;
    }

    public String getFrom1Changed() {
        return from1Changed;
    }

    public void setFrom1Changed(String from1Changed) {
        if (! from1.equals(from1Changed)) {
            this.from1Changed = from1Changed;
        }
    }

    public String getFrom2() {
        if (from2Changed != null) return from2Changed;
        return from2;
    }

    public void setFrom2(String from2) {
        this.from2 = from2;
    }

    public String getFrom2Changed() {
        return from2Changed;
    }

    public void setFrom2Changed(String from2Changed) {
        if (! from2.equals(from2Changed)) {
            this.from2Changed = from2Changed;
        }
    }

    public String getTo1() {
        if (to1Changed != null) return to1Changed;
        return to1;
    }

    public void setTo1(String to1) {
        this.to1 = to1;
    }

    public String getTo1Changed() {
        return to1Changed;
    }

    public void setTo1Changed(String to1Changed) {
        if (!to1.equals(to1Changed)) {
            this.to1Changed = to1Changed;
        }
    }

    public String getTo2() {
        if (to2Changed != null) return to2Changed;
        return to2;
    }

    public void setTo2(String to2) {
        this.to2 = to2;
    }

    public String getTo2Changed() {
        return to2Changed;
    }

    public void setTo2Changed(String to2Changed) {
        if (! to2.equals(to2Changed)) {
            this.to2Changed = to2Changed;
        }
    }

    public boolean hasChangedValues() {
        return from1Changed != null || from2Changed != null || to1Changed != null || to2Changed != null;
    }
}
