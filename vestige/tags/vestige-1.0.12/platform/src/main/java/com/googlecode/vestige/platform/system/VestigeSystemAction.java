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

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.vestige.core.StackedHandler;
import com.googlecode.vestige.core.StackedHandlerUtils;
import com.googlecode.vestige.core.logger.VestigeLoggerFactory;
import com.googlecode.vestige.platform.logger.SLF4JLoggerFactoryAdapter;
import com.googlecode.vestige.platform.logger.SLF4JPrintStream;
import com.googlecode.vestige.platform.system.interceptor.VestigeCopyOnWriteArrayList;
import com.googlecode.vestige.platform.system.interceptor.VestigeDriverVector;
import com.googlecode.vestige.platform.system.interceptor.VestigeInputStream;
import com.googlecode.vestige.platform.system.interceptor.VestigePrintStream;
import com.googlecode.vestige.platform.system.interceptor.VestigeProperties;
import com.googlecode.vestige.platform.system.interceptor.VestigeProxySelector;
import com.googlecode.vestige.platform.system.interceptor.VestigeURLConnectionContentHandlerFactory;
import com.googlecode.vestige.platform.system.interceptor.VestigeURLConnectionHandlersHashTable;
import com.googlecode.vestige.platform.system.interceptor.VestigeURLHandlersHashTable;
import com.googlecode.vestige.platform.system.interceptor.VestigeURLStreamHandlerFactory;

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

    public void execute() throws Exception {
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

        VestigeSystem vestigeSystem = new VestigeSystem();
        VestigeSystem.setFallbackVestigeSystem(vestigeSystem);

        VestigeProperties vestigeProperties;
        VestigePrintStream out;
        VestigePrintStream err;
        VestigeInputStream in;
        // avoid direct log
        synchronized (System.class) {
            out = new VestigePrintStream(new SLF4JPrintStream(true, System.out)) {

                @Override
                public PrintStream getPrintStream() {
                    return VestigeSystem.getSystem().getOut();
                }
            };
            vestigeSystem.setOut(out.getNextHandler());
            System.setOut(out);

            err = new VestigePrintStream(new SLF4JPrintStream(false, System.err)) {

                @Override
                public PrintStream getPrintStream() {
                    return VestigeSystem.getSystem().getErr();
                }
            };
            vestigeSystem.setErr(err.getNextHandler());
            System.setErr(err);

            in = new VestigeInputStream(System.in) {

                @Override
                public InputStream getInputStream() {
                    return VestigeSystem.getSystem().getIn();
                }
            };
            vestigeSystem.setIn(in.getNextHandler());
            System.setIn(in);

            vestigeProperties = new VestigeProperties(System.getProperties()) {
                private static final long serialVersionUID = 5951701845063821073L;

                @Override
                public Properties getProperties() {
                    return VestigeSystem.getSystem().getProperties();
                }
            };
            vestigeSystem.setProperties(vestigeProperties.getNextHandler());
            System.setProperties(vestigeProperties);
        }

        VestigeProxySelector proxySelector;
        synchronized (ProxySelector.class) {
            proxySelector = new VestigeProxySelector(ProxySelector.getDefault()) {
                @Override
                public ProxySelector getProxySelector() {
                    return VestigeSystem.getSystem().getDefaultProxySelector();
                }
            };
            vestigeSystem.setDefaultProxySelector(proxySelector.getNextHandler());
            ProxySelector.setDefault(proxySelector);
        }

        Map<String, StackedHandler<?>> urlFields = new HashMap<String, StackedHandler<?>>();
        VestigeURLStreamHandlerFactory vestigeURLStreamHandlerFactory = new VestigeURLStreamHandlerFactory();
        urlFields.put("factory", vestigeURLStreamHandlerFactory);
        VestigeURLHandlersHashTable vestigeURLHandlersHashTable = new VestigeURLHandlersHashTable();
        urlFields.put("handlers", vestigeURLHandlersHashTable);
        try {
            installFields(URL.class, urlFields);
            vestigeSystem.setURLStreamHandlerFactory(vestigeURLStreamHandlerFactory.getNextHandler());
            vestigeSystem.setURLStreamHandlerByProtocol(vestigeURLHandlersHashTable.getNextHandler());
        } catch (Exception e) {
            LOGGER.warn("Could not intercept URL.setURLStreamHandlerFactory", e);
            urlFields = null;
        }

        Map<String, StackedHandler<?>> urlConnectionFields = new HashMap<String, StackedHandler<?>>();
        VestigeURLConnectionContentHandlerFactory vestigeURLConnectionContentHandlerFactory = new VestigeURLConnectionContentHandlerFactory();
        urlConnectionFields.put("factory", vestigeURLConnectionContentHandlerFactory);
        VestigeURLConnectionHandlersHashTable vestigeURLConnectionHandlersHashTable = new VestigeURLConnectionHandlersHashTable();
        urlConnectionFields.put("handlers", vestigeURLConnectionHandlersHashTable);
        try {
            installFields(URLConnection.class, urlConnectionFields);
            vestigeSystem.setURLConnectionContentHandlerFactory(vestigeURLConnectionContentHandlerFactory.getNextHandler());
            vestigeSystem.setURLConnectionContentHandlerByMime(vestigeURLConnectionHandlersHashTable.getNextHandler());
        } catch (Exception e) {
            LOGGER.warn("Could not intercept URLConnection.setContentHandlerFactory", e);
            urlFields = null;
        }

        Map<String, StackedHandler<?>> driverManagerFields = new HashMap<String, StackedHandler<?>>();
        VestigeDriverVector writeDrivers = new VestigeDriverVector();
        VestigeDriverVector readDrivers = new VestigeDriverVector(null, new WeakHashMap<VestigeSystem, Object>());
        writeDrivers.setReadDrivers(readDrivers);
        driverManagerFields.put("writeDrivers", writeDrivers);
        driverManagerFields.put("readDrivers", readDrivers);
        try {
            try {
                installFields(DriverManager.class, driverManagerFields);
                vestigeSystem.setWriteDrivers(writeDrivers.getNextHandler());
                vestigeSystem.setReadDrivers(new WeakHashMap<Object, Vector<Object>>());
                vestigeSystem.getReadDrivers().put(vestigeSystem, readDrivers.getNextHandler());
            } catch (NoSuchFieldException e) {
                LOGGER.trace("Missing field try another", e);
                writeDrivers = null;
                readDrivers = null;
                // JDK 7
                driverManagerFields.clear();
                VestigeCopyOnWriteArrayList vestigeDriversCopyOnWriteArrayList = new VestigeCopyOnWriteArrayList(null) {
                    private static final long serialVersionUID = -8739537725123134572L;

                    @Override
                    public CopyOnWriteArrayList<Object> getCopyOnWriteArrayList() {
                        return VestigeSystem.getSystem().getRegisteredDrivers();
                    }
                };
                driverManagerFields.put("registeredDrivers", vestigeDriversCopyOnWriteArrayList);
                installFields(DriverManager.class, driverManagerFields);
                vestigeSystem.setRegisteredDrivers(vestigeDriversCopyOnWriteArrayList.getNextHandler());
            }
        } catch (Exception e) {
            LOGGER.warn("Could not intercept DriverManager.registerDriver", e);
            driverManagerFields = null;
        }
        try {
            vestigeSystemRun();
        } finally {
            if (driverManagerFields != null) {
                synchronized (DriverManager.class) {
                    if (writeDrivers != null) {
                        readDrivers = writeDrivers.getReadDrivers();
                        driverManagerFields.put("readDrivers", readDrivers);
                    }
                    uninstallFields(DriverManager.class, driverManagerFields);
                }
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
                System.setIn(StackedHandlerUtils.uninstallStackedHandler(in, System.in));
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

    public abstract void vestigeSystemRun() throws Exception;

}
