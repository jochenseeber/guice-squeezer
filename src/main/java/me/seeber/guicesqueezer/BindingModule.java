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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

import me.seeber.guicesqueezer.java.Argument;

/**
 * Module that configures dynamic bindings from a map
 */
public class BindingModule implements Module {

    /**
     * Binding that specifies how a source source is bound to a target in a {@link Module}, optionally specifying a
     * qualifier and a scope
     */
    public static class Binding {

        /**
         * Source for binding
         */
        public final BindingSource source;

        /**
         * Target for binding
         */
        public final BindingTarget target;

        /**
         * Optional qualifier
         */
        @Nullable
        public final Annotation qualifier;

        /**
         * Optional scope
         */
        @Nullable
        public final Class<? extends Annotation> scope;

        /**
         * Create a new binding
         *
         * @param source Source for binding
         * @param target Target for binding
         * @param qualifier Binding qualifier
         * @param scope Binding scope
         */
        public Binding(BindingSource source,
                BindingTarget target,
                Optional<Annotation> qualifier,
                Optional<Class<? extends Annotation>> scope) {
            this.source = source;
            this.qualifier = qualifier.orElse(null);
            this.scope = scope.orElse(null);
            this.target = target;
        }

        /**
         * Bind with the supplied {@link Binder}
         *
         * @param binder Binder to bind in
         */
        @SuppressWarnings({ "rawtypes" })
        public void bind(Binder binder) {
            AnnotatedBindingBuilder builder = this.source.bind(binder);
            LinkedBindingBuilder<?> linkedBuilder = builder;

            if (this.qualifier != null) {
                linkedBuilder = builder.annotatedWith(this.qualifier);
            }

            ScopedBindingBuilder scopedBuilder = this.target.bind(linkedBuilder);

            if (this.scope != null) {
                scopedBuilder.in(this.scope);
            }
        }

    }

    /**
     * Source for a binding
     */
    public interface BindingSource {

        /**
         * Create a binding builder for the source
         *
         * @param binder Binder
         * @param <T> Type to bind
         * @return Binding builder
         */
        public <T> AnnotatedBindingBuilder<T> bind(Binder binder);

    }

    /**
     * Target for a binding
     */
    public interface BindingTarget {

        /**
         * Create the scoped binding builder for the target
         *
         * @param builder Linked binding builder used for binding
         * @param <T> Type to bind
         * @return Scoped binding builder
         */
        public <T> ScopedBindingBuilder bind(LinkedBindingBuilder<T> builder);

    }

    /**
     * Class source for a binding
     */
    public static class ClassSource implements BindingSource {

        /**
         * Bound class
         */
        private final Class<?> type;

        /**
         * Create a new class source
         *
         * @param type Bound class
         */
        public ClassSource(Class<?> type) {
            this.type = type;
        }

        /**
         * @see me.seeber.guicesqueezer.BindingModule.BindingSource#bind(com.google.inject.Binder)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> AnnotatedBindingBuilder<T> bind(Binder binder) {
            AnnotatedBindingBuilder<T> builder = binder.bind((Class<T>) this.type);
            return builder;
        }

    }

    /**
     * Type source for a binding
     */
    public static class TypeSource implements BindingSource {

        /**
         * Bound type
         */
        private final Type type;

        /**
         * Create a new type source
         *
         * @param type Bound type
         */
        public TypeSource(Type type) {
            this.type = type;
        }

        /**
         * @see me.seeber.guicesqueezer.BindingModule.BindingSource#bind(com.google.inject.Binder)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> AnnotatedBindingBuilder<T> bind(Binder binder) {
            AnnotatedBindingBuilder<T> builder = (AnnotatedBindingBuilder<T>) binder.bind(TypeLiteral.get(this.type));
            return builder;
        }

    }

    /**
     * Class target for a binding
     */
    public static class ClassTarget implements BindingTarget {

        /**
         * Target type
         */
        private final Class<?> type;

        /**
         * Create a new class target
         *
         * @param type Target type
         */
        public ClassTarget(Class<?> type) {
            this.type = type;
        }

        /**
         * @see me.seeber.guicesqueezer.BindingModule.BindingTarget#bind(com.google.inject.binder.LinkedBindingBuilder)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> ScopedBindingBuilder bind(LinkedBindingBuilder<T> builder) {
            return builder.to((Class<? extends T>) this.type);
        }

    }

    /**
     * Provider method target for a binding
     */
    public static class ProviderMethodTarget implements BindingTarget {

        /**
         * Provider method
         */
        private final Method method;

        /**
         * Resolver used to resolver method arguments
         */
        private final ArgumentResolver argumentResolver;

        /**
         * Create a new provider method target
         *
         * @param method Provider method
         * @param argumentResolver Resolver used to resolver method arguments
         */
        public ProviderMethodTarget(Method method, ArgumentResolver argumentResolver) {
            this.method = method;
            this.argumentResolver = argumentResolver;
        }

        /**
         * @see me.seeber.guicesqueezer.BindingModule.BindingTarget#bind(com.google.inject.binder.LinkedBindingBuilder)
         */
        @Override
        public <T> ScopedBindingBuilder bind(LinkedBindingBuilder<T> builder) {
            MethodProvider<@NonNull T> provider = new MethodProvider<>(this.method, this.argumentResolver);
            return builder.toProvider(provider);
        }

    }

    /**
     * Provider that creates an instance by calling a {@link Provides} method
     *
     * @param <T> Provided type
     */
    protected static class MethodProvider<T> implements Provider<T> {

        /**
         * Method to call to create provided instance
         */
        private final Method method;

        /**
         * Resolver used to resolve method arguments
         */
        private final ArgumentResolver argumentResolver;

        /**
         * Injector used to resolve arguments
         */
        @Inject
        private Injector injector;

        /**
         * Create a new provider
         *
         * @param method Method to call to create provided instance
         * @param argumentResolver Resolver used to resolve method arguments
         */
        public MethodProvider(Method method, ArgumentResolver argumentResolver) {
            this.method = method;
            this.argumentResolver = argumentResolver;
        }

        /**
         * Provide the instance by calling the provider method
         *
         * @see javax.inject.Provider#get()
         */
        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            try {
                Object[] parameters = this.argumentResolver.resolveArguments(this.method, this.injector);
                @Nullable T object = (T) this.method.invoke(null, parameters);

                if (object == null) {
                    throw new NullPointerException(format("Provider method '%s' returned a null value", this.method));
                }

                return object;
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalStateException(format("Error calling provider mmethod '%s'", this.method));
            }
        }

    }

    /**
     * Bindings to configure
     */
    private final List<Binding> bindings;

    /**
     * Create a new binding module
     *
     * @param bindings Bindings to configure
     */
    public BindingModule(List<Binding> bindings) {
        this.bindings = new ArrayList<>(bindings);
    }

    /**
     * @see com.google.inject.Module#configure(com.google.inject.Binder)
     */
    @Override
    public void configure(Binder binder) {
        binder = Argument.notNull(binder, "binder");

        for (Binding binding : this.bindings) {
            binding.bind(binder);
        }
    }

}
