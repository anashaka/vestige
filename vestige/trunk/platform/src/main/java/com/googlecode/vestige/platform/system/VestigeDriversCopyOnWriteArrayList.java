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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeDriversCopyOnWriteArrayList extends CopyOnWriteArrayList<Object> implements StackedHandler<CopyOnWriteArrayList<Object>> {

    private static final long serialVersionUID = 998267829607402508L;

    private CopyOnWriteArrayList<Object> nextHandler;

    @Override
    public CopyOnWriteArrayList<Object> getNextHandler() {
        return nextHandler;
    }

    @Override
    public void setNextHandler(final CopyOnWriteArrayList<Object> nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public Iterator<Object> iterator() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.iterator();
        }
        return system.getRegisteredDrivers().iterator();
    }

    @Override
    public boolean addIfAbsent(final Object e) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.addIfAbsent(e);
        }
        return system.getRegisteredDrivers().addIfAbsent(e);
    }

    @Override
    public boolean contains(final Object o) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.contains(o);
        }
        return system.getRegisteredDrivers().contains(o);
    }

    @Override
    public boolean remove(final Object o) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.remove(o);
        }
        return system.getRegisteredDrivers().remove(o);
    }

}
