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

import org.eclipse.core.runtime.Plugin;

import com._1c.g5.v8.dt.core.model.IModelEditingSupport;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IConfigurationProvider;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.form.service.FormIdentifierService;
import com._1c.g5.v8.dt.form.service.datasourceinfo.IDataSourceInfoAssociationService;
import com._1c.g5.wiring.AbstractServiceAwareModule;

/**
 * External services bindings for plugin.
 *
 * @author Dmitriy Marmyshev
 */
public class ExternalDependenciesModule
    extends AbstractServiceAwareModule
{

    /**
     * @param plugin
     */
    public ExternalDependenciesModule(Plugin plugin)
    {
        super(plugin);
    }

    @Override
    protected void doConfigure()
    {
        bind(IV8ProjectManager.class).toService();
        bind(IDataSourceInfoAssociationService.class).toService();
        bind(FormIdentifierService.class).toService();
        bind(IBmModelManager.class).toService();
        bind(IModelEditingSupport.class).toService();
        bind(IConfigurationProvider.class).toService();
    }

}
