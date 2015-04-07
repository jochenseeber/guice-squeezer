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

package me.seeber.guicesqueezer;

import java.lang.reflect.Method;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;

/**
 * Statement that invokes a test method with parameters
 *
 * The parameters are resolved using the supplied {@link Injector} before calling the method.
 */
public class InvokeWithParametersStatement extends Statement {

    /**
     * Test method to call
     */
    private final FrameworkMethod testMethod;

    /**
     * Test object
     */
    private final Object target;

    /**
     * Resolver used to resolve method parameters
     */
    private final ArgumentResolver argumentResolver;

    /**
     * Injector to resolve parameters
     */
    private final Injector injector;

    /**
     * Create a new statement
     *
     * @param testMethod Test method to call
     * @param target Test object
     * @param argumentResolver Resolver used to resolve method parameters
     * @param injector Injector to resolve parameters
     */
    public InvokeWithParametersStatement(FrameworkMethod testMethod,
            Object target,
            ArgumentResolver argumentResolver,
            Injector injector) {
        this.testMethod = testMethod;
        this.target = target;
        this.argumentResolver = argumentResolver;
        this.injector = injector;
    }

    /**
     * @see org.junit.runners.model.Statement#evaluate()
     */
    @Override
    public void evaluate() throws Throwable {
        Method method = this.testMethod.getMethod();
        Object[] parameters = this.argumentResolver.resolveArguments(method, this.injector);
        this.testMethod.invokeExplosively(this.target, parameters);
    }
}
