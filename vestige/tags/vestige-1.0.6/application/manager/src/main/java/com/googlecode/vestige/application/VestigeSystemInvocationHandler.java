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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.vestige.platform.system.VestigeSystem;

/**
 * @author Gael Lalire
 */
public class VestigeSystemInvocationHandler implements InvocationHandler {

    private Map<String, Map<List<Class<?>>, Method>> delegateMethods = new HashMap<String, Map<List<Class<?>>,Method>>();

    private VestigeSystem vestigeSystem;

    public void add(final Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return;
        }
        String methodName = method.getName();
        Map<List<Class<?>>, Method> map = delegateMethods.get(methodName);
        if (map == null) {
            map = new HashMap<List<Class<?>>, Method>();
            delegateMethods.put(methodName, map);
        }
        map.put(Arrays.asList(method.getParameterTypes()), method);
    }

    public VestigeSystemInvocationHandler(final VestigeSystem vestigeSystem) {
        this.vestigeSystem = vestigeSystem;
        for (Method method : VestigeSystem.class.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String methodName = method.getName();
            Map<List<Class<?>>, Method> map = delegateMethods.get(methodName);
            if (map == null) {
                map = new HashMap<List<Class<?>>, Method>();
                delegateMethods.put(methodName, map);
            }
            map.put(Arrays.asList(method.getParameterTypes()), method);
        }
    }

    public static Object createProxy(final ClassLoader classLoader, final Class<?> itf, final VestigeSystem vestigeSystem) {
        return Proxy.newProxyInstance(classLoader, new Class<?>[] {itf}, new VestigeSystemInvocationHandler(vestigeSystem));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Map<List<Class<?>>, Method> map = delegateMethods.get(method.getName());
        if (map == null) {
            throw new UnsupportedOperationException();
        }
        Method delegateMethod = map.get(Arrays.asList(method.getParameterTypes()));
        if (delegateMethod == null) {
            throw new UnsupportedOperationException();
        }
        return delegateMethod.invoke(vestigeSystem, args);
    }

}
