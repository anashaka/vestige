package com.googlecode.vestige.core;

import java.util.Map;

/**
 * @author gaellalire
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
