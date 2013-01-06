package li.klass.fhem.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReflectionUtilTest {

    @Test
    public void testMethodNameToFieldName() {
        assertThat(ReflectionUtil.methodNameToFieldName("getDesiredTemp"), is("desiredTemp"));
    }
}
