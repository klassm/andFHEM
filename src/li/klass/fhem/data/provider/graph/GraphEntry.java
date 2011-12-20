package li.klass.fhem.data.provider.graph;

import java.util.Date;

public class GraphEntry implements Comparable<GraphEntry> {
    private int hour;
    private float value;

    public GraphEntry(Date date, float value) {
        this(date.getHours(), value);
    }

    public GraphEntry(int hour, float value) {
        this.hour = hour;
        this.value = value;
    }

    public int getHour() {
        return hour;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GraphEntry{" +
                "hour=" + hour +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphEntry that = (GraphEntry) o;

        return hour == that.hour;
    }

    @Override
    public int hashCode() {
        return hour;
    }

    @Override
    public int compareTo(GraphEntry graphEntry) {
        return ((Integer) hour).compareTo(graphEntry.getHour());
    }
}
