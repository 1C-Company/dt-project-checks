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
import com.e1c.dt.check.internal.md.MdObjectUniqueNameCheck;

/**
 * Tests for the {@link ConfigurationCompatibilityModeCheck}
 *
 * @author Andrey Volkov
 */
@SuppressWarnings("nls")
public class MdObjectUniqueNameCheckTest
    extends AbstractMdCheckTest
{
    @Test
    public void testMdObjectUniqueNameCheck() throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init("MdObjectUniqueNameCheck");
        IDtProject dtProject = v8Project.getDtProject();

        waitForDD(dtProject);

        Collection<Object> topObjectIds = checkTopObjects(dtProject, MdObjectUniqueNameCheck.CHECK_ID);

        // Re-validate same object
        revalidate(dtProject, topObjectIds, MdObjectUniqueNameCheck.CHECK_ID);

        checkTopObjects(dtProject, MdObjectUniqueNameCheck.CHECK_ID);
    }

    private Collection<Object> checkTopObjects(IDtProject dtProject, String checkId)
    {
        Collection<Object> topObjectIds = new ArrayList<>();

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Catalog.CatalogWithTwoNonUniqueNameAttribute",
            "Invalid the property \"name\". Duplicate metadata object name \"AttributeNotUniqueName\""));

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Catalog.CatalogWithTwoNonUniqueNameTabularSection",
            "Invalid the property \"name\". Duplicate metadata object name \"TabularSectionNotUniqueName\""));

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Catalog.CatalogWithTwoNonUniqueNameTabularSectionAttribute",
            "Invalid the property \"name\". Duplicate metadata object name \"AttributeNotUniqueName\""));

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Configuration",
            "Invalid the property \"name\". Duplicate metadata object name \"English\""));

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Document.DocumentNotUniqueName",
            "Invalid the property \"name\". Duplicate metadata object name \"DocumentNotUniqueName\""));

        topObjectIds.add(checkTopObjectObjectByFqn(dtProject, checkId, "Document.DocumentNotUniqueName1",
            "Invalid the property \"name\". Duplicate metadata object name \"DocumentNotUniqueName\""));

        return topObjectIds;
    }
}
