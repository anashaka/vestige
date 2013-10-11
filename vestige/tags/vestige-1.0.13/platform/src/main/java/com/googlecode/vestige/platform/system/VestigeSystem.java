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
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.ProxySelector;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Gael Lalire
 */
public class VestigeSystem implements PublicVestigeSystem {

    private static ThreadLocal<VestigeSystemHolder> vestigeSystems = new InheritableThreadLocal<VestigeSystemHolder>();


    private Hashtable<String, URLStreamHandler> urlStreamHandlerByProtocol;

    private Hashtable<String, ContentHandler> urlConnectionContentHandlerByMime;

    private URLStreamHandlerFactory urlStreamHandlerFactory;

    private ContentHandlerFactory urlConnectionContentHandlerFactory;

    private PrintStream out;

    private PrintStream err;

    private InputStream in;

    private Properties properties;

    private CopyOnWriteArrayList<Object> registeredDrivers;

    private Vector<Object> writeDrivers;

    private Map<Object, Vector<Object>> readDrivers;

    private ProxySelector defaultProxySelector;

    public VestigeSystem() {
    }

    @SuppressWarnings("unchecked")
    public VestigeSystem(final VestigeSystem previousSystem) {
        // URL
        if (previousSystem.urlStreamHandlerByProtocol != null) {
            urlStreamHandlerByProtocol = new Hashtable<String, URLStreamHandler>();
            urlStreamHandlerByProtocol.putAll(previousSystem.urlStreamHandlerByProtocol);
        }
        urlStreamHandlerFactory = previousSystem.urlStreamHandlerFactory;
        // URLConnection
        if (previousSystem.urlConnectionContentHandlerByMime != null) {
            urlConnectionContentHandlerByMime = new Hashtable<String, ContentHandler>();
            urlConnectionContentHandlerByMime.putAll(previousSystem.urlConnectionContentHandlerByMime);
        }
        urlConnectionContentHandlerFactory = previousSystem.urlConnectionContentHandlerFactory;
        // System
        out = previousSystem.out;
        err = previousSystem.err;
        in = previousSystem.in;
        if (previousSystem.properties != null) {
            Properties defaults = null;
            for (Object opropertyName : Collections.list(previousSystem.properties.propertyNames())) {
                if (!previousSystem.properties.containsKey(opropertyName)) {
                    if (defaults == null) {
                        defaults = new Properties();
                    }
                    if (opropertyName instanceof String) {
                        String propertyName = (String) opropertyName;
                        defaults.put(propertyName, previousSystem.properties.getProperty(propertyName));
                    }
                }
            }
            properties = new Properties(defaults);
            properties.putAll(previousSystem.properties);
        }
        // DriverManager
        if (previousSystem.writeDrivers != null) {
            writeDrivers = (Vector<Object>) previousSystem.writeDrivers.clone();
            readDrivers = new WeakHashMap<Object, Vector<Object>>();
            readDrivers.put(this, (Vector<Object>) writeDrivers.clone());
        }
        if (previousSystem.registeredDrivers != null) {
            registeredDrivers = new CopyOnWriteArrayList<Object>(previousSystem.registeredDrivers);
        }
        // ProxySelector
        defaultProxySelector = previousSystem.defaultProxySelector;
    }

    private static VestigeSystem fallbackVestigeSystem;

    public static void setFallbackVestigeSystem(final VestigeSystem fallbackVestigeSystem) {
        VestigeSystem.fallbackVestigeSystem = fallbackVestigeSystem;
    }

    public static VestigeSystem getSystem() {
        VestigeSystemHolder vestigeSystemHolder = vestigeSystems.get();
        if (vestigeSystemHolder == null) {
            return fallbackVestigeSystem;
        }
        return vestigeSystemHolder.getVestigeSystem();
    }

    public static void pushSystem(final VestigeSystem vestigeSystem) {
        vestigeSystems.set(new VestigeSystemHolder(vestigeSystem, vestigeSystems.get()));
    }

    public static void popSystem() {
        VestigeSystemHolder vestigeSystemHolder = vestigeSystems.get();
        VestigeSystemHolder previous = vestigeSystemHolder.getPrevious();
        if (previous == null) {
            vestigeSystems.remove();
        } else {
            vestigeSystems.set(previous);
        }
        vestigeSystemHolder.unset();
    }

    public void setWriteDrivers(final Vector<Object> writeDrivers) {
        this.writeDrivers = writeDrivers;
    }

    public Vector<Object> getWriteDrivers() {
        return writeDrivers;
    }

    public void setReadDrivers(final Map<Object, Vector<Object>> readDrivers) {
        this.readDrivers = readDrivers;
    }

    public Map<Object, Vector<Object>> getReadDrivers() {
        return readDrivers;
    }

    public CopyOnWriteArrayList<Object> getRegisteredDrivers() {
        return registeredDrivers;
    }

    public URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return urlStreamHandlerFactory;
    }

    public Properties getProperties() {
        return properties;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    public InputStream getIn() {
        return in;
    }

    public ContentHandlerFactory getURLConnectionContentHandlerFactory() {
        return urlConnectionContentHandlerFactory;
    }

    public void setURLConnectionContentHandlerFactory(final ContentHandlerFactory contentHandlerFactory) {
        this.urlConnectionContentHandlerFactory = contentHandlerFactory;
    }

    public void setURLStreamHandlerFactory(final URLStreamHandlerFactory urlStreamHandlerFactory) {
        if (this.urlStreamHandlerFactory != null) {
            throw new Error("factory already defined");
        }
        this.urlStreamHandlerFactory = urlStreamHandlerFactory;
    }

    public void setOut(final PrintStream out) {
        this.out = out;
    }

    public void setErr(final PrintStream err) {
        this.err = err;
    }

    public void setIn(final InputStream in) {
        this.in = in;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public ProxySelector getDefaultProxySelector() {
        return defaultProxySelector;
    }

    public void setDefaultProxySelector(final ProxySelector defaultProxySelector) {
        this.defaultProxySelector = defaultProxySelector;
    }

    public void setRegisteredDrivers(final CopyOnWriteArrayList<Object> registeredDrivers) {
        this.registeredDrivers = registeredDrivers;
    }

    public Hashtable<String, URLStreamHandler> getURLStreamHandlerByProtocol() {
        return urlStreamHandlerByProtocol;
    }

    public void setURLStreamHandlerByProtocol(final Hashtable<String, URLStreamHandler> urlStreamHandlerByProtocol) {
        this.urlStreamHandlerByProtocol = urlStreamHandlerByProtocol;
    }

    public Hashtable<String, ContentHandler> getURLConnectionContentHandlerByMime() {
        return urlConnectionContentHandlerByMime;
    }

    public void setURLConnectionContentHandlerByMime(final Hashtable<String, ContentHandler> urlConnectionContentHandlerByMime) {
        this.urlConnectionContentHandlerByMime = urlConnectionContentHandlerByMime;
    }

}
