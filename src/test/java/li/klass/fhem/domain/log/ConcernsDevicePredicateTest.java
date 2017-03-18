package li.klass.fhem.domain.log;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static li.klass.fhem.domain.log.ConcernsDevicePredicate.extractConcerningDeviceRegexpFromDefinition;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class ConcernsDevicePredicateTest {
    @DataProvider
    public static Object[][] provider() {
        return $$(
                $("CUL_TX_116:T:.*", "CUL_TX_116"),
                $(".", ".*"),
                $(".*", ".*"),
                $(".*|abc", ".*|abc"),
                $(".|abc", ".*|abc"),
                $("(def|abc)", "def|abc")
        );
    }

    @Test
    @UseDataProvider("provider")
    public void testExtractConcerningDeviceNameFromDefinition(String in, String expected) {
        assertThat(extractConcerningDeviceRegexpFromDefinition(in)).isEqualTo(expected);
    }
}