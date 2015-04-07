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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.inject.Provider;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Default resolver used to resolve method arguments with an {@link Injector}
 */
public class DefaultArgumentResolver implements ArgumentResolver {

    /**
     * Annotation inspector used to inspect binding annotations
     */
    private final AnnotationInspector annotationInspector;

    /**
     * Create a new argument resolver
     *
     * @param annotationInspector Annotation inspector used to inspect binding annotations
     */
    public DefaultArgumentResolver(AnnotationInspector annotationInspector) {
        this.annotationInspector = annotationInspector;
    }

    /**
     * @see me.seeber.guicesqueezer.ArgumentResolver#resolveArguments(java.lang.reflect.Method,
     *      com.google.inject.Injector)
     */
    @Override
    public Object[] resolveArguments(Method method, Injector injector) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameterTypes.length];

        for (int i = 0; i < arguments.length; ++i) {
            boolean provider = false;
            Parameter parameter = parameters[i];

            Type parameterType = parameter.getParameterizedType();

            if (parameterType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameterType;

                if (parameterizedType.getRawType() == Provider.class
                        || parameterizedType.getRawType() == com.google.inject.Provider.class) {
                    provider = true;
                    parameterType = parameterizedType.getActualTypeArguments()[0];
                }
            }

            Key<?> key = Key.get(parameterType);
            Optional<Annotation> qualifier = this.annotationInspector.getQualifier(parameter);

            if (qualifier.isPresent()) {
                key = Key.get(parameterType, qualifier.get());
            }

            if (provider) {
                arguments[i] = injector.getProvider(key);
            }
            else {
                arguments[i] = injector.getInstance(key);
            }
        }

        return arguments;
    }

}
