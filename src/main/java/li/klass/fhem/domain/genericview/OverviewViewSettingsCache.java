package li.klass.fhem.domain.genericview;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import li.klass.fhem.resources.ResourceIdMapper;

public class OverviewViewSettingsCache implements OverviewViewSettings, Serializable {

    private final boolean showState;
    private final boolean showMeasured;
    private final ResourceIdMapper stateStringId;
    private final ResourceIdMapper measuredStringId;

    public OverviewViewSettingsCache(OverviewViewSettings settings) {
        showState = settings.showState();
        showMeasured = settings.showMeasured();
        stateStringId = settings.stateStringId();
        measuredStringId = settings.measuredStringId();
    }

    public boolean showState() {
        return showState;
    }

    public boolean showMeasured() {
        return showMeasured;
    }

    public ResourceIdMapper stateStringId() {
        return stateStringId;
    }


    public ResourceIdMapper measuredStringId() {
        return measuredStringId;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return OverviewViewSettings.class;
    }

}
