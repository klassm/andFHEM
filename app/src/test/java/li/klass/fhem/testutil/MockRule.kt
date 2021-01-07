package li.klass.fhem.testutil

import io.mockk.MockKAnnotations
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class MockRule : MethodRule {
    override fun apply(base: Statement, frameworkMethod: FrameworkMethod, test: Any): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                MockKAnnotations.init(test)
                base.evaluate()
            }
        }
    }
}
