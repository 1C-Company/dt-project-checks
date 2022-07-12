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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;

/**
 * Tests for the {@link ConfigurationCompatibilityModeCheck}
 *
 * @author Andrey Volkov
 */
public class ConfigurationMobileApplicationUrlsCheckTest
    extends AbstractMdCheckTest
{
    @Test
    public void testMdObjectUniqueNameCheck() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("ConfigurationMobileApplicationUrlsCheck");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        Collection<Object> topObjectIds = checkTopObjects(dtProject, ConfigurationMobileApplicationUrlsCheck.CHECK_ID);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, ConfigurationMobileApplicationUrlsCheck.CHECK_ID);

        checkTopObjects(dtProject, ConfigurationMobileApplicationUrlsCheck.CHECK_ID);
    }

    private Collection<Object> checkTopObjects(IDtProject dtProject, String checkId)
    {
        Collection<Object> topObjectIds = new ArrayList<>();
        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Configuration"));
        return topObjectIds;
    }
}
