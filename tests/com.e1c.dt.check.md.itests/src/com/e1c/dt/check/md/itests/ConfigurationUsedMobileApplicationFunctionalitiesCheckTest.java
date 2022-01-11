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

import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com.e1c.dt.check.internal.md.ConfigurationUsedMobileApplicationFunctionalitiesCheck;

/**
 * Tests for the {@link ConfigurationUsedMobileApplicationFunctionalitiesCheck}
 *
 * @author Andrey Volkov
 */
@SuppressWarnings("nls")
public class ConfigurationUsedMobileApplicationFunctionalitiesCheckTest
    extends AbstractMdCheckTest
{
    @Test
    public void testCheckMobilePermissions() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationMobilePermissionsCheck");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        String expected = "Unsupported property \"Music library\" for current version of the platform";

        Collection<Object> topObjectIds =
            checkTopObjects(dtProject, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID, expected);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID);

        checkTopObjects(dtProject, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID, expected);
    }

    @Test
    public void testCheckMobilePermissionsSince8315() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationMobilePermissionsSince8315Check");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        String expected = "Unsupported property \"Backup using OS\" for current version of the platform";

        Collection<Object> topObjectIds =
            checkTopObjects(dtProject, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID, expected);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID);

        checkTopObjects(dtProject, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID, expected);
    }

    @Test
    public void testCheckUsedMobileApplicationFunctionalities() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationUsedMobileApplicationFunctionalitiesCheck");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        String expected = "Unsupported property \"Background audio recording\" for current version of the platform";

        Collection<Object> topObjectIds =
            checkTopObjects(dtProject, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID, expected);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID);

        checkTopObjects(dtProject, ConfigurationUsedMobileApplicationFunctionalitiesCheck.CHECK_ID, expected);
    }

    private Collection<Object> checkTopObjects(IDtProject dtProject, String checkId, String expected)
    {
        Collection<Object> topObjectIds = new ArrayList<>();

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Configuration", expected));

        return topObjectIds;
    }
}
