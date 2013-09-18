/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.vestige.core;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Gael Lalire
 */
public final class StackedHandlerUtils {

    private static Map<Class<?>, Object> DEFAULT_HANDLER_BY_CLASS = new HashMap<Class<?>, Object>();

    private StackedHandlerUtils() {
    }

    public static <E> E setDefaultHandler(final Class<E> clazz, final E handler) {
        return clazz.cast(DEFAULT_HANDLER_BY_CLASS.put(clazz, handler));
    }

    public static <E> E getDefaultHandler(final Class<E> clazz) {
        return clazz.cast(DEFAULT_HANDLER_BY_CLASS.get(clazz));
    }

    @SuppressWarnings("unchecked")
    public static <E> E uninstallStackedHandler(final StackedHandler<E> stackedHandler, final E usedHandler) {
        if (usedHandler == stackedHandler) {
            return stackedHandler.getNextHandler();
        }
        if (!(usedHandler instanceof StackedHandler)) {
            return usedHandler;
        }
        StackedHandler<E> currentStackedHandler = (StackedHandler<E>) usedHandler;
        E nextHandler = currentStackedHandler.getNextHandler();
        while (nextHandler != stackedHandler) {
            if (nextHandler instanceof StackedHandler) {
                currentStackedHandler = (StackedHandler<E>) nextHandler;
                nextHandler = currentStackedHandler.getNextHandler();
            } else {
                // not found
                return usedHandler;
            }
        }
        // nextHandler == uninstallStackedHandler
        if (nextHandler instanceof StackedHandler) {
            StackedHandler<E> nextStackedHandler = (StackedHandler<E>) nextHandler;
            currentStackedHandler.setNextHandler(nextStackedHandler.getNextHandler());
        } else {
            currentStackedHandler.setNextHandler(null);
        }
        return usedHandler;
    }

}
