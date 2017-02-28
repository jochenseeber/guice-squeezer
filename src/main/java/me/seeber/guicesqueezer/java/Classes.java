/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2016-2017, Jochen Seeber
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
package me.seeber.guicesqueezer.java;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Utilities for {@link Class} objects
 */
public abstract class Classes {

    /**
     * Get a method
     *
     * @param clazz Class to get method from
     * @param name Name of the method
     * @param parameterTypes Parameter types of the method
     * @return Method
     * @throws NoSuchElementException if the method does not exist
     */
    public static Method method(Class<?> clazz, String name, @NonNull Class<?>... parameterTypes)
            throws NoSuchElementException {
        try {
            @NonNull Method method = clazz.getMethod(name, parameterTypes);
            return method;
        }
        catch (NoSuchMethodException e) {
            throw new NoSuchElementException(format("Unknown method '%s'", name));
        }
    }

}
