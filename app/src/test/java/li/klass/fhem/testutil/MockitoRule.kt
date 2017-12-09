package li.klass.fhem.testutil

import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.mockito.MockitoAnnotations

class MockitoRule : MethodRule {
    override fun apply(base: Statement, frameworkMethod: FrameworkMethod, test: Any): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                MockitoAnnotations.initMocks(test)
                base.evaluate()
            }
        }
    }
}
