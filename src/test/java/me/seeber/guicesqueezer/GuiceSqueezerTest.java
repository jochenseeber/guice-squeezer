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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;

public class GuiceSqueezerTest {
    
    public GuiceSqueezer squeezer;
    
    @Before
    public void initializeTest() throws InitializationError {
        this.squeezer = new GuiceSqueezer(GuiceSqueezerTest.class);
    }
    
    @Test
    public void testGetInjectorFactory() {
        InjectorFactory injectorFactory = this.squeezer.getInjectorFactory();
        assertThat(injectorFactory).isNotNull();
    }
    
    @Test
    public void testGetInjector() {
        Injector injector = this.squeezer.getInjector();
        assertThat(injector).isNotNull();
    }
    
    @Test
    public void testCreateTest() throws Exception {
        Object testObject = this.squeezer.createTest();
        assertThat(testObject).isNotNull().isInstanceOf(GuiceSqueezerTest.class);
    }
    
    @Test
    public void testMethodInvoker() throws Exception {
        Object testObject = this.squeezer.createTest();
        FrameworkMethod testMethod = new FrameworkMethod(testObject.getClass().getMethod("testMethodInvoker"));
        
        Statement statement = this.squeezer.methodInvoker(testMethod, testObject);
        assertThat(statement).isNotNull();
    }
    
    @Test
    public void testValidateTestMethods() {
        List<Throwable> errors = new ArrayList<>();
        this.squeezer.validateTestMethods(errors);
        assertThat(errors).isEmpty();
    }
    
}
