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
<<<<<<< HEAD
 *     1C-Soft LLC - initial implementation
=======
 *     1C-Soft LLC - initial API and implementation
>>>>>>> f317a3edd770771871e5399f4fa35234f4899311
 *******************************************************************************/
package com.e1c.dt.check.internal.md;

import org.eclipse.core.runtime.Plugin;

import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.wiring.AbstractServiceAwareModule;

/**
 * External services bindings for plugin.
 *
 * @author Andrey Volkov
 */
public class ExternalDependenciesModule
    extends AbstractServiceAwareModule
{
    /**
     * Constructor of {@link ExternalDependenciesModule}.
     *
     * @param bundle the parent bundle, cannot be {@code null}
     */
    public ExternalDependenciesModule(Plugin plugin)
    {
        super(plugin);
    }

    @Override
    protected void doConfigure()
    {
        bind(IRuntimeVersionSupport.class).toService();
    }
}
