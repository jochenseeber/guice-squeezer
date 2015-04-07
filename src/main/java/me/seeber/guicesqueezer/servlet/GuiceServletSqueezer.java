/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2016, Jochen Seeber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.seeber.guicesqueezer.servlet;

import java.util.Collections;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.Nullable;
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
            Callable<@Nullable Void> scopedCallable = ServletScopes.scopeRequest(() -> {
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
    protected Statement methodInvoker(@Nullable FrameworkMethod testMethod, @Nullable Object test) {
        Statement statement = super.methodInvoker(testMethod, test);
        Statement wrappedStatement = new ScopedStatement(statement);
        return wrappedStatement;
    }

}
