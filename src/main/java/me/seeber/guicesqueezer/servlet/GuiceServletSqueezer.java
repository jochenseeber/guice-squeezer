
package me.seeber.guicesqueezer.servlet;

import java.util.Collections;
import java.util.concurrent.Callable;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.google.inject.servlet.ServletScopes;

import me.seeber.guicesqueezer.GuiceSqueezer;

/**
 * JUnit runner that runs each test method in a separate request scope
 */
public class GuiceServletSqueezer extends GuiceSqueezer {

    /**
     * Statement that invokes a wrapped statement inside a request context
     */
    protected static class ScopedStatement extends Statement {

        /**
         * Statement to be invoked inside a request context
         */
        private final Statement wrappedStatement;

        /**
         * Create a new wrapped statement
         *
         * @param wrappedStatement Statement to be invoked inside a request context
         */
        public ScopedStatement(Statement wrappedStatement) {
            this.wrappedStatement = wrappedStatement;
        }

        /**
         * @see org.junit.runners.model.Statement#evaluate()
         */
        @Override
        public void evaluate() throws Throwable {
            Callable<Void> scopedCallable = ServletScopes.scopeRequest(() -> {
                try {
                    this.wrappedStatement.evaluate();
                }
                catch (final Error e) {
                    throw e;
                }
                catch (final Exception e) {
                    throw e;
                }
                catch (Throwable t) {
                    throw new RuntimeException("Error invoking test method", t);
                }

                return null;
            }, Collections.emptyMap());

            scopedCallable.call();
        }

    }

    /**
     * Create a new test runner
     *
     * @param testClass Test class
     * @throws InitializationError when, well, the initialization fails
     */
    public GuiceServletSqueezer(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * Create a new method invoker that runs the test inside a request scope
     *
     * @see me.seeber.guicesqueezer.GuiceSqueezer#methodInvoker(org.junit.runners.model.FrameworkMethod,
     *      java.lang.Object)
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod testMethod, Object test) {
        Statement statement = super.methodInvoker(testMethod, test);
        Statement wrappedStatement = new ScopedStatement(statement);
        return wrappedStatement;
    }

}
