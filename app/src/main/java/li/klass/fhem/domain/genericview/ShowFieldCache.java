package li.klass.fhem.domain.genericview;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;

public class ShowFieldCache implements ShowField, Serializable {

    private final ResourceIdMapper _description;
    private final boolean _showInOverview;
    private final boolean _showInDetail;
    private final String _showAfter;

    public ShowFieldCache(ShowField showField) {
        _description = showField.description();
        _showInOverview = showField.showInOverview();
        _showInDetail = showField.showInDetail();
        _showAfter = showField.showAfter();
    }

    @Override
    public ResourceIdMapper description() {
        return _description;
    }

    @Override
    public boolean showInOverview() {
        return _showInOverview;
    }

    @Override
    public boolean showInDetail() {
        return _showInDetail;
    }

    @Override
    public String showAfter() {
        return _showAfter;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ShowField.class;
    }
}
