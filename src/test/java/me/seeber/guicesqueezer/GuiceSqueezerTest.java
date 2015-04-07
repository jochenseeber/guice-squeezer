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
import me.seeber.guicesqueezer.test.TestWithAllModuleConfigurations;
import me.seeber.guicesqueezer.test.TestWithModuleMethod;
import me.seeber.guicesqueezer.test.TestWithModulesAnnotation;
import me.seeber.guicesqueezer.test.TestWithNestedModuleClass;
import me.seeber.guicesqueezer.test.TestWithProvidesType;
import me.seeber.guicesqueezer.test.TestWithProvidesType.TestClass1;
import me.seeber.guicesqueezer.test.TestWithProvidesType.TestClass2;
import me.seeber.guicesqueezer.test.TestWithProvidesType.TestInterface1;
import me.seeber.guicesqueezer.test.TestWithProvidesType.TestInterface2;

import org.junit.Test;
import org.junit.runners.model.InitializationError;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class GuiceSqueezerTest {
    
    @Test
    public void testModuleFromAnnotation() throws InitializationError {
        GuiceSqueezer runner = new GuiceSqueezer(TestWithModulesAnnotation.class);
        Injector injector = runner.getInjector();
        assertThat(injector.getInstance(Key.get(String.class, Names.named("annotation")))).isEqualTo("1");
    }
    
    @Test
    public void testModuleFromMethod() throws InitializationError {
        GuiceSqueezer runner = new GuiceSqueezer(TestWithModuleMethod.class);
        Injector injector = runner.getInjector();
        assertThat(injector.getInstance(Key.get(String.class, Names.named("method")))).isEqualTo("1");
    }
    
    @Test
    public void testModuleFromNestedClass() throws InitializationError {
        GuiceSqueezer runner = new GuiceSqueezer(TestWithNestedModuleClass.class);
        Injector injector = runner.getInjector();
        assertThat(injector.getInstance(Key.get(String.class, Names.named("nested")))).isEqualTo("1");
    }
    
    @Test
    public void testModuleFromAll() throws InitializationError {
        GuiceSqueezer runner = new GuiceSqueezer(TestWithAllModuleConfigurations.class);
        Injector injector = runner.getInjector();
        assertThat(injector.getInstance(Key.get(String.class, Names.named("annotation")))).isEqualTo("1");
        assertThat(injector.getInstance(Key.get(String.class, Names.named("nested")))).isEqualTo("1");
        assertThat(injector.getInstance(Key.get(String.class, Names.named("method")))).isEqualTo("1");
    }
    
    @Test
    public void testProvidesType() throws InitializationError {
        GuiceSqueezer runner = new GuiceSqueezer(TestWithProvidesType.class);
        Injector injector = runner.getInjector();
        assertThat(injector.getInstance(TestInterface1.class)).isInstanceOf(TestClass1.class);
        assertThat(injector.getInstance(TestInterface2.class)).isInstanceOf(TestClass2.class);
    }
}
