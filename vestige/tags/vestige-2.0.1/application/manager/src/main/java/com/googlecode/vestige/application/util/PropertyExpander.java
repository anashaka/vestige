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

package com.googlecode.vestige.application.util;

import java.io.File;

import com.googlecode.vestige.application.ApplicationException;

/**
 * @author Gael Lalire
 */
public class PropertyExpander {

    public static String expand(final String value) throws ApplicationException {
        if (value == null) {
            return null;
        }

        int p = value.indexOf("${", 0);

        // no special characters
        if (p == -1) {
            return value;
        }

        StringBuffer sb = new StringBuffer(value.length());
        int max = value.length();
        int i = 0; // index of last character we copied

        scanner: while (p < max) {
            if (p > i) {
                // copy in anything before the special stuff
                sb.append(value.substring(i, p));
                i = p;
            }
            int pe = p + 2;

            // do not expand ${{ ... }}
            if (pe < max && value.charAt(pe) == '{') {
                pe = value.indexOf("}}", pe);
                if (pe == -1 || pe + 2 == max) {
                    // append remaining chars
                    sb.append(value.substring(p));
                    break scanner;
                } else {
                    // append as normal text
                    pe++;
                    sb.append(value.substring(p, pe + 1));
                }
            } else {
                while ((pe < max) && (value.charAt(pe) != '}')) {
                    pe++;
                }
                if (pe == max) {
                    // no matching '}' found, just add in as normal text
                    sb.append(value.substring(p, pe));
                    break scanner;
                }
                String prop = value.substring(p + 2, pe);
                if (prop.equals("/")) {
                    sb.append(File.separatorChar);
                } else {
                    String val = System.getProperty(prop);
                    if (val != null) {
                        sb.append(val);
                    } else {
                        throw new ApplicationException("unable to expand property " + prop);
                    }
                }
            }
            i = pe + 1;
            p = value.indexOf("${", i);
            if (p == -1) {
                // no more to expand. copy in any extra
                if (i < max) {
                    sb.append(value.substring(i, max));
                }
                // break out of loop
                break scanner;
            }
        }
        return sb.toString();
    }

}
