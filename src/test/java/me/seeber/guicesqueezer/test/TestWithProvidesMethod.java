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
package me.seeber.guicesqueezer.test;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import me.seeber.guicesqueezer.GuiceSqueezer;
import me.seeber.guicesqueezer.test.TestWithBindType.TestQualifier;

@RunWith(GuiceSqueezer.class)
@SuppressWarnings("javadoc")
public class TestWithProvidesMethod {

    public static class TestProvidesMethod {

        public interface TestInterface {
        }

        public static class TestClass implements TestInterface {
        }

    }

    @Provides
    public static TestProvidesMethod.TestInterface providesTestInterface() {
        return new TestProvidesMethod.TestClass();
    }

    @Test
    public void testProvidesMethod(TestProvidesMethod.TestInterface test) {
        assertThat(test).isInstanceOf(TestProvidesMethod.TestClass.class);
    }

    public static class TestProvidesMethodWithParameter_Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance("1");
        }
    }

    public static class TestProvidesMethodWithParameter {

        public interface TestInterface {

            public String getName();

        }

        public static class TestClass implements TestInterface {

            private final String name;

            public TestClass(String name) {
                this.name = name;
            }

            @Override
            public String getName() {
                return this.name;
            }

        }

    }

    @Provides
    public static TestProvidesMethodWithParameter.TestInterface providesTestInterfaceWithParameter(String name) {
        return new TestProvidesMethodWithParameter.TestClass(name);
    }

    @Test
    public void testProvidesMethodWithParameters(TestProvidesMethodWithParameter.TestInterface test) {
        assertThat(test).as("test").isInstanceOf(TestProvidesMethodWithParameter.TestClass.class);
        assertThat(test.getName()).as("test.name").isEqualTo("1");
    }

    public static class TestProvidesMethodWithQualifier {

        @Qualifier
        @Retention(RUNTIME)
        public @interface TestQualifier {
        }

        public interface TestInterface {
        }

        public static class TestClass implements TestInterface {
        }

    }

    @Provides
    @TestQualifier
    public static TestProvidesMethodWithQualifier.TestInterface providesTestInterfaceWithQualifier() {
        return new TestProvidesMethodWithQualifier.TestClass();
    }

    @Test
    public void testProvidesMethodWithQualifier(@TestQualifier TestProvidesMethodWithQualifier.TestInterface test) {
        assertThat(test).isInstanceOf(TestProvidesMethodWithQualifier.TestClass.class);
    }

    public static class TestProvidesMethodWithScope {

        public interface TestInterface {
        }

        public static class TestClass implements TestInterface {
        }

    }

    @Provides
    @Singleton
    public static TestProvidesMethodWithScope.TestInterface providesTestInterfaceWithScope() {
        return new TestProvidesMethodWithScope.TestClass();
    }

    @Test
    public void testProvidesMethodWithScope(TestProvidesMethodWithScope.TestInterface test1,
            TestProvidesMethodWithScope.TestInterface test2) {
        assertThat(test1).as("test1").isInstanceOf(TestProvidesMethodWithScope.TestClass.class);
        assertThat(test2).as("test2").isInstanceOf(TestProvidesMethodWithScope.TestClass.class);

        assertThat(test1).as("test1").isSameAs(test2).as("test2");
    }

}
