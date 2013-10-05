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

package com.googlecode.vestige.platform.system;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeProperties extends Properties implements StackedHandler<Properties> {

    private static final long serialVersionUID = 361157322432941610L;

    private Properties nextHandler;

    private static Properties getDefaults(final Properties properties) {
        Properties defaults = null;
        for (Object opropertyName : Collections.list(properties.propertyNames())) {
            if (opropertyName instanceof String) {
                String propertyName = (String) opropertyName;
                if (!properties.containsKey(propertyName)) {
                    if (defaults == null) {
                        defaults = new Properties();
                    }
                    defaults.put(propertyName, properties.getProperty(propertyName));
                }
            }
        }
        return defaults;
    }

    public VestigeProperties(final Properties properties) {
        super(getDefaults(properties));
        putAll(properties);
        this.nextHandler = properties;
    }

    @Override
    public Object put(final Object key, final Object value) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.put(key, value);
        }
        return system.getProperties().put(key, value);
    }

    @Override
    public Object get(final Object key) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.get(key);
        }
        return system.getProperties().get(key);
    }

    @Override
    public String getProperty(final String key) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.getProperty(key);
        }
        String property = system.getProperties().getProperty(key);
        if (property == null && defaults != null) {
            property = defaults.getProperty(key);
        }
        return property;
    }

    @Override
    public Object setProperty(final String key, final String value) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.setProperty(key, value);
        }
        return system.getProperties().setProperty(key, value);
    }

    @Override
    public boolean contains(final Object value) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.contains(value);
        }
        return system.getProperties().contains(value);
    }

    @Override
    public boolean containsKey(final Object key) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.containsKey(key);
        }
        return system.getProperties().containsKey(key);
    }

    @Override
    public Enumeration<Object> elements() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.elements();
        }
        return system.getProperties().elements();
    }

    @Override
    public Set<java.util.Map.Entry<Object, Object>> entrySet() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.entrySet();
        }
        return system.getProperties().entrySet();
    }

    @Override
    public Collection<Object> values() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.values();
        }
        return system.getProperties().values();
    }

    @Override
    public boolean isEmpty() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.isEmpty();
        }
        return system.getProperties().isEmpty();
    }

    @Override
    public Set<Object> keySet() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.keySet();
        }
        return system.getProperties().keySet();
    }

    @Override
    public String toString() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.toString();
        }
        return system.getProperties().toString();
    }

    @Override
    public int size() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.size();
        }
        return system.getProperties().size();
    }

    @Override
    public void clear() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            super.clear();
        } else {
            system.getProperties().clear();
        }
    }

    @Override
    public Object remove(final Object key) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.remove(key);
        }
        return system.getProperties().remove(key);
    }

    @Override
    public Enumeration<Object> keys() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.keys();
        }
        return system.getProperties().keys();
    }

    @Override
    public Properties getNextHandler() {
        return nextHandler;
    }

    @Override
    public void setNextHandler(final Properties nextHandler) {
        this.nextHandler = nextHandler;
    }

}
