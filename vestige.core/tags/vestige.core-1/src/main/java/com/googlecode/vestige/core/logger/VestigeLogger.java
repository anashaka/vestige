/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.vestige.core.logger;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Gael Lalire
 */
public interface VestigeLogger {

    /**
     * Log a LogRecord.
     * <p>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    void log(final LogRecord record);

    /**
     * Log a message, with no arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   msg     The string message (or a key in the message catalog)
     */
    void log(final Level level, final String msg);

    /**
     * Log a message, with one object parameter.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   msg     The string message (or a key in the message catalog)
     * @param   param1  parameter to the message
     */
    void log(final Level level, final String msg, final Object param1);

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   msg     The string message (or a key in the message catalog)
     * @param   params  array of parameters to the message
     */
    void log(final Level level, final String msg, final Object params[]);

    /**
     * Log a message, with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   msg     The string message (or a key in the message catalog)
     * @param   thrown  Throwable associated with log message.
     */
    void log(final Level level, final String msg, final Throwable thrown);

    /**
     * Log a message, specifying source class and method,
     * with no arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   msg     The string message (or a key in the message catalog)
     */
    void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg);

    /**
     * Log a message, specifying source class and method,
     * with a single object parameter to the log message.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   msg      The string message (or a key in the message catalog)
     * @param   param1    Parameter to the log message.
     */
    void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object param1);

    /**
     * Log a message, specifying source class and method,
     * with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   msg     The string message (or a key in the message catalog)
     * @param   params  Array of parameters to the message
     */
    void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object params[]);

    /**
     * Log a message, specifying source class and method,
     * with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   msg     The string message (or a key in the message catalog)
     * @param   thrown  Throwable associated with log message.
     */
    void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Throwable thrown);

    /**
     * Log a message, specifying source class, method, and resource bundle name
     * with no arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * The msg string is localized using the named resource bundle.  If the
     * resource bundle name is null, or an empty String or invalid
     * then the msg string is not localized.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   bundleName     name of resource bundle to localize msg,
     *                         can be null
     * @param   msg     The string message (or a key in the message catalog)
     */

    void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg);

    /**
     * Log a message, specifying source class, method, and resource bundle name,
     * with a single object parameter to the log message.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * The msg string is localized using the named resource bundle.  If the
     * resource bundle name is null, or an empty String or invalid
     * then the msg string is not localized.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   bundleName     name of resource bundle to localize msg,
     *                         can be null
     * @param   msg      The string message (or a key in the message catalog)
     * @param   param1    Parameter to the log message.
     */
    void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg,
            final Object param1);

    /**
     * Log a message, specifying source class, method, and resource bundle name,
     * with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * The msg string is localized using the named resource bundle.  If the
     * resource bundle name is null, or an empty String or invalid
     * then the msg string is not localized.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   bundleName     name of resource bundle to localize msg,
     *                         can be null.
     * @param   msg     The string message (or a key in the message catalog)
     * @param   params  Array of parameters to the message
     */
    void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg,
            final Object params[]);

    /**
     * Log a message, specifying source class, method, and resource bundle name,
     * with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * The msg string is localized using the named resource bundle.  If the
     * resource bundle name is null, or an empty String or invalid
     * then the msg string is not localized.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param   level   One of the message level identifiers, e.g. SEVERE
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that issued the logging request
     * @param   bundleName     name of resource bundle to localize msg,
     *                         can be null
     * @param   msg     The string message (or a key in the message catalog)
     * @param   thrown  Throwable associated with log message.
     */
    void logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg,
            final Throwable thrown);

    /**
     * Log a method entry.
     * <p>
     * This is a convenience method that can be used to log entry
     * to a method.  A LogRecord with message "ENTRY", log level
     * FINER, and the given sourceMethod and sourceClass is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that is being entered
     */
    void entering(final String sourceClass, final String sourceMethod);

    /**
     * Log a method entry, with one parameter.
     * <p>
     * This is a convenience method that can be used to log entry
     * to a method.  A LogRecord with message "ENTRY {0}", log level
     * FINER, and the given sourceMethod, sourceClass, and parameter
     * is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that is being entered
     * @param   param1         parameter to the method being entered
     */
    void entering(final String sourceClass, final String sourceMethod, final Object param1);

    /**
     * Log a method entry, with an array of parameters.
     * <p>
     * This is a convenience method that can be used to log entry
     * to a method.  A LogRecord with message "ENTRY" (followed by a
     * format {N} indicator for each entry in the parameter array),
     * log level FINER, and the given sourceMethod, sourceClass, and
     * parameters is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that is being entered
     * @param   params         array of parameters to the method being entered
     */
    void entering(final String sourceClass, final String sourceMethod, final Object params[]);

    /**
     * Log a method return.
     * <p>
     * This is a convenience method that can be used to log returning
     * from a method.  A LogRecord with message "RETURN", log level
     * FINER, and the given sourceMethod and sourceClass is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of the method
     */
    void exiting(final String sourceClass, final String sourceMethod);

    /**
     * Log a method return, with result object.
     * <p>
     * This is a convenience method that can be used to log returning
     * from a method.  A LogRecord with message "RETURN {0}", log level
     * FINER, and the gives sourceMethod, sourceClass, and result
     * object is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of the method
     * @param   result  Object that is being returned
     */
    void exiting(final String sourceClass, final String sourceMethod, final Object result);

    /**
     * Log throwing an exception.
     * <p>
     * This is a convenience method to log that a method is
     * terminating by throwing an exception.  The logging is done
     * using the FINER level.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.  The
     * LogRecord's message is set to "THROW".
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod  name of the method.
     * @param   thrown  The Throwable that is being thrown.
     */
    void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown);

    /**
     * Log a SEVERE message.
     * <p>
     * If the logger is currently enabled for the SEVERE message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void severe(final String msg);

    /**
     * Log a WARNING message.
     * <p>
     * If the logger is currently enabled for the WARNING message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void warning(final String msg);

    /**
     * Log an INFO message.
     * <p>
     * If the logger is currently enabled for the INFO message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void info(final String msg);

    /**
     * Log a CONFIG message.
     * <p>
     * If the logger is currently enabled for the CONFIG message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void config(final String msg);

    /**
     * Log a FINE message.
     * <p>
     * If the logger is currently enabled for the FINE message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void fine(final String msg);

    /**
     * Log a FINER message.
     * <p>
     * If the logger is currently enabled for the FINER message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void finer(final String msg);

    /**
     * Log a FINEST message.
     * <p>
     * If the logger is currently enabled for the FINEST message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param   msg     The string message (or a key in the message catalog)
     */
    void finest(final String msg);

    /**
     * Get the log Level that has been specified for this Logger.
     * The result may be null, which means that this logger's
     * effective level will be inherited from its parent.
     *
     * @return  this Logger's level
     */
    Level getLevel();

    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param   level   a message logging level
     * @return  true if the given message level is currently being logged.
     */
    boolean isLoggable(final Level level);

}