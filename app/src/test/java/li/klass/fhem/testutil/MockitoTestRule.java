package li.klass.fhem.testutil;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

public class MockitoTestRule implements MethodRule {
    @Override
    public Statement apply(final Statement base, FrameworkMethod frameworkMethod, final Object test) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MockitoAnnotations.initMocks(test);
                base.evaluate();
            }
        };
    }
}
