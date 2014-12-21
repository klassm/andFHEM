package li.klass.fhem.domain.genericview;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;

public class ShowFieldCache implements ShowField, Serializable {

    private final ResourceIdMapper description;
    private final boolean showInOverview;
    private final boolean showInDetail;
    private final String showAfter;

    public ShowFieldCache(ShowField showField) {
        description = showField.description();
        showInOverview = showField.showInOverview();
        showInDetail = showField.showInDetail();
        showAfter = showField.showAfter();
    }

    @Override
    public ResourceIdMapper description() {
        return description;
    }

    @Override
    public boolean showInOverview() {
        return showInOverview;
    }

    @Override
    public boolean showInDetail() {
        return showInDetail;
    }

    @Override
    public String showAfter() {
        return showAfter;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ShowField.class;
    }
}
