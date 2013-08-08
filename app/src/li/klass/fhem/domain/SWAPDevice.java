package li.klass.fhem.domain;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;
import org.w3c.dom.NamedNodeMap;

public class SWAPDevice extends Device<SWAPDevice> {
    @Override
    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        super.readSTATE(tagName, attributes, value);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String formatTargetState(String targetState) {
        if (targetState.endsWith("°C")) {
            double temperature = ValueExtractUtil.extractLeadingDouble(targetState);
            return ValueDescriptionUtil.appendTemperature(temperature);
        }
        return super.formatTargetState(targetState);
    }
}
