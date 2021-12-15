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
package com.e1c.dt.check.form.itests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.EObject;
import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormField;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.g5.v8.dt.testing.check.SingleProjectReadOnlyCheckTestBase;

/**
 * @author Dmitriy Marmyshev
 *
 */
public class NamedElementNameCheckTest
    extends SingleProjectReadOnlyCheckTestBase
{

    private static final String CHECK_ID = "form-named-element-name";

    private static final String PROJECT_NAME = "NamedElementName";

    private static final String FQN_FORM = "CommonForm.IncorrectCommandBar.Form";

    private static final String FQN_FORM2 = "CommonForm.IncorrectItem.Form";

    private static final String FQN_FORM3 = "CommonForm.Correct.Form";

    @Test
    public void testNameIsEmpty() throws Exception
    {

        IDtProject dtProject = dtProjectManager.getDtProject(PROJECT_NAME);
        assertNotNull(dtProject);

        IBmObject object = getTopObjectByFqn(FQN_FORM, dtProject);
        assertTrue(object instanceof Form);
        Form form = (Form)object;

        EObject item = form.getAutoCommandBar();
        Marker marker = getFirstMarker(CHECK_ID, item, dtProject);
        assertNotNull(marker);

    }

    @Test
    public void testNameIsIncorrect() throws Exception
    {

        IDtProject dtProject = dtProjectManager.getDtProject(PROJECT_NAME);
        assertNotNull(dtProject);

        IBmObject object = getTopObjectByFqn(FQN_FORM2, dtProject);
        assertTrue(object instanceof Form);
        Form form = (Form)object;

        EObject item = form.getItems().get(0);
        Marker marker = getFirstMarker(CHECK_ID, item, dtProject);
        assertNotNull(marker);
        assertTrue(item instanceof FormField);

        marker = getFirstMarker(CHECK_ID, ((FormField)item).getExtendedTooltip(), dtProject);
        assertNotNull(marker);

        marker = getFirstMarker(CHECK_ID, ((FormField)item).getContextMenu(), dtProject);
        assertNotNull(marker);

        item = form.getAttributes().get(0);
        marker = getFirstMarker(CHECK_ID, item, dtProject);
        assertNotNull(marker);

    }

    @Test
    public void testFormNamesCorrect() throws Exception
    {

        IDtProject dtProject = dtProjectManager.getDtProject(PROJECT_NAME);
        assertNotNull(dtProject);

        IBmObject object = getTopObjectByFqn(FQN_FORM3, dtProject);
        assertTrue(object instanceof Form);
        Form form = (Form)object;

        Marker marker = getFirstNestedMarker(CHECK_ID, form, dtProject);
        assertNull(marker);

    }

    @Override
    protected String getTestConfigurationName()
    {
        return PROJECT_NAME;
    }
}
