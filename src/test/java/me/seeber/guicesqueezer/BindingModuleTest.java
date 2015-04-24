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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

@RunWith(JMockit.class)
public class BindingModuleTest {
    
    private BindingModule module;
    
    @Test
    public void test() {
        assertThat(1);
    }
    
    @Before
    public void initializeTest() {
        Map<Class<?>, Class<?>> bindings = Collections.singletonMap(Serializable.class, String.class);
        this.module = new BindingModule(bindings);
    }
    
    @Test
    public void testConfigure() {
        Binder binder = new MockUp<Binder>() {
            @Mock(invocations = 1)
            public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
                assertThat(type).isSameAs(Serializable.class);
                
                return new MockUp<AnnotatedBindingBuilder<T>>() {
                    @Mock(invocations = 1)
                    public ScopedBindingBuilder to(Class<? extends T> implementation) {
                        assertThat(implementation).isSameAs(String.class);
                        return null;
                    }
                }.getMockInstance();
            }
        }.getMockInstance();
        
        BindingModuleTest.this.module.configure(binder);
    }
}
