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

import java.util.Vector;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeDriverVector extends Vector<Object> implements StackedHandler<Vector<Object>> {

    private static final long serialVersionUID = -8974921954435686686L;

    private Vector<Object> nextHandler;

    @Override
    public Vector<Object> getNextHandler() {
        return nextHandler;
    }

    @Override
    public void setNextHandler(final Vector<Object> nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public Object elementAt(final int index) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.elementAt(index);
        }
        return system.getDriverVector().elementAt(index);
    }

    @Override
    public void removeElementAt(final int index) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            super.removeElementAt(index);
        } else {
            system.getDriverVector().removeElementAt(index);
        }
    }

    @Override
    public void addElement(final Object obj) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            super.addElement(obj);
        } else {
            system.getDriverVector().addElement(obj);
        }
    }

    @Override
    public int size() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.size();
        }
        return system.getDriverVector().size();
    }

}
