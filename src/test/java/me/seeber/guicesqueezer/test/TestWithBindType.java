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

package me.seeber.guicesqueezer.test;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.junit.Test;
import org.junit.runner.RunWith;

import me.seeber.guicesqueezer.Bind;
import me.seeber.guicesqueezer.GuiceSqueezer;

@RunWith(GuiceSqueezer.class)
@SuppressWarnings("javadoc")
public class TestWithBindType {

    @Qualifier
    @Retention(RUNTIME)
    public @interface TestQualifier {
    }

    public interface TestProvidesImplementedInterface_Interface {
    }

    @Bind
    public static class TestProvidesImplementedInterface_Implementation
            implements TestProvidesImplementedInterface_Interface {
    }

    @Test
    public void testProvidesImplementedInterface(TestProvidesImplementedInterface_Interface test) {
        assertThat(test).isInstanceOf(TestProvidesImplementedInterface_Implementation.class);
    }

    public static class TestProvidesSuperclass_Class {
    }

    @Bind
    public static class TestProvidesSuperclass_Implementation extends TestProvidesSuperclass_Class {
    }

    @Test
    public void testProvidesSuperclass(TestProvidesSuperclass_Class test) {
        assertThat(test).isInstanceOf(TestProvidesSuperclass_Implementation.class);
    }

    public interface TestProvidesSpecifiedInterface_Interface {
    }

    @Bind(TestProvidesSpecifiedInterface_Interface.class)
    public static class TestSpecifiedInterface_Implementation
            implements Cloneable, TestProvidesSpecifiedInterface_Interface {
    }

    @Test
    public void testProvidesSpecifiedInterface(TestProvidesSpecifiedInterface_Interface test) {
        assertThat(test).isInstanceOf(TestSpecifiedInterface_Implementation.class);
    }

    public interface TestProvidesQualifiedInterface_Interface {
    }

    @Bind
    @TestQualifier
    public static class TestProvidesQualifiedInterface_Implementation
            implements TestProvidesQualifiedInterface_Interface {
    }

    @Test
    public void testProvidesQualifiedInterface(@TestQualifier TestProvidesQualifiedInterface_Interface test) {
        assertThat(test).isInstanceOf(TestProvidesQualifiedInterface_Implementation.class);
    }

    public interface TestProvidesScopedInterface_Interface {
    }

    @Bind
    @Singleton
    public static class TestProvidesScopedInterface_Implementation implements TestProvidesScopedInterface_Interface {
    }

    @Test
    public void testProvidesScopedInterface(Provider<TestProvidesScopedInterface_Interface> testProvider) {
        TestProvidesScopedInterface_Interface test1 = testProvider.get();
        assertThat(test1).isInstanceOf(TestProvidesScopedInterface_Interface.class);

        TestProvidesScopedInterface_Interface test2 = testProvider.get();
        assertThat(test2).isInstanceOf(TestProvidesScopedInterface_Interface.class);

        assertThat(test1).isSameAs(test2);
    }

}
