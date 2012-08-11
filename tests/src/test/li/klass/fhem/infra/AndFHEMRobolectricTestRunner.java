package li.klass.fhem.infra;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.AndFHEMMainActivity;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.lang.reflect.Method;

public class AndFHEMRobolectricTestRunner extends RobolectricTestRunner {
    public AndFHEMRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass, new RobolectricConfig(new File(ProjectMetaDataProvider.getProjectRoot() + "/app")));
    }

    @Override
    public void beforeTest(Method method) {
        AndFHEMApplication.setContext(new AndFHEMMainActivity());
        super.beforeTest(method);
    }
}
