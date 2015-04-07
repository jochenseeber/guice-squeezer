
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
