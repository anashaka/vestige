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

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeSecurityManager extends SecurityManager implements StackedHandler<SecurityManager> {

    private static final RuntimePermission MODIFY_THREAD_GROUP_PERMISSION = new RuntimePermission("modifyThreadGroup");

    private static final RuntimePermission MODIFY_THREAD_PERMISSION = new RuntimePermission("modifyThread");

    private ThreadLocal<ThreadGroup> threadGroupThreadLocal = new InheritableThreadLocal<ThreadGroup>();

    private SecurityManager nextHandler;

    private ThreadGroup rootGroup;

    public VestigeSecurityManager(final SecurityManager nextHandler) {
        this.nextHandler = nextHandler;
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        rootGroup = root;
    }

    public void setThreadGroup(final ThreadGroup threadGroup) {
        threadGroupThreadLocal.set(threadGroup);
    }

    public void unsetThreadGroup() {
        threadGroupThreadLocal.remove();
    }

    @Override
    public void checkAccess(final Thread t) {
        super.checkAccess(t);
        ThreadGroup otherThreadGroup = t.getThreadGroup();
        if (otherThreadGroup == rootGroup) {
            return;
        }
        ThreadGroup threadGroup = threadGroupThreadLocal.get();
        if (threadGroup != null && otherThreadGroup != null && !threadGroup.parentOf(otherThreadGroup)) {
            checkPermission(MODIFY_THREAD_PERMISSION);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup g) {
        super.checkAccess(g);
        if (g == rootGroup) {
            return;
        }
        ThreadGroup threadGroup = threadGroupThreadLocal.get();
        if (threadGroup != null && !threadGroup.parentOf(g)) {
            checkPermission(MODIFY_THREAD_GROUP_PERMISSION);
        }
    }

    public SecurityManager getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(final SecurityManager nextHandler) {
        this.nextHandler = nextHandler;
    }

}
