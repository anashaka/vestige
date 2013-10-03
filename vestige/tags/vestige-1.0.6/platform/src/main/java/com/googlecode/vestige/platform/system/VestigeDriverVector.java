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

import java.sql.Driver;
import java.util.Vector;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeDriverVector extends Vector<Driver> implements StackedHandler<Vector<Driver>> {

    private static final long serialVersionUID = -8974921954435686686L;

    private Vector<Driver> nextHandler;

    @Override
    public Vector<Driver> getNextHandler() {
        return nextHandler;
    }

    @Override
    public void setNextHandler(final Vector<Driver> nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public Driver elementAt(final int index) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.elementAt(index);
        }
        return system.getDriverVector().elementAt(index);
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
