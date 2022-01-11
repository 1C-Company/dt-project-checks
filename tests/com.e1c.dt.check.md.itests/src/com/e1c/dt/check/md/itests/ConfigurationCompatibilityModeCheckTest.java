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
import com.e1c.dt.check.internal.md.ConfigurationCompatibilityModeCheck;

/**
 * Tests for the {@link ConfigurationCompatibilityModeCheck}
 *
 * @author Andrey Volkov
 */
@SuppressWarnings("nls")
public class ConfigurationCompatibilityModeCheckTest
    extends AbstractMdCheckTest
{
    @Test
    public void testConfigurationCompatibilityModeCheckMax() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationCompatibilityModeCheckMax");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        String expected = "Unsupported compatibility mode, maximal version of compatibility mode is "
            + v8Project.getVersion().toString();

        Collection<Object> topObjectIds =
            checkTopObjects(dtProject, ConfigurationCompatibilityModeCheck.CHECK_ID, expected);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationCompatibilityModeCheck.CHECK_ID);

        checkTopObjects(dtProject, ConfigurationCompatibilityModeCheck.CHECK_ID, expected);
    }

    @Test
    public void testConfigurationCompatibilityModeCheckMin() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationCompatibilityModeCheckMin");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        String expected = "Unsupported compatibility mode, minimal version of compatibility mode is 8.3.6";

        Collection<Object> topObjectIds =
            checkTopObjects(dtProject, ConfigurationCompatibilityModeCheck.CHECK_ID, expected);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationCompatibilityModeCheck.CHECK_ID);

        checkTopObjects(dtProject, ConfigurationCompatibilityModeCheck.CHECK_ID, expected);
    }

    private Collection<Object> checkTopObjects(IDtProject dtProject, String checkId, String expected)
    {
        Collection<Object> topObjectIds = new ArrayList<>();

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Configuration", expected));

        return topObjectIds;
    }
}
