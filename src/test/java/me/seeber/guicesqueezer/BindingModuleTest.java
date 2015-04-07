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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

import me.seeber.guicesqueezer.BindingModule.Binding;
import me.seeber.guicesqueezer.BindingModule.ClassSource;
import me.seeber.guicesqueezer.BindingModule.ClassTarget;
import me.seeber.guicesqueezer.java.Lists;

@SuppressWarnings({ "javadoc" })
public class BindingModuleTest {

    @Mock
    protected Binder binder;

    @Mock
    protected AnnotatedBindingBuilder<Serializable> annotatedBindingBuilder;

    @Mock
    protected LinkedBindingBuilder<Serializable> linkedBindingBuilder;

    @Mock
    protected ScopedBindingBuilder scopedBindingBuilder;

    public BindingModuleTest() {
        this.binder = mock(Binder.class);
        this.annotatedBindingBuilder = mock(AnnotatedBindingBuilder.class);
        this.linkedBindingBuilder = mock(LinkedBindingBuilder.class);
        this.scopedBindingBuilder = mock(ScopedBindingBuilder.class);
    }

    @Before
    public void setup() {
        when(this.binder.bind(Serializable.class)).thenReturn(this.annotatedBindingBuilder);
        when(this.annotatedBindingBuilder.annotatedWith(Names.named("test"))).thenReturn(this.linkedBindingBuilder);
        when(this.linkedBindingBuilder.to(String.class)).thenReturn(this.scopedBindingBuilder);
        when(this.annotatedBindingBuilder.to(String.class)).thenReturn(this.scopedBindingBuilder);

    }

    @Test
    public void testSimpleBinding() {
        Binding binding = new Binding(new ClassSource(Serializable.class), new ClassTarget(String.class),
                Optional.empty(), Optional.empty());
        BindingModule module = new BindingModule(Lists.of(binding));

        module.configure(this.binder);

        verify(this.annotatedBindingBuilder).to(String.class);
    }

    @Test
    public void testAnnotatedBinding() {
        Binding binding = new Binding(new ClassSource(Serializable.class), new ClassTarget(String.class),
                Optional.of(Names.named("test")), Optional.empty());
        BindingModule module = new BindingModule(Collections.singletonList(binding));

        module.configure(this.binder);

        verify(this.annotatedBindingBuilder).annotatedWith(Names.named("test"));
        verify(this.linkedBindingBuilder).to(String.class);
    }

    @Test
    public void testScopedBinding() {
        Binding binding = new Binding(new ClassSource(Serializable.class), new ClassTarget(String.class),
                Optional.empty(), Optional.of(Singleton.class));
        BindingModule module = new BindingModule(Collections.singletonList(binding));

        module.configure(this.binder);

        verify(this.annotatedBindingBuilder).to(String.class);
        verify(this.scopedBindingBuilder).in(Singleton.class);
    }

    @Test
    public void testAnnotatedScopedBinding() {
        Binding binding = new Binding(new ClassSource(Serializable.class), new ClassTarget(String.class),
                Optional.of(Names.named("test")), Optional.of(Singleton.class));
        BindingModule module = new BindingModule(Collections.singletonList(binding));

        module.configure(this.binder);

        verify(this.annotatedBindingBuilder).annotatedWith(Names.named("test"));
        verify(this.linkedBindingBuilder).to(String.class);
        verify(this.scopedBindingBuilder).in(Singleton.class);
    }
}
