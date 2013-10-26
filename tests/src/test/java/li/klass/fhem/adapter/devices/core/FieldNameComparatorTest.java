package li.klass.fhem.adapter.devices.core;

import li.klass.fhem.domain.HCSDevice;
import li.klass.fhem.util.ReflectionUtil;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class FieldNameComparatorTest {
    @Test
    public void testOrder() throws Exception {
        Class<HCSDevice> hcsDeviceClass = HCSDevice.class;

        FieldNameComparator comparator = FieldNameComparator.COMPARATOR;

        Field afterField = hcsDeviceClass.getDeclaredField("commaSeparatedDemandDevices");
        Field orderField = hcsDeviceClass.getDeclaredField("numberOfDemandDevices");
        Field otherField = ReflectionUtil.findField(hcsDeviceClass, "state");

        assertThat(comparator.compare(afterField, orderField), is(1));
        assertThat(comparator.compare(orderField, afterField), is(-1));

        assertThat(comparator.compare(afterField, otherField), is(-1));
        assertThat(comparator.compare(otherField, afterField), is(1));

        assertThat(comparator.compare(orderField, otherField), is(-1));
        assertThat(comparator.compare(otherField, orderField), is(1));
    }

    @Test
    public void testHCSOrder() {
        Class<HCSDevice> hcsDeviceClass = HCSDevice.class;

        FieldNameComparator comparator = FieldNameComparator.COMPARATOR;
        List<Field> fields = Arrays.asList(hcsDeviceClass.getDeclaredFields());

        Collections.sort(fields, comparator);

        int numberOfDemandDevicesIndex = -1;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equalsIgnoreCase("numberOfDemandDevices")) {
                numberOfDemandDevicesIndex = i;
                break;
            }
        }

        if (numberOfDemandDevicesIndex == -1) {
            fail("cannot find demandDevices attribute index.");
        }

        assertThat(fields.get(numberOfDemandDevicesIndex + 1).getName(), is("commaSeparatedDemandDevices"));
    }
}
