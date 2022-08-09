/*******************************************************************************
 * Copyright (C) 2021, 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     1C-Soft LLC - initial API and implementation
 *******************************************************************************/
package com.e1c.dt.check.internal.form;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

import com._1c.g5.wiring.InjectorAwareServiceRegistrator;
import com._1c.g5.wiring.ServiceInitialization;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Plugin activator. The activator class controls the plugin life cycle.
 *
 * @author Dmitriy Marmyshev
 */
public class CorePlugin
    extends Plugin
    implements DebugOptionsListener
{

    public static final String PLUGIN_ID = "com.e1c.dt.check.form"; //$NON-NLS-1$

    /**
     * Name of option that controls if tracing is enabled.
     */
    public static final String DEBUG_OPTION = PLUGIN_ID + "/debug"; //$NON-NLS-1$

    private static boolean debug = false;
    private static DebugTrace debugTrace = null;

    private static CorePlugin plugin;

    private volatile Injector injector;
    private InjectorAwareServiceRegistrator registrator;

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static CorePlugin getDefault()
    {
        return plugin;
    }

    /**
     * Writes a status to the plugin log.
     *
     * @param status status to log, cannot be <code>null</code>
     */
    public static void log(IStatus status)
    {
        plugin.getLog().log(status);
    }

    /**
     * Writes a throwable to the plugin log as error status.
     *
     * @param throwable throwable, cannot be <code>null</code>
     */
    public static void logError(Throwable throwable)
    {
        log(createErrorStatus(throwable.getMessage(), throwable));
    }

    /**
     * Creates error status by a given message and cause throwable.
     *
     * @param message status message, cannot be <code>null</code>
     * @param throwable throwable, can be <code>null</code> if not applicable
     * @return status created error status, never <code>null</code>
     */
    public static IStatus createErrorStatus(String message, Throwable throwable)
    {
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
    }

    /**
     * Creates warning status by a given message.
     *
     * @param message status message, cannot be <code>null</code>
     * @return status created warning status, never <code>null</code>
     */
    public static IStatus createWarningStatus(String message)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
    }

    /**
     * Creates warning status by a given message and cause throwable.
     *
     * @param message status message, cannot be <code>null</code>
     * @param throwable throwable, can be <code>null</code> if not applicable
     * @return status created warning status, never <code>null</code>
     */
    public static IStatus createWarningStatus(final String message, Exception throwable)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        super.start(bundleContext);

        plugin = this;

        registrator = new InjectorAwareServiceRegistrator(bundleContext, this::getInjector);

        ServiceInitialization.schedule(() -> {
            try
            {
                registrator.service(DebugOptionsListener.class)
                    .withProperty(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID)
                    .registerInstance(this);
            }
            catch (Exception e)
            {
                logError(e);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        try
        {
            registrator.unregisterServices();
            registrator.deactivateManagedServices(this);
        }
        finally
        {
            injector = null;
            plugin = null;
            super.stop(bundleContext);
        }
    }

    @Override
    public void optionsChanged(DebugOptions options)
    {
        debug = options.getBooleanOption(DEBUG_OPTION, false);
        debugTrace = options.newDebugTrace(PLUGIN_ID);
    }

    @Override
    public boolean isDebugging()
    {
        return debug;
    }

    /**
     * Traces debug information.
     *
     * This uses {@link DebugTrace} facility. A message will be traced if
     * tracing is enabled globally, for plugin ({@link #DEBUG_OPTION}) and for individual
     * component via {@code traceOption}.
     *
     * @param traceOption Name of the option that is to be checked if message should be traced.
     * This corresponds to an individual component of function. The value should have
     * the following format: /debug/componentName. It should be toggled either via .options file
     * or through Window  → Preferences  → General → Tracing. Note the importance of {@code /} in the beginning.
     * If value is {@code null} then option for an individual component will not be checked.
     * However, {@link #DEBUG_OPTION} will still be checked.
     * @param pattern Format of the message to be traced according to {@link MessageFormat#format(String, Object...)}.
     * @param arguments Parameters of the message to be traced.
     */
    public static void trace(String traceOption, String pattern, Object... arguments)
    {
        if (debug && debugTrace != null)
        {
            debugTrace.trace(traceOption, MessageFormat.format(pattern, arguments));
        }
    }

    /**
     * Returns Guice injector for this plugin.
     *
     * @return Guice injector for this plugin, never <code>null</code>
     */
    /* package */ Injector getInjector()
    {
        Injector localInstance = injector;
        if (localInstance == null)
        {
            synchronized (CorePlugin.class)
            {
                localInstance = injector;
                if (localInstance == null)
                {
                    localInstance = createInjector();
                    injector = localInstance;
                }
            }
        }
        return localInstance;
    }

    private Injector createInjector()
    {
        try
        {
            return Guice.createInjector(new ExternalDependenciesModule(this), new ServiceModule());
        }
        catch (Exception e)
        {
            log(createErrorStatus("Failed to create injector for " //$NON-NLS-1$
                + getBundle().getSymbolicName(), e));
            throw new RuntimeException("Failed to create injector for " //$NON-NLS-1$
                + getBundle().getSymbolicName(), e);
        }
    }

}
