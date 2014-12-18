package li.klass.fhem.domain.genericview;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;

public class OverviewViewSettingsCache implements OverviewViewSettings, Serializable {

    private final boolean _showState;
    private final boolean _showMeaured;
    private final ResourceIdMapper _stateStringId;
    private final ResourceIdMapper _measuredStringId;

    public OverviewViewSettingsCache(OverviewViewSettings settings) {
        _showState = settings.showState();
        _showMeaured = settings.showMeasured();
        _stateStringId = settings.stateStringId();
        _measuredStringId = settings.measuredStringId();
    }

    public boolean showState() {
        return _showState;
    }

    public boolean showMeasured() {
        return _showMeaured;
    }

    public ResourceIdMapper stateStringId() {
        return _stateStringId;
    }


    public ResourceIdMapper measuredStringId() {
        return _measuredStringId;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return OverviewViewSettings.class;
    }

}
