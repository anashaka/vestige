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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeDriverVector extends Vector<Object> implements StackedHandler<Vector<Object>>, VestigeSystemListener {

    private static final long serialVersionUID = -8974921954435686686L;

    private Vector<Object> nextHandler;

    private VestigeDriverVector readDrivers;

    private Map<VestigeSystem, Vector<Object>> readDriversBySystem;

    public VestigeDriverVector() {
        readDriversBySystem = new HashMap<VestigeSystem, Vector<Object>>();
    }

    public VestigeDriverVector(final VestigeDriverVector writeDrivers) {
        // this instance will be new value of readDrivers field
        readDriversBySystem = new HashMap<VestigeSystem, Vector<Object>>(writeDrivers.readDriversBySystem);
        nextHandler = writeDrivers.readDrivers.nextHandler;
    }

    public void setReadDrivers(final VestigeDriverVector readDrivers) {
        this.readDrivers = readDrivers;
    }

    public VestigeDriverVector getReadDrivers() {
        return readDrivers;
    }

    @Override
    public Vector<Object> getNextHandler() {
        return nextHandler;
    }

    @Override
    public void setNextHandler(final Vector<Object> nextHandler) {
        this.nextHandler = nextHandler;
    }

    @SuppressWarnings("unchecked")
    public Vector<Object> getDriverVector(final VestigeSystem system) {
        if (readDrivers == null) {
            Vector<Object> vector = readDriversBySystem.get(system);
            if (vector == null) {
                // first read
                vector = (Vector<Object>) system.getWriteDrivers().clone();
                readDriversBySystem.put(system, vector);
            }
            return vector;
        }
        return system.getWriteDrivers();
    }

    @Override
    public Object elementAt(final int index) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.elementAt(index);
        }
        return getDriverVector(system).elementAt(index);
    }

    @Override
    public void removeElementAt(final int index) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            super.removeElementAt(index);
        } else {
            getDriverVector(system).removeElementAt(index);
        }
    }

    @Override
    public void addElement(final Object obj) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            super.addElement(obj);
        } else {
            getDriverVector(system).addElement(obj);
        }
    }

    @Override
    public int size() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.size();
        }
        return getDriverVector(system).size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system != null) {
            // change in current (ie writeDrivers) so if another system ask for a clone, this system can continue to access its readDrivers
            readDriversBySystem.put(system, (Vector<Object>) system.getWriteDrivers().clone());
        }
        readDrivers = new VestigeDriverVector(this);
        return readDrivers;
    }

    @Override
    public void systemPushed(final VestigeSystem system) {
    }

    @Override
    public void systemPoped(final VestigeSystem system) {
        readDriversBySystem.remove(system);
        readDrivers.readDriversBySystem.remove(system);
    }

}
