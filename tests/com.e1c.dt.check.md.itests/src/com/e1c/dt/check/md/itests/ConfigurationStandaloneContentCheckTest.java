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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com.e1c.dt.check.md.ConfigurationStandaloneContentCheck;

/**
 * Tests for the {@link ConfigurationStandaloneContentCheck}
 *
 * @author Andrey Volkov
 */
public class ConfigurationStandaloneContentCheckTest
    extends AbstractMdCheckTest
{
    @Test
    public void ConfigurationStandaloneContentCheckUsed() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationStandaloneContentCheckUsed");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        Collection<Object> topObjectIds =
            checkStandaloneContent(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationStandaloneContentCheck.CHECK_ID);

        checkStandaloneContent(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID);
    }

    @Test
    public void ConfigurationStandaloneContentCheckUnused() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationStandaloneContentCheckUnused");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        Collection<Object> topObjectIds =
            checkStandaloneContent(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationStandaloneContentCheck.CHECK_ID);

        checkStandaloneContent(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID);
    }

    @Test
    public void ConfigurationStandaloneContentCheckPriority() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationStandaloneContentCheckPriority");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        Collection<Object> topObjectIds =
            checkStandaloneContent(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationStandaloneContentCheck.CHECK_ID);

        checkStandaloneContent(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID);
    }

    private Collection<Object> checkStandaloneContent(IDtProject dtProject, String checkId)
    {
        Collection<Object> topObjectIds = new ArrayList<>();
        Configuration configuration = (Configuration)getTopObjectByFqn("Configuration", dtProject);
        IBmObject bmObject = (IBmObject)configuration.getMobileApplicationContent();
        Long bmObjectId = bmObject.bmGetId();
        checkMarker(dtProject, checkId, bmObjectId);
        topObjectIds.add(bmObjectId);
        return topObjectIds;
    }
}
