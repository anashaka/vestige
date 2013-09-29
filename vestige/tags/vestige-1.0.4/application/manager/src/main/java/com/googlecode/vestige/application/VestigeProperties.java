/*
 * This file is part of Vestige.
 *
 * Vestige is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vestige is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vestige.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.vestige.application;

import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * @author Gael Lalire
 */
public class VestigeProperties extends Properties {

    private static final long serialVersionUID = 361157322432941610L;

    private transient ThreadLocal<LinkedList<Map<String, String>>> localProperties = new InheritableThreadLocal<LinkedList<Map<String, String>>>();

    public VestigeProperties(final Properties properties) {
        super(properties);
    }

    public void pushApplication(final Map<String, String> properties) {
        LinkedList<Map<String, String>> linkedList = localProperties.get();
        if (linkedList == null) {
            linkedList = new LinkedList<Map<String, String>>();
            localProperties.set(linkedList);
        }
        linkedList.addFirst(properties);
    }

    public void popApplication() {
        LinkedList<Map<String, String>> linkedList = localProperties.get();
        linkedList.removeFirst();
        if (linkedList.size() == 0) {
            localProperties.remove();
        }
    }

    @Override
    public synchronized String getProperty(final String key) {
        LinkedList<Map<String, String>> linkedList = localProperties.get();
        String value = null;
        if (linkedList != null) {
            value = linkedList.getFirst().get(key);
        }
        if (value != null) {
            return value;
        }
        return super.getProperty(key);
    }

    @Override
    public synchronized Object setProperty(final String key, final String value) {
        LinkedList<Map<String, String>> linkedList = localProperties.get();
        if (linkedList != null) {
            return linkedList.getFirst().put(key, value);
        }
        return super.setProperty(key, value);
    }

}
