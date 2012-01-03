package li.klass.fhem.domain;

import android.content.res.Resources;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import org.w3c.dom.NamedNodeMap;

public class OwcountDevice extends Device<OwcountDevice> {
    
    private float counterA;
    private float counterB;
    private float correlationA;
    private float correlationB;
    private String present;
    private String warnings;

    
    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("COUNTERS.A")) {
            this.counterA = Float.valueOf(nodeContent);
        } else if (keyValue.equals("COUNTERS.B")) {
            this.counterB = Float.valueOf(nodeContent);
        } else if (keyValue.equals("CORR1")) {
            this.correlationA = Float.valueOf(nodeContent);
        } else if (keyValue.equals("CORR2")) {
            this.correlationB = Float.valueOf(nodeContent);
        } else if (keyValue.equals("PRESENT")) {
            Resources resources = AndFHEMApplication.getContext().getResources();
            this.present = nodeContent.equals("1") ? resources.getString(R.string.yes) : resources.getString(R.string.no);
        } else if (keyValue.equals("WARNINGS")) {
            this.warnings = nodeContent;
        }
    }

    public float getCounterA() {
        return counterA;
    }

    public float getCounterB() {
        return counterB;
    }

    public float getCorrelationA() {
        return correlationA;
    }

    public float getCorrelationB() {
        return correlationB;
    }

    public String getPresent() {
        return present;
    }

    public String getWarnings() {
        return warnings;
    }
}
