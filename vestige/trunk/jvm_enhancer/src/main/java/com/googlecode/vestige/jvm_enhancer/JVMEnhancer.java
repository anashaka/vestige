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

package com.googlecode.vestige.jvm_enhancer;

import java.net.ProxySelector;

import com.btr.proxy.util.Logger;
import com.googlecode.vestige.core.StackedHandlerUtils;
import com.googlecode.vestige.core.Vestige;
import com.googlecode.vestige.core.VestigeExecutor;

/**
 * @author Gael Lalire
 */
public class JVMEnhancer {

    private static SystemProxySelector systemProxySelector;

    public static void start() {
        // INSTALL
        synchronized (ProxySelector.class) {
            Logger.setBackend(new SLF4JBackend());
            ProxySelector proxySelector = ProxySelector.getDefault();
            systemProxySelector = new SystemProxySelector();
            systemProxySelector.setNextHandler(proxySelector);
            ProxySelector.setDefault(systemProxySelector);
        }
    }

    public static void stop() {
        // UNINSTALL
        synchronized (ProxySelector.class) {
            ProxySelector proxySelector = ProxySelector.getDefault();
            ProxySelector newProxySelector = StackedHandlerUtils.uninstallStackedHandler(systemProxySelector, proxySelector);
            if (proxySelector != newProxySelector) {
                ProxySelector.setDefault(newProxySelector);
            }
            systemProxySelector = null;
            Logger.setBackend(null);
        }
    }

    public static void boot() throws InterruptedException {
        VestigeExecutor vestigeExecutor = new VestigeExecutor();
        Thread thread = vestigeExecutor.createWorker("bootstrap-sun-worker", true, 0);

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        try {
            // keep the context classloader in static field
            Class<?> appContextClass = vestigeExecutor.classForName(systemClassLoader, "sun.awt.AppContext");
            vestigeExecutor.invoke(systemClassLoader, appContextClass.getMethod("getAppContext"), null);
        } catch (Exception e) {
            // ignore
        }
        try {
            // keep the context classloader in static field
            vestigeExecutor.classForName(systemClassLoader, "javax.security.auth.Policy");
        } catch (Exception e) {
            // ignore
        }
        try {
            // keep an exception in static field
            vestigeExecutor.classForName(systemClassLoader, "com.sun.org.apache.xerces.internal.dom.DOMNormalizer");
        } catch (Exception e) {
            // ignore
        }
        try {
            // keep an exception in static field
            vestigeExecutor.classForName(systemClassLoader, "com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser");
        } catch (Exception e) {
            // ignore
        }
        try {
            // keep an exception in static field
            vestigeExecutor.classForName(systemClassLoader, "javax.management.remote.JMXServiceURL");
        } catch (Exception e) {
            // ignore
        }
        try {
            // sun.security.pkcs11.SunPKCS11 create thread (with the context
            // classloader of parent thread) and sun.security.jca.Providers keep
            // it in static field
            Class<?> providersClass = vestigeExecutor.classForName(systemClassLoader, "sun.security.jca.Providers");
            Object providerList = vestigeExecutor.invoke(systemClassLoader, providersClass.getMethod("getProviderList"), null);
            Class<?> providerListClass = vestigeExecutor.classForName(systemClassLoader, "sun.security.jca.ProviderList");
            vestigeExecutor.invoke(systemClassLoader, providerListClass.getMethod("getService", String.class, String.class), providerList, "MessageDigest", "SHA");
        } catch (Exception e) {
            // ignore
        }
        try {
            // sun.misc.GC create thread (with the context classloader of parent
            // thread)
            Class<?> gcClass = vestigeExecutor.classForName(systemClassLoader, "sun.misc.GC");
            vestigeExecutor.invoke(systemClassLoader, gcClass.getMethod("requestLatency", long.class), null, Long.valueOf(Long.MAX_VALUE - 1));
        } catch (Exception e) {
            // ignore
        }
        try {
            // com.sun.jndi.ldap.LdapPoolManager may create thread (with the context
            // classloader of parent thread)
            vestigeExecutor.classForName(systemClassLoader, "com.sun.jndi.ldap.LdapPoolManager");
        } catch (Exception e) {
            // ignore
        }
        thread.interrupt();
        thread.join();
    }

    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("expecting at least 1 arg : mainClass");
        }
        // preload some crappy classes to avoid classloader leak
        boot();
        // install system proxy selector
        start();
        try {
            String[] dargs = new String[args.length - 1];
            System.arraycopy(args, 1, dargs, 0, dargs.length);
            Vestige.runMain(Thread.currentThread().getContextClassLoader(), args[0], dargs);
        } finally {
            stop();
        }
    }

}
