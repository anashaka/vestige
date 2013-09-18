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

import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JULLogger extends Logger {

    private VestigeLogger logger;

    private String name;

    public JULLogger(final String name, final VestigeLogger logger) {
        super(name, null);
        this.logger = logger;
    }

    /**
     * Log a LogRecord.
     * <p>
     * All the other logging methods in this class call through this method to
     * actually perform any logging. Subclasses can override this single method
     * to capture all log activity.
     * @param record the LogRecord to be published
     */
    @Override
    public void log(final LogRecord record) {
        logger.log(record);
    }

    @Override
    public void log(final Level level, final String msg) {
        logger.log(level, msg);
    }

    @Override
    public void log(final Level level, final String msg, final Object param1) {
        logger.log(level, msg, param1);
    }

    @Override
    public void log(final Level level, final String msg, final Object params[]) {
        logger.log(level, msg, params);
    }

    @Override
    public void log(final Level level, final String msg, final Throwable thrown) {
        logger.log(level, msg, thrown);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        logger.logp(level, sourceClass, sourceMethod, msg);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object param1) {
        logger.logp(level, sourceClass, sourceMethod, msg, param1);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
            final Object params[]) {
        logger.logp(level, sourceClass, sourceMethod, msg, params);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
            final Throwable thrown) {
        logger.logp(level, sourceClass, sourceMethod, msg, thrown);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg) {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg, final Object param1) {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, param1);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg, final Object params[]) {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, params);
    }

    @Override
    public void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName,
            final String msg, final Throwable thrown) {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, thrown);
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod) {
        logger.entering(sourceClass, sourceMethod);
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object param1) {
        logger.entering(sourceClass, sourceMethod, param1);
    }

    @Override
    public void entering(final String sourceClass, final String sourceMethod, final Object params[]) {
        logger.entering(sourceClass, sourceMethod, params);
    }

    @Override
    public void exiting(final String sourceClass, final String sourceMethod) {
        logger.exiting(sourceClass, sourceMethod);
    }

    @Override
    public void exiting(final String sourceClass, final String sourceMethod, final Object result) {
        logger.exiting(sourceClass, sourceMethod, result);
    }

    @Override
    public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
        logger.throwing(sourceClass, sourceMethod, thrown);
    }

    @Override
    public void severe(final String msg) {
        logger.severe(msg);
    }

    @Override
    public void warning(final String msg) {
        logger.warning(msg);
    }

    @Override
    public void info(final String msg) {
        logger.info(msg);
    }

    @Override
    public void config(final String msg) {
        logger.config(msg);
    }

    @Override
    public void fine(final String msg) {
        logger.fine(msg);
    }

    @Override
    public void finer(final String msg) {
        logger.finer(msg);
    }

    @Override
    public void finest(final String msg) {
        logger.finest(msg);
    }

    @Override
    public void setLevel(final Level newLevel) throws SecurityException {
        // ignoring
    }

    @Override
    public Level getLevel() {
        return logger.getLevel();
    }

    @Override
    public boolean isLoggable(final Level level) {
        return logger.isLoggable(level);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    public void setFilter(final Filter newFilter) throws SecurityException {
        // ignoring
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return super.getResourceBundle();
    }

    @Override
    public String getResourceBundleName() {
        return super.getResourceBundleName();
    }

    @Override
    public void addHandler(final Handler handler) throws SecurityException {
         super.addHandler(handler);
    }

    @Override
    public void removeHandler(final Handler handler) throws SecurityException {
        super.removeHandler(handler);
    }

    @Override
    public Handler[] getHandlers() {
        return super.getHandlers();
    }

    @Override
    public void setUseParentHandlers(final boolean useParentHandlers) {
        super.setUseParentHandlers(useParentHandlers);
    }

    @Override
    public boolean getUseParentHandlers() {
        return super.getUseParentHandlers();
    }

    @Override
    public Logger getParent() {
        return super.getParent();
    }

    @Override
    public void setParent(final Logger parent) {
        super.setParent(parent);
    }


}
