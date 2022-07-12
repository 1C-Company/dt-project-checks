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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.emf.ecore.EReference;
import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.scc.model.StandaloneContent;
import com._1c.g5.v8.dt.scc.model.StandaloneContentMdObjectAware;
import com._1c.g5.v8.dt.scc.model.StandaloneContentPackage;
import com._1c.g5.v8.dt.validation.marker.BmObjectMarker;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.check.ConfigurationStandaloneContentCheck;

/**
 * Tests for the {@link ConfigurationStandaloneContentFix}.
 *
 * @author Andrey Volkov
 */
public class ConfigurationStandaloneContentFixTest
    extends AbstractMdFixTest
{
    @Test
    public void configurationStandaloneContentFixPriority() throws Exception
    {
        testProject("ConfigurationStandaloneContentCheckPriority",
            StandaloneContentPackage.Literals.STANDALONE_CONTENT__PRIORITY_ITEM);
    }

    @Test
    public void configurationStandaloneContentFixUnused() throws Exception
    {
        testProject("ConfigurationStandaloneContentCheckUnused",
            StandaloneContentPackage.Literals.STANDALONE_CONTENT__UNUSED_ITEM);
    }

    @Test
    public void configurationStandaloneContentFixUsed() throws Exception
    {
        testProject("ConfigurationStandaloneContentCheckUsed",
            StandaloneContentPackage.Literals.STANDALONE_CONTENT__USED_ITEM);
    }

    private void checkNoMarker(String checkId, IBmObject bmObject, IDtProject project, MdObject targetMdObject)
    {
        Marker[] nestedMarkers = markerManager.getNestedMarkers(project.getWorkspaceProject(), bmObject.bmGetId());

        assertFalse(Arrays.stream(nestedMarkers).anyMatch(marker -> {
            BmObjectMarker bmMarker = (BmObjectMarker)marker;
            IBmObject badBmObject = bmObject.bmGetEngine().getObjectById(bmMarker.getObjectId());
            return Objects.equals(targetMdObject, ((StandaloneContentMdObjectAware)badBmObject).getMetadata());
        }));
    }

    @SuppressWarnings("unchecked")
    private void testProject(String checkProjectName, EReference eReference) throws Exception
    {
        // Start project and wait until all checks are performed
        IV8Project v8Project = init(checkProjectName);
        IDtProject dtProject = v8Project.getDtProject();
        assertNotNull(dtProject);

        waitForDD(dtProject);

        IBmObject object = getTopObjectByFqn("Configuration", dtProject);
        assertTrue(object instanceof Configuration);
        Configuration configuration = (Configuration)object;
        StandaloneContent mobileApplicationContent = (StandaloneContent)configuration.getMobileApplicationContent();
        IBmObject bmObject = (IBmObject)mobileApplicationContent;

        // check marker
        BmObjectMarker bmMarker =
            (BmObjectMarker)checkMarker(dtProject, ConfigurationStandaloneContentCheck.CHECK_ID, bmObject.bmGetId());

        // check contains
        long badObjectReferenceBmId = bmMarker.getObjectId();
        IBmObject badBmObject = bmObject.bmGetEngine().getObjectById(badObjectReferenceBmId);
        MdObject badMdObject = ((StandaloneContentMdObjectAware)badBmObject).getMetadata();

        // make fix
        applyFix(bmMarker, dtProject, Messages.ConfigurationStandaloneContentFix_Remove_bad_content_item_title);

        waitForDD(dtProject);

        object = getTopObjectByFqn("Configuration", dtProject);
        assertTrue(object instanceof Configuration);
        mobileApplicationContent = (StandaloneContent)configuration.getMobileApplicationContent();
        bmObject = (IBmObject)mobileApplicationContent;

        // check no marker
        checkNoMarker(ConfigurationStandaloneContentCheck.CHECK_ID, bmObject, dtProject, badMdObject);

        // check no contains
        assertFalse(((Collection<StandaloneContentMdObjectAware>)mobileApplicationContent.eGet(eReference)).stream()
            .filter(i -> Objects.equals(badMdObject, i.getMetadata()))
            .findAny()
            .isPresent());
    }
}
