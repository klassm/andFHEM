package li.klass.fhem.domain;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.DataProviders;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static com.tngtech.java.junit.dataprovider.DataProviders.testForEach;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class EventMapParserTest {
    @DataProvider
    public static Object[][] eventMapTestCase() {
        return testForEach(
                new TestCase("/gpio 12 on:on/gpio 12 off:off/gpio 12 gpio:off/gpio 12 output:off/")
                        .expect("gpio 12 on", "on")
                        .expect("gpio 12 off", "off")
                        .expect("gpio 12 gpio", "off")
                        .expect("gpio 12 output", "off"),
                new TestCase(",gpio 12 on:on,gpio 12 off:off,gpio 12 gpio:off,gpio 12 output:off,")
                        .expect("gpio 12 on", "on")
                        .expect("gpio 12 off", "off")
                        .expect("gpio 12 gpio", "off")
                        .expect("gpio 12 output", "off"),
                new TestCase("on:an off:aus")
                        .expect("on", "an")
                        .expect("off", "aus")
        );
    }

    @Test
    @UseDataProvider("eventMapTestCase")
    public void parse_event_map(TestCase testCase) {
        Map<String, String> expected = testCase.expected;

        Map<String, String> result = EventMapParser.INSTANCE.parse(testCase.content);

        assertThat(result).isEqualTo(expected);

    }

    static class TestCase {
        private final String content;
        private Map<String, String> expected = new HashMap<>();

        TestCase(String content) {
            this.content = content;
        }

        TestCase expect(String key, String value) {
            expected.put(key, value);
            return this;
        }
    }
}
