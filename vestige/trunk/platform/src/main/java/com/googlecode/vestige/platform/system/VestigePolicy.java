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

import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigePolicy extends Policy implements StackedHandler<Policy> {

    private Policy nextHandler;

    private ThreadLocal<PermissionCollection> permissionCollectionThreadLocal = new InheritableThreadLocal<PermissionCollection>();

    private Set<ClassLoader> safeClassLoaders = new HashSet<ClassLoader>();

    public VestigePolicy(final Policy nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void setPermissionCollection(final PermissionCollection permissionCollection) {
        permissionCollectionThreadLocal.set(permissionCollection);
    }

    public void unsetPermissionCollection() {
        permissionCollectionThreadLocal.remove();
    }

    public void clearSafeClassLoader() {
        safeClassLoaders.clear();
    }

    public void addSafeClassLoader(final ClassLoader safeClassLoader) {
        ClassLoader classLoader = safeClassLoader;
        while (classLoader != null) {
            safeClassLoaders.add(classLoader);
            classLoader = classLoader.getParent();
        }
    }

    @Override
    public PermissionCollection getPermissions(final CodeSource codesource) {
        return new Permissions();
    }

    @Override
    public PermissionCollection getPermissions(final ProtectionDomain domain) {
        return new Permissions();
    }

    @Override
    public boolean implies(final ProtectionDomain domain, final Permission permission) {
        ClassLoader classLoader = domain.getClassLoader();
        for (ClassLoader safeClassLoader : safeClassLoaders) {
            if (safeClassLoader == classLoader) {
                // safeClassLoader are not restricted
                return true;
            }
        }
        PermissionCollection permissionCollection = permissionCollectionThreadLocal.get();
        if (permissionCollection == null) {
            // not restricted
            return true;
        }
        if (permissionCollection.implies(permission)) {
            return true;
        }
        if (!AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                ClassLoader permClassLoader = permission.getClass().getClassLoader();
                if (permClassLoader == null) {
                    return true;
                }
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                while (systemClassLoader != null) {
                    if (systemClassLoader == permClassLoader) {
                        return true;
                    }
                    systemClassLoader = systemClassLoader.getParent();
                }
                return false;
            }
        })) {
            // the permission is not a system permission, so we ignore it
            return true;
        }
        return false;
    }

    public Policy getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(final Policy nextHandler) {
        this.nextHandler = nextHandler;
    }

}
