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

import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.AbstractDataPath;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormField;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.Table;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.form.DataPathReferredObjectCheck;
import com.e1c.dt.internal.check.form.itests.SingleProjectWithExtReadOnlyCheckTestBase;

/**
 * Tests for {@link DataPathReferredObjectCheck} check.
 *
 * @author Dmitriy Marmyshev
 */
public class DataPathReferredObjectCheckTest
    extends SingleProjectWithExtReadOnlyCheckTestBase
{
    private static final String CHECK_ID = "form-data-path";

    private static final String PROJECT_NAME = "FormDataPath";
    private static final String PROJECT_EXT_NAME = "FormDataPathExt";

    private static final String FQN_FORM = "CommonForm.ListForm.Form";

    private static final String FQN_FORM2 = "Catalog.Products.Form.ListForm.Form";

    private static final String FQN_FORM3 = "Catalog.Products.Form.ItemForm.Form";

    /**
     * Test the dynamic list form with custom query finds data path to unknown field that not exist in query text
     *
     * @throws Exception the exception
     */
    @Test
    public void testDynamicListFormWithCustomQuery() throws Exception
    {
        List.of(PROJECT_NAME, PROJECT_EXT_NAME).forEach(projName -> {
            IDtProject dtProject = dtProjectManager.getDtProject(projName);
            assertNotNull(dtProject);

            IBmObject object = getTopObjectByFqn(FQN_FORM, dtProject);
            assertTrue(object instanceof Form);
            Form form = (Form)object;
            checkDynamicListFormWithCustomQuery(dtProject, form, false);
            if (form.getBaseForm() != null && !form.getBaseForm().eIsProxy())
            {
                checkDynamicListFormWithCustomQuery(dtProject, form.getBaseForm(), true);
            }
        });
    }

    /**
     * Test the dynamic list form with main table finds data path to unknown field that not exist in the object
     *
     * @throws Exception the exception
     */
    @Test
    public void testListFormWithMainTable() throws Exception
    {
        List.of(PROJECT_NAME, PROJECT_EXT_NAME).forEach(projName -> {
            IDtProject dtProject = dtProjectManager.getDtProject(projName);
            assertNotNull(dtProject);

            IBmObject object = getTopObjectByFqn(FQN_FORM2, dtProject);
            assertTrue(object instanceof Form);

            Form form = (Form)object;
            checkListFormWithMainTableMarkers(dtProject, form, false);
            if (form.getBaseForm() != null && !form.getBaseForm().eIsProxy())
            {
                checkListFormWithMainTableMarkers(dtProject, form.getBaseForm(), true);
            }
        });
    }

    /**
     * Test the object form finds data path to unknown field that not exist in the object
     *
     * @throws Exception the exception
     */
    @Test
    public void testItemForm() throws Exception
    {
        List.of(PROJECT_NAME, PROJECT_EXT_NAME).forEach(projName -> {
            IDtProject dtProject = dtProjectManager.getDtProject(projName);
            assertNotNull(dtProject);

            IBmObject object = getTopObjectByFqn(FQN_FORM3, dtProject);
            assertTrue(object instanceof Form);
            Form form = (Form)object;

            checkItemFormMarkers(dtProject, form, false);
            if (form.getBaseForm() != null && !form.getBaseForm().eIsProxy())
            {
                checkItemFormMarkers(dtProject, form.getBaseForm(), true);
            }
        });
    }

    @Override
    protected String getTestConfigurationName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected String getTestConfigurationExtName()
    {
        return PROJECT_EXT_NAME;
    }

    private void checkDynamicListFormWithCustomQuery(IDtProject dtProject, Form form, boolean isBaseForm)
    {
        FormItem item = getItemByName(form, "List");
        assertTrue(item instanceof Table);

        AbstractDataPath dataPath = ((Table)item).getDataPath();

        Marker marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "ListField1");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "ListField2");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        if (isBaseForm)
        {
            assertNull(marker);
        }
        else
        {
            assertNotNull(marker);
        }
    }

    private void checkListFormWithMainTableMarkers(IDtProject dtProject, Form form, boolean isBaseForm)
    {
        FormItem item = getItemByName(form, "List");
        assertTrue(item instanceof Table);

        AbstractDataPath dataPath = ((Table)item).getDataPath();

        Marker marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        dataPath = ((Table)item).getRowPictureDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "Ref");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "Code");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "SKU");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "Unknown");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        if (isBaseForm)
        {
            assertNull(marker);
        }
        else
        {
            assertNotNull(marker);
        }

        item = getItemByName(form, "Current");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "CurrentUnknown");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        if (isBaseForm)
        {
            assertNull(marker);
        }
        else
        {
            assertNotNull(marker);
        }
    }

    private void checkItemFormMarkers(IDtProject dtProject, Form form, boolean isBaseForm)
    {
        FormItem item = getItemByName(form, "Code");
        assertTrue(item instanceof FormField);

        AbstractDataPath dataPath = ((FormField)item).getDataPath();

        Marker marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "SKU");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        assertNull(marker);

        item = getItemByName(form, "Unknown");
        assertTrue(item instanceof FormField);

        dataPath = ((FormField)item).getDataPath();

        marker = getFirstMarker(CHECK_ID, dataPath, dtProject);
        if (isBaseForm)
        {
            assertNull(marker);
        }
        else
        {
            assertNotNull(marker);
        }
    }

    private FormItem getItemByName(Form form, String name)
    {
        for (TreeIterator<EObject> iterator = form.eAllContents(); iterator.hasNext();)
        {
            EObject child = iterator.next();
            if (child instanceof FormItem && name.equals(((FormItem)child).getName()))
            {
                return (FormItem)child;
            }
        }
        return null;
    }
}
