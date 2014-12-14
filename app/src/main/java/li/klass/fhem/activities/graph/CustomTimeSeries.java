package li.klass.fhem.activities.graph;

import android.util.Log;
import li.klass.fhem.util.ReflectionUtil;
import org.achartengine.model.TimeSeries;

import java.lang.reflect.Field;

public class CustomTimeSeries extends TimeSeries {

    public CustomTimeSeries(String title, int scaleNumber) {
        super(title);
        setScaleNumber(scaleNumber);
    }

    private void setScaleNumber(int scaleNumber) {
        try {
            Field field = ReflectionUtil.findField(getClass(), "mScaleNumber");
            field.set(this, scaleNumber);
        } catch (Exception e) {
            Log.e(CustomTimeSeries.class.getName(), "could not set scale number", e);
        }
    }
}
