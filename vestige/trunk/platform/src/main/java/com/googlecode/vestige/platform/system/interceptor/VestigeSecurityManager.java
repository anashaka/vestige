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

package com.googlecode.vestige.platform.system.interceptor;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public abstract class VestigeSecurityManager extends SecurityManager implements StackedHandler<SecurityManager> {

    private SecurityManager nextHandler;

    private SecurityManager previousSecurityManager;

    public VestigeSecurityManager(final SecurityManager nextHandler, final SecurityManager previousSecurityManager) {
        this.nextHandler = nextHandler;
        this.previousSecurityManager = previousSecurityManager;
    }

    public abstract SecurityManager getSecurityManager();

    @Override
    public void checkAccept(final String host, final int port) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkAccept(host, port);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAccept(host, port);
        }
    }

    @Override
    public void checkAccess(final Thread t) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkAccess(t);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAccess(t);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup g) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkAccess(g);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAccess(g);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkAwtEventQueueAccess();
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAwtEventQueueAccess();
        }
    }

    @Override
    public void checkConnect(final String host, final int port) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkConnect(host, port);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkConnect(host, port);
        }
    }

    @Override
    public void checkConnect(final String host, final int port, final Object context) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkConnect(host, port, context);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkConnect(host, port, context);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkCreateClassLoader();
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkCreateClassLoader();
        }
    }

    @Override
    public void checkDelete(final String file) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkDelete(file);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkDelete(file);
        }
    }

    @Override
    public void checkExec(final String cmd) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkExec(cmd);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkExec(cmd);
        }
    }

    @Override
    public void checkExit(final int status) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkExit(status);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkExit(status);
        }
    }

    @Override
    public void checkLink(final String lib) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkLink(lib);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkLink(lib);
        }
    }

    @Override
    public void checkListen(final int port) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkListen(port);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkListen(port);
        }
    }

    @Override
    public void checkMemberAccess(final Class<?> clazz, final int which) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkMemberAccess(clazz, which);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkMemberAccess(clazz, which);
        }
    }

    @Override
    public void checkMulticast(final InetAddress maddr) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkMulticast(maddr);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkMulticast(maddr);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void checkMulticast(final InetAddress maddr, final byte ttl) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkMulticast(maddr, ttl);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkMulticast(maddr, ttl);
        }
    }

    @Override
    public void checkPackageAccess(final String pkg) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPackageAccess(pkg);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPackageAccess(pkg);
        }
    }

    @Override
    public void checkPackageDefinition(final String pkg) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPackageDefinition(pkg);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPackageDefinition(pkg);
        }
    }

    @Override
    public void checkPermission(final Permission perm) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPermission(perm);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(perm);
        }
    }

    @Override
    public void checkPermission(final Permission perm, final Object context) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPermission(perm, context);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(perm, context);
        }
    }

    @Override
    public void checkPrintJobAccess() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPrintJobAccess();
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPrintJobAccess();
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPropertiesAccess();
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPropertiesAccess();
        }
    }

    @Override
    public void checkPropertyAccess(final String key) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPropertyAccess(key);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPropertyAccess(key);
        }
    }

    @Override
    public void checkRead(final FileDescriptor fd) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkRead(fd);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkRead(fd);
        }
    }

    @Override
    public void checkRead(final String file) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkRead(file);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkRead(file);
        }
    }

    @Override
    public void checkRead(final String file, final Object context) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkRead(file, context);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkRead(file, context);
        }
    }

    @Override
    public void checkSecurityAccess(final String target) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkSecurityAccess(target);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkSecurityAccess(target);
        }
    }

    @Override
    public void checkSetFactory() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkSetFactory();
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkSetFactory();
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkSystemClipboardAccess();
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkSystemClipboardAccess();
        }
    }

    @Override
    public boolean checkTopLevelWindow(final Object window) {
        if (previousSecurityManager != null && !previousSecurityManager.checkTopLevelWindow(window)) {
            return false;
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null && !securityManager.checkTopLevelWindow(window)) {
            return false;
        }
        return true;
    }

    @Override
    public void checkWrite(final FileDescriptor fd) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkWrite(fd);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkWrite(fd);
        }
    }

    @Override
    public void checkWrite(final String file) {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkWrite(file);
        }
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            securityManager.checkWrite(file);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            return securityManager.equals(obj);
        }
        return super.equals(obj);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean getInCheck() {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            return securityManager.getInCheck();
        }
        return super.getInCheck();
    }

    @Override
    public Object getSecurityContext() {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            return securityManager.getSecurityContext();
        }
        return super.getSecurityContext();
    }

    @Override
    public ThreadGroup getThreadGroup() {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            return securityManager.getThreadGroup();
        }
        return super.getThreadGroup();
    }

    @Override
    public int hashCode() {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            return securityManager.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        SecurityManager securityManager = getSecurityManager();
        if (securityManager != null) {
            return securityManager.toString();
        }
        return super.toString();
    }

    public SecurityManager getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(final SecurityManager nextHandler) {
        this.nextHandler = nextHandler;
    }

}
