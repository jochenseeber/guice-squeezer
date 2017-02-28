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

import java.lang.reflect.Method;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Composition that manages how tests are configured and invoked
 */
public interface TestComposition {

    /**
     * Create the Guice injector for a test case
     *
     * @param testClass Test class
     * @return Injector Injector for the class
     */
    public Module createTestClassModule(Class<?> testClass);

    /**
     * Create the Guice injector for a test method
     *
     * @param testMethod Test method
     * @return Local injector for the test method
     */
    public Module createTestMethodModule(Method testMethod);

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
     * @param injector Injector to create objects
     * @return Invocation statement
     */
    public Statement createInvocationStatement(FrameworkMethod testMethod, Object test, Injector injector);

}
