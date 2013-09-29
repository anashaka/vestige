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

package com.googlecode.vestige.core.callable;

import java.util.concurrent.Callable;

/**
 * @author Gael Lalire
 */
public class CreateThread implements Callable<Thread> {

    private String name;

    private Runnable runnable;

    public CreateThread(final String name, final Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
    }

    public Thread call() {
        return new Thread(runnable, name);
    }

}
