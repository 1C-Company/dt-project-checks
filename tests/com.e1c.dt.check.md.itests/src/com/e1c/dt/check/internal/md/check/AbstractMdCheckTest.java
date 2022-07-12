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
package com.e1c.dt.check.internal.md.check;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.testing.GuiceModules;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.CheckMdDependenciesModule;
import com.e1c.g5.v8.dt.check.ICheckScheduler;
import com.e1c.g5.v8.dt.testing.check.CheckTestBase;
import com.google.inject.Inject;

/**
 * Abstract tests for the metadata.
 *
 * @author Andrey Volkov
 */
@GuiceModules(modules = { CheckMdDependenciesModule.class })
public abstract class AbstractMdCheckTest
    extends CheckTestBase
{
    /**
     * Check scheduler.
     */
    @Inject
    protected ICheckScheduler checker;

    /**
     * V8 project manager.
     */
    @Inject
    protected IV8ProjectManager projectManager;

    /**
     * Checks and return object marker if present.
     *
     * @param dtProject the dt-project, cannot be {@code null}
     * @param checkId the check id, cannot be {@code null}
     * @param bmObjectId the target bm-object id, cannot be {@code null}
     * @return
     */
    protected Marker checkMarker(IDtProject dtProject, String checkId, Long bmObjectId)
    {
        Marker targetMarker = getFirstNestedMarker(checkId, bmObjectId, dtProject);
        assertNotNull(targetMarker);
        return targetMarker;
    }

    /**
     * Checks top object marker if present and return top object id.
     *
     * @param dtProject the dt-project, cannot be {@code null}
     * @param checkId the check id, cannot be {@code null}
     * @param topObjectFqn the target top object fqn, cannot be {@code null}
     * @return top object bm id, cannot be {@code null}
     */
    protected Long checkTopObjectObjectByFqn(IDtProject dtProject, String checkId, String topObjectFqn)
    {
        IBmObject bmObject = getTopObjectByFqn(topObjectFqn, dtProject);
        Long bmObjectId = bmObject.bmGetId();
        checkMarker(dtProject, checkId, bmObjectId);
        return bmObjectId;
    }

    /**
     * Initialize v8 project by name.
     *
     * @param projectName the project name, cannot be {@code null}
     * @return initialized v8 project, can be {@code null}
     * @throws CoreException
     */
    protected IV8Project init(String projectName) throws CoreException
    {
        return projectManager.getProject(testingWorkspace.setUpProject(projectName, getClass()));
    }

    /**
     * Revalidates objects.
     *
     * @param dtProject the dt-project, cannot be {@code null}
     * @param bmObjectIds the object bm ids to revalidate, cannot be {@code null}
     * @param checkId the check id, cannot be {@code null}
     */
    protected void revalidate(IDtProject dtProject, Collection<Object> bmObjectIds, String checkId)
    {
        IBmModel model = bmModelManager.getModel(dtProject);
        model.executeReadonlyTask(new AbstractBmTask<Void>("GetObjectId")
        {
            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
            {
                checker.scheduleValidation(dtProject.getWorkspaceProject(), Collections.singleton(checkId), bmObjectIds,
                    transaction, new NullProgressMonitor());
                return null;
            }
        });
        waitForDD(dtProject);
    }
}
