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
package com.e1c.dt.check.md.itests;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.ClassRule;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.platform.version.Version;
import com._1c.g5.v8.dt.testing.GuiceModules;
import com._1c.g5.v8.dt.testing.TestingPlatformSupport;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.itests.ExternalDependenciesModule;
import com.e1c.g5.v8.dt.check.ICheckScheduler;
import com.e1c.g5.v8.dt.testing.check.CheckTestBase;
import com.google.inject.Inject;

/**
 * Abstract tests for the metadata.
 *
 * @author Andrey Volkov
 */
@GuiceModules(modules = { ExternalDependenciesModule.class })
public abstract class AbstractMdCheckTest
    extends CheckTestBase
{
    @ClassRule
    public static TestingPlatformSupport testingPlatformSupport =
        new TestingPlatformSupport(Version.V8_3_8, Version.V8_3_9, Version.V8_3_10, Version.V8_3_11, Version.V8_3_12,
            Version.V8_3_13, Version.V8_3_14, Version.V8_3_15, Version.V8_3_16, Version.V8_3_17, Version.V8_3_18,
            Version.V8_3_19, Version.V8_3_20, Version.V8_3_21, Version.V8_3_22, Version.V8_3_23, Version.V8_3_24);

    @Inject
    protected ICheckScheduler checker;
    @Inject
    private IV8ProjectManager projectManager;

    protected IV8Project init(String projectName) throws CoreException
    {
        return projectManager.getProject(testingWorkspace.setUpProject(projectName, getClass()));
    }

    protected void checkMarker(IDtProject dtProject, String checkId, Long bmObjectId)
    {
        Marker targetMarker = getFirstNestedMarker(checkId, bmObjectId, dtProject);
        assertNotNull(targetMarker);
    }

    protected Long checkTopObjectObjectByFqn(IDtProject dtProject, String checkId, String topObjectFqn)
    {
        IBmObject bmObject = getTopObjectByFqn(topObjectFqn, dtProject);
        Long bmObjectId = bmObject.bmGetId();
        checkMarker(dtProject, checkId, bmObjectId);
        return bmObjectId;
    }

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
