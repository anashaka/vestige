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

import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.util.Hashtable;
import java.util.Map;

import com.googlecode.vestige.core.StackedHandler;

/**
 * @author Gael Lalire
 */
public class VestigeURLConnectionHandlersHashTable extends Hashtable<String, ContentHandler> implements StackedHandler<Hashtable<String, ContentHandler>> {

    private static final long serialVersionUID = 774582859985938991L;

    private Hashtable<String, ContentHandler> nextHandler;

    @Override
    public ContentHandler get(final Object mimeType) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.get(mimeType);
        }
        Map<String, ContentHandler> urlConnectionContentHandlerByMime = system.getURLConnectionContentHandlerByMime();
        ContentHandler urlConnectionContentHandler = urlConnectionContentHandlerByMime.get(mimeType);
        if (urlConnectionContentHandler == null) {
            ContentHandlerFactory contentHandlerFactory = system.getURLConnectionContentHandlerFactory();
            if (contentHandlerFactory != null) {
                urlConnectionContentHandler = contentHandlerFactory.createContentHandler((String) mimeType);
                urlConnectionContentHandlerByMime.put((String) mimeType, urlConnectionContentHandler);
            }
        }
        return urlConnectionContentHandler;
    }

    @Override
    public ContentHandler put(final String mimeType, final ContentHandler urlStreamHandler) {
        VestigeSystem system = VestigeSystem.getSystem();
        if (system == null) {
            return super.put(mimeType, urlStreamHandler);
        }
        return system.getURLConnectionContentHandlerByMime().put(mimeType, urlStreamHandler);
    }

    @Override
    public Hashtable<String, ContentHandler> getNextHandler() {
        return nextHandler;
    }

    @Override
    public void setNextHandler(final Hashtable<String, ContentHandler> nextHandler) {
        this.nextHandler = nextHandler;
    }

}
