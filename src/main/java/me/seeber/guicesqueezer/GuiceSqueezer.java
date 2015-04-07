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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Runner for Guice based tests
 */
public class GuiceSqueezer extends BlockJUnit4ClassRunner {
    
    /**
     * Injector used to create test objects
     */
    private Injector injector;
    
    /**
     * Create a new runner
     * 
     * @param testClass Test class
     * @throws InitializationError if something goes wrong
     */
    public GuiceSqueezer(Class<?> testClass) throws InitializationError {
        super(testClass);
    }
    
    /**
     * Create a new test object from Guice
     * 
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        Object testObject = getInjector().getInstance(getTestClass().getJavaClass());
        return testObject;
    }
    
    /**
     * Get the injector used to create test objects
     * 
     * @return Injector
     */
    protected Injector getInjector() {
        if (this.injector == null) {
            ModuleBuilder module = setupModule(getTestClass().getJavaClass());
            this.injector = Guice.createInjector(module.get());
        }
        
        return this.injector;
    }
    
    /**
     * Setup the module used to create the test injector
     * 
     * This method inspects the test class and its super classes to detect all configured test modules. Currently, you
     * can configure modules in three ways:
     * <ol>
     * <li>Annotate a test class with a {@link TestModules} annotation
     * <li>Create a nested public static class that implements {@link Module} and has a no-arg constructor
     * <li>Annotate a public static method that returns a modile with {@link ProvidesModule}
     * </ol>
     * 
     * The returned module builder contains a module with all configured test modules. Modules configured in a
     * superclass are overridden by modules in subclasses, and modules from each class are combined into one module.
     * 
     * @param testClass Test class
     * @return Module builder for test module
     */
    protected ModuleBuilder setupModule(Class<?> testClass) {
        TestModules testModules = testClass.getAnnotation(TestModules.class);
        List<Module> modules = new ArrayList<>();
        
        if (testModules != null) {
            for (Class<? extends Module> moduleClass : testModules.value()) {
                Module module = createModule(moduleClass);
                modules.add(module);
            }
        }
        
        for (Class<?> nestedClass : testClass.getDeclaredClasses()) {
            if ((nestedClass.getModifiers() & Modifier.STATIC) != 0 && Module.class.isAssignableFrom(nestedClass)) {
                Class<? extends Module> moduleClass = nestedClass.asSubclass(Module.class);
                
                Module module = createModule(moduleClass);
                modules.add(module);
            }
        }
        
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.getAnnotation(ProvidesModule.class) != null) {
                Module module = createModule(method);
                modules.add(module);
            }
        }
        
        Module annotationModule = createAnnotationModule(testClass);
        modules.add(annotationModule);
        
        ModuleBuilder builder = ModuleBuilder.modules(modules);
        
        if (testClass.getSuperclass() != null) {
            ModuleBuilder inheritedModule = setupModule(testClass.getSuperclass());
            builder = inheritedModule.override(builder.get());
        }
        
        return builder;
    }
    
    protected Module createAnnotationModule(Class<?> testClass) {
        Map<Class<?>, Class<?>> bindings = new HashMap<>();
        
        for (Class<?> nestedClass : testClass.getDeclaredClasses()) {
            ProvidesType providesAnnotation = nestedClass.getAnnotation(ProvidesType.class);
            
            if (providesAnnotation != null) {
                if ((nestedClass.getModifiers() & Modifier.STATIC) == 0) {
                    throw new RuntimeException(format("Class '%s' annotated with ProvidesType must be static.",
                            nestedClass));
                }
                
                Class<?> interfaceClass = providesAnnotation.value();
                
                if (interfaceClass == Void.class) {
                    if (nestedClass.getInterfaces().length == 0) {
                        throw new RuntimeException(
                                format("ProvidesType annotation on class '%s' needs to specify the bound interface or the class needs to implement an interface.",
                                        nestedClass.getName()));
                    }
                    
                    interfaceClass = nestedClass.getInterfaces()[0];
                }
                
                bindings.put(interfaceClass, nestedClass);
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
    protected <M extends Module> M createModule(Class<M> moduleClass) {
        if ((moduleClass.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalStateException(format("Module class '%s' must be public.", moduleClass));
        }
        
        try {
            M module = moduleClass.newInstance();
            return module;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(format("Could not create module '%s'", moduleClass.getName()), e);
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
    protected Module createModule(Method method) {
        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            throw new RuntimeException(format("Module provider method '%s' must be static.", method));
        }
        
        if (!Module.class.isAssignableFrom(method.getReturnType())) {
            throw new RuntimeException(format("Module provider method '%s' must return a Module.", method));
        }
        
        if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalStateException(format("Module provider method '%s' must be public.", method));
        }
        
        try {
            Module module = (Module) method.invoke(null);
            return module;
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(format("Error calling module provider method '%s'", method), e);
        }
    }
    
}
