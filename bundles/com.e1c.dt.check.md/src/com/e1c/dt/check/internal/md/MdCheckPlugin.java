/*******************************************************************************
 * Copyright (C) 2022, 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     1C-Soft LLC - initial implementation
 *******************************************************************************/
package com.e1c.dt.check.internal.md;

import static java.text.MessageFormat.format;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Plugin activator. The activator class controls the plugin life cycle.
 *
 * @author Alexander Tretyakevich
 */
public class MdCheckPlugin
    extends Plugin
{
    /** Plugin identifier. */
    public static final String ID = "com.e1c.dt.check.md"; //$NON-NLS-1$

    private static MdCheckPlugin plugin;

    private Injector injector;

    /**
     * Returns the shared plugin instance.
     *
     * @return the plugin instance, never {@code null} if plugin is started
     */
    public static MdCheckPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Creates an info status by the provided message.
     *
     * @param message the status message, cannot be {@code null}
     * @return the status created info status, never {@code null}
     */
    public static IStatus createInfoStatus(String message)
    {
        return new Status(IStatus.INFO, ID, 0, message, null);
    }

    /**
     * Creates an error status by the provided message.
     *
     * @param message the status message, cannot be {@code null}
     * @return the created error status, never {@code null}
     */
    public static IStatus createErrorStatus(String message)
    {
        return createErrorStatus(message, null);
    }

    /**
     * Creates an error status by the provided message and the cause throwable.
     *
     * @param message the status message, cannot be {@code null}
     * @param throwable the cause throwable, can be {@code null} if not applicable
     * @return the created error status, never {@code null}
     */
    public static IStatus createErrorStatus(String message, Throwable throwable)
    {
        return new Status(IStatus.ERROR, ID, 0, message, throwable);
    }

    /**
     * Writes the status to the plugin log.
     *
     * @param status the status to write to the plugin log, cannot be {@code null}
     */
    public static void log(IStatus status)
    {
        getDefault().getLog().log(status);
    }

    /**
     * Creates and logs an error status by the provided message.
     *
     * @param message the status message, cannot be {@code null}
     */
    public static void logError(String message)
    {
        log(createErrorStatus(message));
    }

    /**
     * Creates and logs an error status by the provided message and the cause throwable.
     *
     * @param message the status message, cannot be {@code null}
     * @param throwable the cause throwable, can be {@code null} if not applicable
     */
    public static void logError(String message, Throwable throwable)
    {
        log(createErrorStatus(message, throwable));
    }

    /**
     * Logs the provided message as {@code INFO} status if plugin is in debug mode.
     *
     * @param message the message to log, cannot be {@code null}
     */
    public static void logDebug(String message)
    {
        if (getDefault().isDebugging())
        {
            log(createInfoStatus(message));
        }
    }

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the plugin Guice-injector. Method is synchronized.
     *
     * @return the plugin Guice-injector, never {@code null}
     */
    /* package */ synchronized Injector getInjector()
    {
        if (injector == null)
        {
            return injector = createInjector();
        }
        return injector;
    }

    private Injector createInjector()
    {
        try
        {
            return Guice.createInjector(new ExternalDependenciesModule(this));
        }
        catch (Exception e)
        {
            String message = format("Failed to create injector for {0}", getBundle().getSymbolicName()); //$NON-NLS-1$
            log(createErrorStatus(message, e));
            throw new RuntimeException(message, e);
        }
    }
}
