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

package me.seeber.guicesqueezer.test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Named;

import me.seeber.guicesqueezer.GuiceSqueezer;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.sun.istack.internal.Nullable;

@RunWith(GuiceSqueezer.class)
public class TestWithParameter {
    
    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance("1");
            bind(String.class).annotatedWith(Names.named("2")).toInstance("2");
            bind(String.class).annotatedWith(Names.named("3")).toInstance("3");
        }
    }
    
    @Test
    public void testMethod_NoParameters() {
        // Empty
    }
    
    @Test
    public void testMethod_Parameter(String testString) {
        assertThat(testString).isEqualTo("1");
    }
    
    @Test
    public void testMethod_ParameterWithQualifier(@Named("2") String testString) {
        assertThat(testString).isEqualTo("2");
    }
    
    @Test
    public void testMethod_ParameterWithBindingAnnotation(@com.google.inject.name.Named("3") String testString) {
        assertThat(testString).isEqualTo("3");
    }
    
    @Test
    public void testMethod_ParameterWithOtherAnnotation(@Nullable String testString) {
        assertThat(testString).isEqualTo("1");
    }
}
