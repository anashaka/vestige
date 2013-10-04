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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 * @author Gael Lalire
 */
public class VestigeSystem {

    private static ThreadLocal<LinkedList<VestigeSystem>> vestigeSystems = new InheritableThreadLocal<LinkedList<VestigeSystem>>();

    private Map<String, URLStreamHandler> urlStreamHandlerByProtocol = new HashMap<String, URLStreamHandler>();

    private Map<String, ContentHandler> urlConnectionContentHandlerByMime = new HashMap<String, ContentHandler>();

    private URLStreamHandlerFactory urlStreamHandlerFactory;

    private ContentHandlerFactory contentHandlerFactory;

    private PrintStream out;

    private PrintStream err;

    private InputStream in;

    private Properties properties;

    private Vector<Object> driverVector = new Vector<Object>();

    private ProxySelector defaultProxySelector;

    public VestigeSystem(final VestigeSystem previousSystem) {
        urlStreamHandlerByProtocol = new HashMap<String, URLStreamHandler>();
        properties = new Properties();
        if (previousSystem == null) {
            out = System.out;
            err = System.err;
            in = System.in;
            properties.putAll(System.getProperties());
            defaultProxySelector = ProxySelector.getDefault();
        } else {
            urlStreamHandlerByProtocol.putAll(previousSystem.urlStreamHandlerByProtocol);
            urlStreamHandlerFactory = previousSystem.urlStreamHandlerFactory;
            out = previousSystem.out;
            err = previousSystem.err;
            in = previousSystem.in;
            properties.putAll(previousSystem.properties);
            driverVector.addAll(previousSystem.driverVector);
            defaultProxySelector = previousSystem.defaultProxySelector;
        }
    }

    public static VestigeSystem getSystem() {
        LinkedList<VestigeSystem> linkedList = vestigeSystems.get();
        if (linkedList == null) {
            return null;
        }
        return linkedList.getFirst();
    }

    public static void pushSystem(VestigeSystem vestigeSystem) {
        LinkedList<VestigeSystem> linkedList = vestigeSystems.get();
        if (linkedList == null) {
            linkedList = new LinkedList<VestigeSystem>();
            vestigeSystems.set(linkedList);
        }
        if (vestigeSystem == null) {
            VestigeSystem previousSystem = null;
            if (linkedList.size() != 0) {
                previousSystem = linkedList.getFirst();
            }
            vestigeSystem = new VestigeSystem(previousSystem);
        }
        linkedList.addFirst(vestigeSystem);
    }

    public static void popSystem() {
        LinkedList<VestigeSystem> linkedList = vestigeSystems.get();
        linkedList.removeFirst();
        if (linkedList.size() == 0) {
            vestigeSystems.remove();
        }
    }

    public Vector<Object> getDriverVector() {
        return driverVector;
    }

    public Map<String, ContentHandler> getURLConnectionContentHandlerByMime() {
        return urlConnectionContentHandlerByMime;
    }

    public Map<String, URLStreamHandler> getURLStreamHandlerByProtocol() {
        return urlStreamHandlerByProtocol;
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
        return contentHandlerFactory;
    }

    public void setURLConnectionContentHandlerFactory(final ContentHandlerFactory contentHandlerFactory) {
        this.contentHandlerFactory = contentHandlerFactory;
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

}
