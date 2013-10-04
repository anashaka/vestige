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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

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
    public synchronized String getProperty(final String key) {
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
    public synchronized Object setProperty(final String key, final String value) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.setProperty(key, value);
        }
        return system.getProperties().setProperty(key, value);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
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
