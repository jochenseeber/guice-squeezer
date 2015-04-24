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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Binder;
import com.google.inject.Module;

public class BindingModule implements Module {
    
    private final Map<Class<?>, Class<?>> bindings;
    
    public BindingModule(Map<Class<?>, Class<?>> bindings) {
        this.bindings = new HashMap<>(bindings);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void configure(Binder binder) {
        for (Entry<Class<?>, Class<?>> bindingEntry : this.bindings.entrySet()) {
            binder.bind((Class) bindingEntry.getKey()).to(bindingEntry.getValue());
        }
    }
    
}
