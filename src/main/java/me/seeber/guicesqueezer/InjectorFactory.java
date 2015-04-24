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

import java.lang.reflect.Method;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;

/**
 * Factory to create Guice injectors for test cases
 */
public interface InjectorFactory {
    
    /**
     * Create the Guice injector for a test case
     * 
     * @param testClass Test class
     * @return Injector Injector for the class
     */
    public Injector createTestClassInjector(Class<?> testClass);
    
    /**
     * Create the Guice injector for a test method
     * 
     * @param testMethod Test method
     * @param classInjector Global injector for the test case
     * @return Local injector for the test method
     */
    public Injector createTestMethodInjector(Method testMethod, Injector classInjector);
    
    /**
     * Validate test methods
     * 
     * @param testMethods Test methods
     * @return Found errors
     */
    public List<Throwable> validateTestMethods(List<FrameworkMethod> testMethods);
    
    /**
     * Create a method invocation statement
     * 
     * Change the default JUnit behavior to add parameters
     * 
     * @param testMethod Test method
     * @param test Test object
     * @param classInjector Injector to create objects
     * @return Invocation statement
     */
    public Statement createInvocationStatement(FrameworkMethod testMethod, Object test, Injector classInjector);
    
}
