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
package com.e1c.dt.check.internal.md.fix;

import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;

import com._1c.g5.v8.dt.core.naming.ISymbolicLinkLocalizer;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.testing.GuiceModules;
import com._1c.g5.v8.dt.ui.util.OpenHelper;
import com._1c.g5.v8.dt.ui.validation.BmMarkerWrapper;
import com._1c.g5.v8.dt.validation.marker.IMarkerWrapper;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.CheckMdDependenciesModule;
import com.e1c.dt.check.internal.md.check.AbstractMdCheckTest;
import com.e1c.g5.v8.dt.check.qfix.FixProcessHandle;
import com.e1c.g5.v8.dt.check.qfix.FixVariantDescriptor;
import com.e1c.g5.v8.dt.check.qfix.IFixManager;
import com.google.inject.Inject;

/**
 * The abstract test base class to test fixes in metadata.
 *
 * @author Andrey Volkov
 */
@GuiceModules(modules = { CheckMdDependenciesModule.class })
public abstract class AbstractMdFixTest
    extends AbstractMdCheckTest
{
    private final OpenHelper openHelper = new OpenHelper();

    @Inject
    private ISymbolicLinkLocalizer symbolicLinkLocalizer;
    @Inject
    private IFixManager fixManager;

    /**
     * Apply fix for the marker.
     *
     * @param marker the marker, cannot be {@code null}
     * @param dtProject the DT project of the marker, cannot be {@code null}
     * @param targetFixDescription target fix description, cannot be {@code null}
     */
    protected void applyFix(Marker marker, IDtProject dtProject, String targetFixDescription)
    {
        IMarkerWrapper markerWrapper = new BmMarkerWrapper(marker, dtProject.getWorkspaceProject(), bmModelManager,
            projectManager, symbolicLinkLocalizer, openHelper);

        FixProcessHandle handle = fixManager.prepareFix(markerWrapper, dtProject);

        FixVariantDescriptor variantDescr = null;

        Collection<FixVariantDescriptor> variants = fixManager.getApplicableFixVariants(handle);
        for (FixVariantDescriptor variant : variants)
        {
            if (variant.getDescription().matches(targetFixDescription))
            {
                variantDescr = variant;
            }
        }

        if (variantDescr != null)
        {
            fixManager.selectFixVariant(variantDescr, handle);
            fixManager.executeFix(handle, new NullProgressMonitor());
            fixManager.finishFix(handle);
        }
    }
}
