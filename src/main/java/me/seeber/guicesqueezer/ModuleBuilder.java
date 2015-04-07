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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Fluid interface to create {@link Guice} modules
 * 
 * This class allows you to create modules using a fluent interface, e.g.
 * 
 * module().install(firstModule, secondModule).override(thirdModile).get()
 */
public class ModuleBuilder {
    
    /**
     * Empty module that contains no bindings
     */
    protected static final Module EMPTY_MODULE = new AbstractModule() {
        @Override
        protected void configure() {
            // Empty
        }
    };
    
    /**
     * Empty module builder
     */
    public static final ModuleBuilder EMPTY = new ModuleBuilder();
    
    /**
     * Current module that was built
     */
    private final Module module;
    
    /**
     * Create a new module builder from multiple modules
     * 
     * @param modules Modules to add to the builder
     * @return Module builder
     */
    public static ModuleBuilder module(Module... modules) {
        return new ModuleBuilder(modules);
    }
    
    /**
     * Create a new module builder from collection of modules
     * 
     * @param modules Modules to add to the builder
     * @return Module builder
     */
    public static ModuleBuilder modules(Iterable<? extends Module> modules) {
        return new ModuleBuilder(Modules.combine(modules));
    }
    
    /**
     * Get an empty module builder
     * 
     * @return Empty module builder
     */
    public static ModuleBuilder empty() {
        return EMPTY;
    }
    
    /**
     * Create a new module builder
     * 
     * @param modules Modules to add to the builder
     */
    public ModuleBuilder(Module... modules) {
        this.module = Modules.combine(modules);
    }
    
    /**
     * Create a new module builder by adding some modules to the current builder's module
     * 
     * @param additionalModules Additional modules that the new builder should contain
     * @return Module builder containing the builder's current module and the additional ones.
     */
    public ModuleBuilder install(Module... additionalModules) {
        Module[] modules = new Module[additionalModules.length + 1];
        modules[0] = this.module;
        System.arraycopy(additionalModules, 0, modules, 1, additionalModules.length);
        
        Module result = Modules.combine(modules);
        return new ModuleBuilder(result);
    }
    
    /**
     * Create a new module builder by overriding the current builder's module with additional modules
     * 
     * @param additionalModules Additional modules that the new builder should contain
     * @return Module builder containing the builder's current module and the additional ones.
     */
    public ModuleBuilder override(Module... additionalModules) {
        Module result = Modules.override(this.module).with(additionalModules);
        return new ModuleBuilder(result);
    }
    
    /**
     * Get the current module
     * 
     * @return Current module
     */
    public Module get() {
        return this.module;
    }
    
}
