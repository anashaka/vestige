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

import java.util.Map;

/**
 * @author Gael Lalire
 */
public class VestigeClassNotFoundException extends ClassNotFoundException {

    private static final long serialVersionUID = -1308142576374269964L;

    private Map<String, String> properties;

    public VestigeClassNotFoundException(final String className, final Map<String, String> properties) {
        super(className);
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

}
