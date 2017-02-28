/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2016-2017, Jochen Seeber
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
package me.seeber.guicesqueezer;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import me.seeber.guicesqueezer.java.Argument;
import me.seeber.guicesqueezer.java.Validate;

/**
 * JUnit {@link Runner} to run Guice based unit tests
 */
public class GuiceSqueezer extends BlockJUnit4ClassRunner {

    /**
     * Injector used to create test objects
     */
    @Nullable
    private Injector injector;

    /**
     * Injector factory used to create Guice injectors for tests
     */
    @Nullable
    private TestComposition injectorFactory;

    /**
     * Create a new runner
     *
     * @param testClass Test class
     * @throws InitializationError if something goes wrong
     */
    public GuiceSqueezer(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * @see org.junit.runners.BlockJUnit4ClassRunner#runChild(org.junit.runners.model.FrameworkMethod,
     *      org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(@Nullable FrameworkMethod method, @Nullable RunNotifier notifier) {
        updateInjector(Argument.notNull(method, "method"));

        super.runChild(method, notifier);
    }

    /**
     * Update the injector for a new framework method
     *
     * @param method Test method
     * @return Injector to use
     */
    protected Injector updateInjector(FrameworkMethod method) {
        Module classModule = getInjectorFactory().createTestClassModule(getTestClass().getJavaClass());
        Module methodModule = getInjectorFactory()
                .createTestMethodModule(Argument.notNull(method, "method").getMethod());
        Module module = Modules.override(classModule).with(methodModule);
        Injector injector = Guice.createInjector(module);

        setInjector(injector);

        return injector;
    }

    /**
     * Create a new test object from Guice
     *
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        Object testObject = getInjector().getInstance(getTestClass().getJavaClass());

        assert testObject != null;

        return testObject;
    }

    /**
     * Get a method invoker
     *
     * @see org.junit.runners.BlockJUnit4ClassRunner#methodInvoker(org.junit.runners.model.FrameworkMethod,
     *      java.lang.Object)
     */
    @Override
    protected Statement methodInvoker(@Nullable FrameworkMethod testMethod, @Nullable Object test) {
        Statement statement = getInjectorFactory().createInvocationStatement(Argument.notNull(testMethod, "testMethod"),
                Argument.notNull(test, "test"), getInjector());
        return statement;
    }

    /**
     * @see org.junit.runners.BlockJUnit4ClassRunner#validateTestMethods(java.util.List)
     */
    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        errors = Argument.notNull(errors, "errors");

        List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<Throwable> validationErrors = getInjectorFactory().validateTestMethods(testMethods);
        errors.addAll(validationErrors);
    }

    /**
     * Get the factory used to create Guice injectors for the test
     *
     * @return Injector factory
     */
    protected TestComposition getInjectorFactory() {
        if (this.injectorFactory == null) {
            this.injectorFactory = new DefaultTestComposition();
        }

        assert this.injectorFactory != null;

        return this.injectorFactory;
    }

    /**
     * Get the injector used to create test objects
     *
     * @return Injector
     */
    protected Injector getInjector() {
        return Validate.notNull(this.injector, "injector");
    }

    /**
     * Set the injector used to create test objects
     *
     * @param injector Injector
     */
    protected void setInjector(Injector injector) {
        this.injector = injector;
    }

}
