/*
 * #%L
 * Guice Squeezer test runner for Guice and JUnit
 * %%
 * Copyright (C) 2015 Jochen Seeber
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package me.seeber.guicesqueezer;

import java.util.List;

import org.junit.Test;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;

/**
 * JUnit {@link Runner} to run Guice based unit tests
 */
public class GuiceSqueezer extends BlockJUnit4ClassRunner {
    
    /**
     * Injector used to create test objects
     */
    private Injector injector;
    
    /**
     * Injector factory used to create Guice injectors for tests
     */
    private InjectorFactory injectorFactory;
    
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
     * Create a new test object from Guice
     * 
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        Object testObject = getInjector().getInstance(getTestClass().getJavaClass());
        return testObject;
    }
    
    /**
     * Get the injector used to create test objects
     * 
     * @return Injector
     */
    protected Injector getInjector() {
        if (this.injector == null) {
            this.injector = getInjectorFactory().createTestClassInjector(getTestClass().getJavaClass());
        }
        
        return this.injector;
    }
    
    /**
     * Get a method invoker
     * 
     * @see org.junit.runners.BlockJUnit4ClassRunner#methodInvoker(org.junit.runners.model.FrameworkMethod,
     *      java.lang.Object)
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod testMethod, Object test) {
        Statement statement = getInjectorFactory().createInvocationStatement(testMethod, test, this.injector);
        return statement;
    }
    
    /**
     * @see org.junit.runners.BlockJUnit4ClassRunner#validateTestMethods(java.util.List)
     */
    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<Throwable> validationErrors = getInjectorFactory().validateTestMethods(testMethods);
        errors.addAll(validationErrors);
    }
    
    /**
     * Get the factory used to create Guice injectors for the test
     * 
     * @return Injector factory
     */
    protected InjectorFactory getInjectorFactory() {
        if (this.injectorFactory == null) {
            this.injectorFactory = new DefaultInjectorFactory();
        }
        
        return this.injectorFactory;
    }
    
}
