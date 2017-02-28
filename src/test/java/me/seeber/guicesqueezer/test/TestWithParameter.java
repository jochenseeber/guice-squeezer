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

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Named;
import javax.inject.Provider;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import me.seeber.guicesqueezer.GuiceSqueezer;

@RunWith(GuiceSqueezer.class)
@SuppressWarnings("javadoc")
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

    @Test
    public void testMethod_ParameterWithProvider(Provider<String> testStringProvider) {
        assertThat(testStringProvider.get()).isEqualTo("1");
    }

    @Test
    public void testMethod_ParameterWithGuiceProvider(com.google.inject.Provider<String> testStringProvider) {
        assertThat(testStringProvider.get()).isEqualTo("1");
    }
}
