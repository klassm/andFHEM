package li.klass.fhem.service.graph;

import java.util.Date;

public class GraphEntry implements Comparable<GraphEntry> {
    private float value;

    private Date date;

    public GraphEntry(Date date, float value) {
        this.value = value;
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "GraphEntry{" +
                "value=" + value +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphEntry that = (GraphEntry) o;

        return Float.compare(that.value, value) == 0 && !(date != null ? !date.equals(that.date) : that.date != null);

    }

    @Override
    public int hashCode() {
        int result = (value != +0.0f ? Float.floatToIntBits(value) : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(GraphEntry graphEntry) {
        return (date).compareTo(graphEntry.getDate());
    }
}
