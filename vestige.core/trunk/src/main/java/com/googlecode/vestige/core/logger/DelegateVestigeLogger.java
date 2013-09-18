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

package com.googlecode.vestige.core.logger;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Gael Lalire
 */
public class DelegateVestigeLogger implements VestigeLogger {

    private volatile VestigeLogger logger;

    private volatile VestigeLoggerFactory factory;

    private String name;

    public DelegateVestigeLogger(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public VestigeLogger getLogger() {
        VestigeLoggerFactory newFactory = VestigeLoggerFactory.getVestigeLoggerFactory();
        VestigeLoggerFactory currentFactory = this.factory;
        if (newFactory == null) {
            this.factory = null;
            logger = null;
        } else {
            if (newFactory != currentFactory) {
                this.factory = currentFactory;
                logger = newFactory.createLogger(name);
            }
        }
        return logger;
    }

    public void log(final LogRecord record) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.log(record);
        }
    }

    public void log(final Level level, final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.log(level, msg);
        }
    }

    public void log(final Level level, final String msg, final Object param1) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.log(level, msg, param1);
        }
    }

    public void log(final Level level, final String msg, final Object params[]) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.log(level, msg, params);
        }
    }

    public void log(final Level level, final String msg, final Throwable thrown) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.log(level, msg, thrown);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logp(level, sourceClass, sourceMethod, msg);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object param1) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logp(level, sourceClass, sourceMethod, msg, param1);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
            final Object params[]) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logp(level, sourceClass, sourceMethod, msg, params);
        }
    }

    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
            final Throwable thrown) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logp(level, sourceClass, sourceMethod, msg, thrown);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logrb(level, sourceClass, sourceMethod, bundleName, msg);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg, final Object param1) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, param1);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg, final Object params[]) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, params);
        }
    }

    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg, final Throwable thrown) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, thrown);
        }
    }

    public void entering(final String sourceClass, final String sourceMethod) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.entering(sourceClass, sourceMethod);
        }
    }

    public void entering(final String sourceClass, final String sourceMethod, final Object param1) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.entering(sourceClass, sourceMethod, param1);
        }
    }

    public void entering(final String sourceClass, final String sourceMethod, final Object params[]) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.entering(sourceClass, sourceMethod, params);
        }
    }

    public void exiting(final String sourceClass, final String sourceMethod) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.exiting(sourceClass, sourceMethod);
        }
    }

    public void exiting(final String sourceClass, final String sourceMethod, final Object result) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.exiting(sourceClass, sourceMethod, result);
        }
    }

    public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.throwing(sourceClass, sourceMethod, thrown);
        }
    }

    public void severe(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.severe(msg);
        }
    }

    public void warning(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.warning(msg);
        }
    }

    public void info(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.info(msg);
        }
    }

    public void config(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.config(msg);
        }
    }

    public void fine(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.fine(msg);
        }
    }

    public void finer(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.finer(msg);
        }
    }

    public void finest(final String msg) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            logger.finest(msg);
        }
    }

    public Level getLevel() {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            return logger.getLevel();
        }
        return Level.OFF;
    }

    public boolean isLoggable(final Level level) {
        VestigeLogger logger = getLogger();
        if (logger != null) {
            return logger.isLoggable(level);
        }
        return false;
    }

}
