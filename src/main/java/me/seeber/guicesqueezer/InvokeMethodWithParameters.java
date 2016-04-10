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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Provider;
import javax.inject.Qualifier;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;

public class InvokeMethodWithParameters extends Statement {

    private final FrameworkMethod testMethod;

    private final Object target;

    private Injector injector;

    public InvokeMethodWithParameters(FrameworkMethod testMethod, Object target, Injector injector) {
        this.testMethod = testMethod;
        this.target = target;
        this.injector = injector;
    }

    /**
     * @see org.junit.runners.model.Statement#evaluate()
     */
    @Override
    public void evaluate() throws Throwable {
        Method method = this.testMethod.getMethod();
        Type[] parameterTypes = method.getGenericParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameters.length; ++i) {
            boolean provider = false;
            Type parameterType = parameterTypes[i];

            if (parameterType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameterType;

                if (parameterizedType.getRawType() == Provider.class
                        || parameterizedType.getRawType() == com.google.inject.Provider.class) {
                    provider = true;
                    parameterType = parameterizedType.getActualTypeArguments()[0];
                }
            }

            Key<?> key = Key.get(parameterType);

            for (Annotation annotation : method.getParameterAnnotations()[i]) {
                if (annotation.annotationType().getAnnotation(Qualifier.class) != null
                        || annotation.annotationType().getAnnotation(BindingAnnotation.class) != null) {
                    key = Key.get(parameterType, annotation);
                    break;
                }
            }

            if (provider) {
                parameters[i] = this.injector.getProvider(key);
            }
            else {
                parameters[i] = this.injector.getInstance(key);
            }
        }

        this.testMethod.invokeExplosively(this.target, parameters);
    }
}
