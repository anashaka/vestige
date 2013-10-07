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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.vestige.core.StackedHandler;
import com.googlecode.vestige.core.StackedHandlerUtils;
import com.googlecode.vestige.core.logger.VestigeLoggerFactory;
import com.googlecode.vestige.platform.logger.SLF4JLoggerFactoryAdapter;
import com.googlecode.vestige.platform.logger.SLF4JPrintStream;

/**
 * @author Gael Lalire
 */
public abstract class VestigeSystemAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(VestigeSystemAction.class);

    private static void unsetFinalField(final Field field, final Callable<Void> callable) throws Exception {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        boolean accessible = modifiersField.isAccessible();
        if (!accessible) {
            modifiersField.setAccessible(true);
        }
        try {
            int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            try {
                callable.call();
            } finally {
                modifiersField.setInt(field, modifiers);
            }
        } finally {
            if (!accessible) {
                modifiersField.setAccessible(false);
            }
        }
    }

    public void execute() throws Throwable {
        if (VestigeSystem.getSystem() != null) {
            // we are already protected
            vestigeSystemRun();
            return;
        }

        synchronized (VestigeLoggerFactory.class) {
            SLF4JLoggerFactoryAdapter factory = new SLF4JLoggerFactoryAdapter();
            factory.setNextHandler(VestigeLoggerFactory.getVestigeLoggerFactory());
            VestigeLoggerFactory.setVestigeLoggerFactory(factory);
        }

        VestigeProperties vestigeProperties;
        SLF4JPrintStream out;
        SLF4JPrintStream err;
        // avoid direct log
        synchronized (System.class) {
            out = new SLF4JPrintStream(true);
            out.setNextHandler(System.out);
            System.setOut(out);
            err = new SLF4JPrintStream(false);
            err.setNextHandler(System.err);
            System.setErr(err);
            vestigeProperties = new VestigeProperties(System.getProperties());
            System.setProperties(vestigeProperties);
        }

        VestigeProxySelector proxySelector;
        synchronized (ProxySelector.class) {
            proxySelector = new VestigeProxySelector();
            proxySelector.setNextHandler(ProxySelector.getDefault());
            ProxySelector.setDefault(proxySelector);
        }

        Map<String, StackedHandler<?>> urlFields = new HashMap<String, StackedHandler<?>>();
        urlFields.put("factory", new VestigeURLStreamHandlerFactory());
        urlFields.put("handlers", new VestigeURLHandlersHashTable());
        try {
            installFields(URL.class, urlFields);
        } catch (Exception e) {
            LOGGER.warn("Could not intercept URL.setURLStreamHandlerFactory", e);
            urlFields = null;
        }

        Map<String, StackedHandler<?>> urlConnectionFields = new HashMap<String, StackedHandler<?>>();
        urlConnectionFields.put("factory", new VestigeURLConnectionContentHandlerFactory());
        urlConnectionFields.put("handlers", new VestigeURLConnectionHandlersHashTable());
        try {
            installFields(URLConnection.class, urlConnectionFields);
        } catch (Exception e) {
            LOGGER.warn("Could not intercept URLConnection.setContentHandlerFactory", e);
            urlFields = null;
        }

        Map<String, StackedHandler<?>> driverManagerFields = new HashMap<String, StackedHandler<?>>();
        VestigeDriverVector writeDrivers = new VestigeDriverVector();
        VestigeDriverVector readDrivers = new VestigeDriverVector();
        writeDrivers.setReadDrivers(readDrivers);
        driverManagerFields.put("writeDrivers", writeDrivers);
        driverManagerFields.put("readDrivers", readDrivers);
        try {
            try {
                installFields(DriverManager.class, driverManagerFields);
                VestigeSystem.addVestigeSystemListeners(writeDrivers);
            } catch (NoSuchFieldException e) {
                LOGGER.trace("Missing field try another", e);
                writeDrivers = null;
                readDrivers = null;
                // JDK 7
                driverManagerFields.clear();
                driverManagerFields.put("registeredDrivers", new VestigeDriversCopyOnWriteArrayList());
                installFields(DriverManager.class, driverManagerFields);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not intercept DriverManager.registerDriver", e);
            driverManagerFields = null;
        }
        try {
            vestigeSystemRun();
        } finally {
            if (driverManagerFields != null) {
                if (writeDrivers != null) {
                    driverManagerFields.put("readDrivers", writeDrivers.getReadDrivers());
                    VestigeSystem.removeVestigeSystemListeners(writeDrivers);
                }
                uninstallFields(DriverManager.class, driverManagerFields);
            }

            if (urlConnectionFields != null) {
                uninstallFields(URLConnection.class, urlConnectionFields);
            }

            if (urlFields != null) {
                uninstallFields(URL.class, urlFields);
            }

            synchronized (ProxySelector.class) {
                ProxySelector.setDefault(StackedHandlerUtils.uninstallStackedHandler(proxySelector, ProxySelector.getDefault()));
            }

            synchronized (System.class) {
                System.setProperties(StackedHandlerUtils.uninstallStackedHandler(vestigeProperties, System.getProperties()));
                System.setOut(StackedHandlerUtils.uninstallStackedHandler(out, System.out));
                System.setErr(StackedHandlerUtils.uninstallStackedHandler(err, System.err));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void uninstallFields(final Class<?> clazz, final Map<String, StackedHandler<?>> valueByFieldName) throws Exception {
        synchronized (clazz) {
            for (final Entry<String, StackedHandler<?>> entry : valueByFieldName.entrySet()) {
                final Field declaredField = clazz.getDeclaredField(entry.getKey());
                final boolean accessible = declaredField.isAccessible();
                if (!accessible) {
                    declaredField.setAccessible(true);
                }
                try {
                    Callable<Void> callable = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            declaredField.set(null, StackedHandlerUtils.uninstallStackedHandler((StackedHandler<Object>) entry.getValue(), declaredField.get(null)));
                            return null;
                        }
                    };
                    if (Modifier.isFinal(declaredField.getModifiers())) {
                        unsetFinalField(declaredField, callable);
                    } else {
                        callable.call();
                    }
                } finally {
                    if (!accessible) {
                        declaredField.setAccessible(false);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void installFields(final Class<?> clazz, final Map<String, StackedHandler<?>> valueByFieldName) throws Exception {
        synchronized (clazz) {
            for (final Entry<String, StackedHandler<?>> entry : valueByFieldName.entrySet()) {
                final Field declaredField = clazz.getDeclaredField(entry.getKey());
                final boolean accessible = declaredField.isAccessible();
                if (!accessible) {
                    declaredField.setAccessible(true);
                }
                try {
                    Callable<Void> callable = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            StackedHandler value = entry.getValue();
                            value.setNextHandler(declaredField.get(null));
                            declaredField.set(null, value);
                            return null;
                        }
                    };
                    if (Modifier.isFinal(declaredField.getModifiers())) {
                        unsetFinalField(declaredField, callable);
                    } else {
                        callable.call();
                    }
                } finally {
                    if (!accessible) {
                        declaredField.setAccessible(false);
                    }
                }
            }
        }
    }

    public abstract void vestigeSystemRun() throws Throwable;

}
