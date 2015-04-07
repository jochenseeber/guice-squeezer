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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Qualifier;
import javax.inject.Scope;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.ScopeAnnotation;
import com.google.inject.util.Modules;

import me.seeber.guicesqueezer.BindingModule.Binding;
import me.seeber.guicesqueezer.BindingModule.ClassSource;
import me.seeber.guicesqueezer.BindingModule.ClassTarget;
import me.seeber.guicesqueezer.BindingModule.ProviderMethodTarget;
import me.seeber.guicesqueezer.BindingModule.TypeSource;
import me.seeber.guicesqueezer.java.Assert;

/**
 * Default composition that inspects annotations to determine the test composition
 */
public class DefaultTestComposition implements TestComposition {

    /**
     * Annotation inspector used to inspect binding annotations
     */
    private final AnnotationInspector annotationInspector;

    /**
     * Parameter resolver used to resolve method arguments
     */
    private final ArgumentResolver argumentResolver;

    /**
     * Create a new injector factory
     */
    public DefaultTestComposition() {
        this.annotationInspector = new DefaultAnnotationInspector();
        this.argumentResolver = new DefaultArgumentResolver(this.annotationInspector);
    }

    /**
     * Create a new injector factory
     *
     * @param annotationInspector Annotation inspector used to inspect binding annotations
     * @param argumentResolver Parameter resolver used to resolve method arguments
     */
    public DefaultTestComposition(AnnotationInspector annotationInspector, ArgumentResolver argumentResolver) {
        this.annotationInspector = annotationInspector;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Create a module that contains bindings specified by {@link Bind} annotations
     *
     * @param testClass Test class
     * @return Module with bindings
     */
    protected Module createModuleUsingBoundNestedClasses(Class<?> testClass) {
        List<Binding> bindings = new ArrayList<>();

        for (Class<?> nestedClass : testClass.getDeclaredClasses()) {
            Bind bindingAnnotation = nestedClass.getAnnotation(Bind.class);

            if (bindingAnnotation != null) {
                if ((nestedClass.getModifiers() & Modifier.STATIC) == 0) {
                    throw new IllegalArgumentException(format("Class '%s' annotated with '%s' must be static.",
                            nestedClass.getName(), Bind.class.getSimpleName()));
                }

                @NonNull Class<?> boundType = bindingAnnotation.value();

                if (boundType == Void.class) {
                    if (nestedClass.getInterfaces().length > 0) {
                        boundType = nestedClass.getInterfaces()[0];
                    }
                    else if (nestedClass.getSuperclass() != Object.class) {
                        boundType = Assert.notNull(nestedClass.getSuperclass(), "superclass");
                    }
                    else {
                        throw new IllegalArgumentException(
                                format("Cannot determine bound type for class '%s' annotated with '%s'. If you do "
                                        + "not specify the bound type in the annotation, the class must either "
                                        + "implement an interface or extend a class other than Object that is then "
                                        + "used as bound type.", nestedClass.getName(), Bind.class.getSimpleName()));
                    }
                }

                Optional<Annotation> qualifier = this.annotationInspector.getQualifier(nestedClass);
                Optional<Class<? extends Annotation>> scope = getScope(nestedClass);

                Binding binding = new Binding(new ClassSource(boundType), new ClassTarget(nestedClass), qualifier,
                        scope);
                bindings.add(binding);
            }
        }

        Module module = new BindingModule(bindings);
        return module;
    }

    /**
     * Create a module that contains bindings specified by {@link Provides} methods
     *
     * @param testClass Class to test
     * @return Module with bindings
     */
    protected Module createModuleUsingProviderMethods(Class<?> testClass) {
        List<Binding> bindings = new ArrayList<>();

        for (Method method : testClass.getDeclaredMethods()) {
            Provides providesAnnotation = method.getAnnotation(Provides.class);

            if (providesAnnotation != null) {
                if ((method.getModifiers() & Modifier.STATIC) == 0) {
                    throw new IllegalArgumentException(format("Method '%s' annotated with '%s' must be static.",
                            method.getName(), Provides.class.getSimpleName()));
                }

                if (method.getReturnType() == Void.class) {
                    throw new IllegalArgumentException(
                            format("Method '%s' annotated with '%s' must have a return type.", method.getName(),
                                    Provides.class.getSimpleName()));
                }

                Type boundType = method.getReturnType();

                Optional<Annotation> qualifier = this.annotationInspector.getQualifier(method);
                Optional<Class<? extends Annotation>> scope = getScope(method);

                Binding binding = new Binding(new TypeSource(boundType),
                        new ProviderMethodTarget(method, this.argumentResolver), qualifier, scope);
                bindings.add(binding);
            }
        }

        Module module = new BindingModule(bindings);
        return module;
    }

    /**
     * Get the scope annotation for an annotated element
     *
     * Looks for the scope annotation of a binding, i.e. an annotation whose definition is annotated with {@link Scope}
     * or {@link ScopeAnnotation}.
     *
     * @param annotatedElement Annotated element
     * @return Scope annotation
     * @throws IllegalArgumentException if there is more than one matching annotation
     */
    protected Optional<Class<? extends Annotation>> getScope(AnnotatedElement annotatedElement)
            throws IllegalArgumentException {
        Annotation scopeAnnotation = null;

        for (Annotation annotation : annotatedElement.getAnnotations()) {
            if (annotation.annotationType().getAnnotation(Scope.class) != null
                    || annotation.annotationType().getAnnotation(ScopeAnnotation.class) != null) {
                if (scopeAnnotation == null) {
                    scopeAnnotation = annotation;
                }
                else {
                    throw new IllegalArgumentException(
                            format("Element '%s' has more than one scope annotation", annotatedElement));
                }
            }
        }

        Optional<Class<? extends Annotation>> scope = Optional.ofNullable(scopeAnnotation).map(a -> a.annotationType());
        return scope;
    }

    /**
     * Create a test module from a module class.
     *
     * The class must be public.
     *
     * @param moduleClass Class of test module
     * @return Test module
     */
    protected Module createModuleFromClass(Class<? extends @Nullable Module> moduleClass) {
        if (moduleClass.getEnclosingClass() != null && (moduleClass.getModifiers() & Modifier.STATIC) == 0) {
            throw new IllegalArgumentException(format("Nested module class '%s' must be static.", moduleClass));
        }

        if ((moduleClass.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException(format("Module class '%s' must be public.", moduleClass));
        }

        try {
            moduleClass.getConstructor();
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    format("Module class '%s' must have a no-arg constructor.", moduleClass));
        }

        try {
            Module module = moduleClass.newInstance();
            return module;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(format("Could not create module '%s'", moduleClass.getName()), e);
        }
    }

    /**
     * Create a test module by calling a factory method
     *
     * The method must be public, static and return an object of type {@link Module}.
     *
     * @param method Method that returns the test module
     * @return Test module
     */
    protected Module createModuleFromMethod(Method method) {
        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            throw new IllegalArgumentException(format("Module provider method '%s' must be static.", method));
        }

        if (!Module.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalArgumentException(format("Module provider method '%s' must return a Module.", method));
        }

        if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException(format("Module provider method '%s' must be public.", method));
        }

