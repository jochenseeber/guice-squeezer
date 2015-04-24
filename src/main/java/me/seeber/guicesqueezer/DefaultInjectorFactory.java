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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Qualifier;

import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Default factory to create Guice injectors for test cases
 */
public class DefaultInjectorFactory implements InjectorFactory {
    
    /**
     * @see me.seeber.guicesqueezer.InjectorFactory#createTestClassInjector(java.lang.Class)
     */
    @Override
    public Injector createTestClassInjector(Class<?> testClass) {
        Module module = createTestClassModule(testClass);
        Injector injector = Guice.createInjector(module);
        return injector;
    }
    
    /**
     * @see me.seeber.guicesqueezer.InjectorFactory#createTestMethodInjector(java.lang.reflect.Method,
     *      com.google.inject.Injector)
     */
    @Override
    public Injector createTestMethodInjector(Method testMethod, Injector classInjector) {
        Module module = createTestMethodModule(testMethod);
        Injector injector = classInjector.createChildInjector(module);
        return injector;
    }
    
    /**
     * Create a module that contains bindings specified by {@link Bind} annotations
     * 
     * @param testClass Test class
     * @return Module with bindings
     */
    protected Module createModuleUsingBoundNestedClasses(Class<?> testClass) {
        Map<Class<?>, Class<?>> bindings = new HashMap<>();
        
        for (Class<?> nestedClass : testClass.getDeclaredClasses()) {
            Bind bindingAnnotation = nestedClass.getAnnotation(Bind.class);
            
            if (bindingAnnotation != null) {
                if ((nestedClass.getModifiers() & Modifier.STATIC) == 0) {
                    throw new IllegalArgumentException(format("Class '%s' annotated with '%s' must be static.",
                            nestedClass.getName(), Bind.class.getSimpleName()));
                }
                
                Class<?> boundType = bindingAnnotation.value();
                
                if (boundType == Void.class) {
                    if (nestedClass.getInterfaces().length > 0) {
                        boundType = nestedClass.getInterfaces()[0];
                    }
                    else if (nestedClass.getSuperclass() != Object.class) {
                        boundType = nestedClass.getSuperclass();
                    }
                    else {
                        throw new IllegalArgumentException(
                                format("Cannot determine bound type for class '%s' annotated with '%s'. If you do not specify the bound type in the annotation, the class must either implement an interface or extend a class other than Object that is then used as bound type.",
                                        nestedClass.getName(), Bind.class.getSimpleName()));
                    }
                }
                
                bindings.put(boundType, nestedClass);
            }
        }
        
        Module module = new BindingModule(bindings);
        return module;
    }
    
    /**
     * Create a test module from a module class.
     * 
     * The class must be public.
     * 
     * @param moduleClass Class of test module
     * @return Test module
     */
    protected Module createModuleFromClass(Class<? extends Module> moduleClass) {
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
            throw new IllegalArgumentException(format("Module class '%s' must have a no-arg constructor.", moduleClass));
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
    protected Module createTestClassModule(Class<?> testClass) {
        List<Module> modules = new ArrayList<>();
        
        Module annotationModule = createModuleUsingTestModulesAnnotations(testClass);
        modules.add(annotationModule);
        
        Module nestedClassModule = createModuleUsingNestedModuleClasses(testClass);
        modules.add(nestedClassModule);
        
        Module providerModule = createModuleUsingFactoryMethods(testClass);
        modules.add(providerModule);
        
        Module providedTypesModule = createModuleUsingBoundNestedClasses(testClass);
        modules.add(providedTypesModule);
        
        Module combinedModule = Modules.combine(modules);
        
        if (testClass.getSuperclass() != null) {
            Module superclassModule = createTestClassModule(testClass.getSuperclass());
            combinedModule = Modules.override(superclassModule).with(combinedModule);
        }
        
        return combinedModule;
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
        
        return Modules.combine(modules);
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
        
        return Modules.combine(modules);
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
    protected Module createTestMethodModule(Method testMethod) {
        List<Module> modules = new ArrayList<>();
        
        modules.add(createModuleUsingTestModulesAnnotations(testMethod));
        
        return Modules.combine(modules);
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
                Module module = createModuleFromClass(moduleClass);
                modules.add(module);
            }
        }
        
        return Modules.combine(modules);
    }
    
    /**
     * Checks that all methods annotated with {@link Test} are
     * <ul>
     * <li>Public and return void
     * <li>Have no parameter that is annotated with more than one {@link Qualifier} annotation
     * </ul>
     * 
     * @see me.seeber.guicesqueezer.InjectorFactory#validateTestMethods(List)
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
                            Throwable error = new IllegalArgumentException(format(
                                    "Test method '%s' has more than one qualifier annotation for parameter %d",
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
    public Statement createInvocationStatement(FrameworkMethod testMethod, Object test, Injector classInjector) {
        Statement statement = null;
        Method method = testMethod.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        
        if (parameterTypes.length == 0) {
            statement = new InvokeMethod(testMethod, test);
        }
        else {
            Injector injector = createTestMethodInjector(method, classInjector);
            Object[] parameters = new Object[parameterTypes.length];
            
            for (int i = 0; i < parameters.length; ++i) {
                Key<?> key = null;
                
                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                    if (annotation.annotationType().getAnnotation(Qualifier.class) != null
                            || annotation.annotationType().getAnnotation(BindingAnnotation.class) != null) {
                        key = Key.get(parameterTypes[i], annotation);
                        break;
                    }
                }
                
                if (key == null) {
                    key = Key.get(parameterTypes[i]);
                }
                
                Object parameter = injector.getInstance(key);
                parameters[i] = parameter;
            }
            
            statement = new InvokeMethodWithParameters(testMethod, test, parameters);
        }
        
        return statement;
    }
    
}
