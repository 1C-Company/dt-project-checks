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
 *     1C-Soft LLC - initial API and implementation
 *******************************************************************************/
package com.e1c.dt.check.internal.md.itests;

import org.osgi.framework.FrameworkUtil;

import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IDerivedDataManagerProvider;
import com._1c.g5.v8.dt.core.platform.IDtProjectManager;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.core.platform.IWorkspaceOrchestrator;
import com._1c.g5.v8.dt.validation.marker.IMarkerManager;
import com._1c.g5.wiring.AbstractServiceAwareModule;
import com.e1c.g5.v8.dt.check.ICheckScheduler;
import com.e1c.g5.v8.dt.check.settings.ICheckRepository;

/**
 * Plugin external dependencies module.
 *
 * @author Andrey Volkov
 */
public class ExternalDependenciesModule
    extends AbstractServiceAwareModule
{
    /**
     * Constructor of {@link ExternalDependenciesModule}.
     */
    public ExternalDependenciesModule()
    {
        super(FrameworkUtil.getBundle(ExternalDependenciesModule.class).getBundleContext());
    }

    @Override
    protected void doConfigure()
    {
        bind(IMarkerManager.class).toService();
        bind(IDtProjectManager.class).toService();
        bind(IWorkspaceOrchestrator.class).toService();
        bind(IBmModelManager.class).toService();
        bind(ICheckRepository.class).toService();
        bind(ICheckScheduler.class).toService();
        bind(IDerivedDataManagerProvider.class).toService();
        bind(IV8ProjectManager.class).toService();
    }
}