        try {
            Module module = (Module) method.invoke(null);

            if (module == null) {
                throw new NullPointerException(format("Module provider method '%s' must not return null.", method));
            }

            return module;
        }
        catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new IllegalArgumentException(format("Error calling module provider method '%s'", method), e);
        }
    }

    /**
     * Setup the module used to create the test injector
     *
     * This method inspects the test class and its super classes to detect all configured test modules. Currently, you
     * can configure modules in three ways:
     * <ol>
     * <li>Annotate a test class with a {@link TestModules} annotation
     * <li>Create a nested public static class that implements {@link Module} and has a no-arg constructor
     * <li>Create a public static method that returns a module
     * </ol>
     *
     * The returned module builder contains a module with all configured test modules. Modules configured in a
     * superclass are overridden by modules in subclasses, and modules from each class are combined into one module.
     *
     * @param testClass Test class
     * @return Module builder for test module
     */
    @Override
    public Module createTestClassModule(Class<?> testClass) {
        Module annotationModule = createModuleUsingTestModulesAnnotations(testClass);

        Module nestedClassModule = createModuleUsingNestedModuleClasses(testClass);
        Module providerModule = createModuleUsingFactoryMethods(testClass);
        Module boundTypesModule = createModuleUsingBoundNestedClasses(testClass);
        Module providedTypesModule = createModuleUsingProviderMethods(testClass);

        Module classModule = Modules.override(annotationModule).with(nestedClassModule, providerModule,
                boundTypesModule, providedTypesModule);

        Class<?> superclass = testClass.getSuperclass();

        if (superclass != null) {
            Module superclassModule = createTestClassModule(superclass);
            classModule = Modules.override(superclassModule).with(classModule);
        }

        return classModule;
    }

    /**
     * Build a module from factory methods
     *
     * Checks all static methods for those returning a Module and having no parameters. These are invoked, and all
     * returned modules are combined into the result.
     *
     * @param testClass Test class
     * @return Module with all modules created from factory methods
     */
    protected Module createModuleUsingFactoryMethods(Class<?> testClass) {
        List<Module> modules = new ArrayList<>();

        for (Method method : testClass.getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.STATIC) != 0 && method.getParameterCount() == 0
                    && Module.class.isAssignableFrom(method.getReturnType())) {
                Module module = createModuleFromMethod(method);
                modules.add(module);
            }
        }

        Module module = Modules.combine(modules);
        return module;
    }

    /**
     * Build a module from nested module classes
     *
     * Checks all nested static classes for those that implement {@link Module} and instantiates them. The created
     * modules are combined into the result.
     *
     * @param testClass Test class
     * @return Module with all modules created from nested classes
     */
    protected Module createModuleUsingNestedModuleClasses(Class<?> testClass) {
        List<Module> modules = new ArrayList<>();

        for (Class<?> nestedClass : testClass.getDeclaredClasses()) {
            if ((nestedClass.getModifiers() & Modifier.STATIC) != 0 && Module.class.isAssignableFrom(nestedClass)) {
                Class<? extends Module> moduleClass = nestedClass.asSubclass(Module.class);
                Module nestedModule = createModuleFromClass(moduleClass);
                modules.add(nestedModule);
            }
        }

        Module module = Modules.combine(modules);
        return module;
    }

    /**
     * Setup the module used to create the test method injector
     *
     * This method inspects the method to detect all configured test modules. Currently, you can configure modules in
     * three ways:
     * <ol>
     * <li>Annotate the method with a {@link TestModules} annotation
     * </ol>
     *
     * The returned module builder contains a module with all configured test modules.
     *
     * @param testMethod Test method
     * @return Module builder for test method
     */
    @Override
    public Module createTestMethodModule(Method testMethod) {
        List<Module> modules = new ArrayList<>();

        modules.add(createModuleUsingTestModulesAnnotations(testMethod));

        Module module = Modules.combine(modules);
        return module;
    }

    /**
     * Create modules specified by a {@link TestModules} annotation
     *
     * @param element Annotated element
     * @return Modules specified by annotation
     */
    protected Module createModuleUsingTestModulesAnnotations(AnnotatedElement element) {
        List<Module> modules = new ArrayList<>();
        TestModules testModules = element.getAnnotation(TestModules.class);

        if (testModules != null) {
            for (Class<? extends Module> moduleClass : testModules.value()) {
                if (moduleClass == null) {
                    throw new NullPointerException(format("Annotation '%s' must not contain null values",
                            TestModules.class.getSimpleName(), testModules.value()));
                }

                Module module = createModuleFromClass(moduleClass);
                modules.add(module);
            }
        }

        Module module = Modules.combine(modules);
        return module;
    }

    /**
     * Checks that all methods annotated with {@link Test} are
     * <ul>
     * <li>Public and return void
     * <li>Have no parameter that is annotated with more than one {@link Qualifier} annotation
     * </ul>
     *
     * @see me.seeber.guicesqueezer.TestComposition#validateTestMethods(List)
     */
    @Override
    public List<Throwable> validateTestMethods(List<FrameworkMethod> testMethods) {
        List<Throwable> errors = new ArrayList<>();

        for (FrameworkMethod testMethod : testMethods) {
            testMethod.validatePublicVoid(false, errors);

            for (int i = 0; i < testMethod.getMethod().getParameterCount(); ++i) {
                Annotation qualifierAnnotation = null;

                for (Annotation parameterAnnotation : testMethod.getMethod().getParameterAnnotations()[i]) {
                    if (parameterAnnotation.annotationType().getAnnotation(Qualifier.class) != null
                            || parameterAnnotation.annotationType().getAnnotation(BindingAnnotation.class) != null) {
                        if (qualifierAnnotation == null) {
                            qualifierAnnotation = parameterAnnotation;
                        }
                        else {
                            Throwable error = new IllegalArgumentException(
                                    format("Test method '%s' has more than one qualifier annotation for parameter %d",
                                            testMethod.getMethod(), i));
                            errors.add(error);
                        }
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Get a method invoker
     *
     * Change the default JUnit behavior to add parameters
     */
    @Override
    public Statement createInvocationStatement(FrameworkMethod testMethod, Object test, Injector injector) {
        InvokeWithParametersStatement statement = new InvokeWithParametersStatement(testMethod, test,
                this.argumentResolver, injector);
        return statement;
    }

}
