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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import me.seeber.guicesqueezer.java.Validate;

@SuppressWarnings("javadoc")
public class GuiceSqueezerTest {

    @Nullable
    public GuiceSqueezer squeezer;

    @Before
    public void initializeTest() throws InitializationError {
        this.squeezer = new GuiceSqueezer(GuiceSqueezerTest.class);
    }

    @Test
    public void testGetInjectorFactory() {
        TestComposition injectorFactory = getSqueezer().getInjectorFactory();
        assertThat(injectorFactory).isNotNull();
    }

    @Test
    public void testCreateTest() throws Exception {
        FrameworkMethod testMethod = new FrameworkMethod(GuiceSqueezerTest.class.getMethod("testMethodInvoker"));
        getSqueezer().updateInjector(testMethod);

        Object testObject = getSqueezer().createTest();
        assertThat(testObject).isNotNull().isInstanceOf(GuiceSqueezerTest.class);
    }

    @Test
    public void testMethodInvoker() throws Exception {
        FrameworkMethod testMethod = new FrameworkMethod(GuiceSqueezerTest.class.getMethod("testMethodInvoker"));
        getSqueezer().updateInjector(testMethod);

        Object testObject = getSqueezer().createTest();

        Statement statement = getSqueezer().methodInvoker(testMethod, testObject);
        assertThat(statement).isNotNull();
    }

    @Test
    public void testValidateTestMethods() {
        List<Throwable> errors = new ArrayList<>();
        getSqueezer().validateTestMethods(errors);
        assertThat(errors).isEmpty();
    }

    protected GuiceSqueezer getSqueezer() {
        return Validate.notNull(this.squeezer, "squeezer");
    }

}
