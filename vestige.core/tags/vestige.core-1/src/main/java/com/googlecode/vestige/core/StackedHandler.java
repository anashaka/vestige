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


/**
 * Utility class for JVM wide handler like :
 * <ul>
 * <li> {@link java.net.ProxySelector}</li>
 * <li> {@link java.net.ResponseCache}</li>
 * <li> {@link java.net.CookieHandler}</li>
 * <li> {@link java.util.Locale}</li>
 * <li> {@link java.util.TimeZone}</li>
 * </ul>
 * <p>
 * If handler keeper does not have a getDefault method you can use
 * {@link StackedHandlerUtils#getDefaultHandler(Class)} and
 * {@link StackedHandlerUtils#setDefaultHandler(Class, Object)} </p>
 * Handler examples
 * <ul>
 * <li> {@link java.net.Authenticator} </li>
 * </ul>
 * @author Gael Lalire
 */
public interface StackedHandler<E> {

    E getNextHandler();

    void setNextHandler(E nextHandler);

}
