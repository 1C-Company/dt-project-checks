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
package com.e1c.dt.check.internal.md.itests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.ClassRule;
import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.metadata.mdclass.Catalog;
import com._1c.g5.v8.dt.metadata.mdclass.CommonAttribute;
import com._1c.g5.v8.dt.metadata.mdclass.CommonAttributeContentItem;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.Document;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassFactory;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.metadata.mdclass.Subsystem;
import com._1c.g5.v8.dt.platform.version.Version;
import com._1c.g5.v8.dt.testing.TestingPlatformSupport;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.MdReferenceIntegrity;
import com.e1c.g5.v8.dt.testing.check.CheckTestBase;

/**
 * Tests for {@link MdReferenceIntegrity} check.
 *
 * @author Alexander Tretyakevich
 */
public class MdReferenceIntegrityCheckTest
    extends CheckTestBase
{
    @ClassRule
    public static TestingPlatformSupport testingPlatformSupport = new TestingPlatformSupport(Version.V8_3_20);

    private static final String CHECK_ID = "md-reference-intergrity";

    private static final String PROJECT_NAME = "MdIntegrity";

    private static final String CATALOG_TYPE = "Catalog.";
    private static final String CATALOG_NAME = "Catalog";
    private static final String CATALOG_NAME1 = "Catalog1";
    private static final String CATALOG_NAME2 = "Catalog2";
    private static final String FQN_CATALOG = CATALOG_TYPE + CATALOG_NAME;
    private static final String FQN_CATALOG1 = CATALOG_TYPE + CATALOG_NAME1;
    private static final String FQN_CATALOG2 = CATALOG_TYPE + CATALOG_NAME2;

    private static final String DOCUMENT_TYPE = "Document.";
    private static final String DOCUMENT_NAME = "Document";
    private static final String DOCUMENT_NAME1 = "Document1";
    private static final String DOCUMENT_NAME2 = "Document2";
    private static final String FQN_DOCUMENT = DOCUMENT_TYPE + DOCUMENT_NAME;
    private static final String FQN_DOCUMENT1 = DOCUMENT_TYPE + DOCUMENT_NAME1;
    private static final String FQN_DOCUMENT2 = DOCUMENT_TYPE + DOCUMENT_NAME2;

    private static final String SUBSYSTEM_TYPE = "Subsystem.";
    private static final String SUBSYSTEM_NAME = "Subsystem";
    private static final String FQN_SUBSYSTEM = SUBSYSTEM_TYPE + SUBSYSTEM_NAME;
    private static final String FQN_CHILD_SUBSYSTEM = SUBSYSTEM_TYPE + "Subsystem.Subsystem.Subsystem";

    private static final String FQN_COMMON_ATTRIBUTE = "CommonAttribute.CommonAttribute";
    private static final String FQN_CONFIGURATION = "Configuration";

    /**
     * Test the removal of the object and corresponding breaking of the integrity for supported objects
     * @throws CoreException
     */
    @Test
    public void testMdObjectRemoval() throws CoreException
    {
        openProjectAndWaitForValidationFinish(PROJECT_NAME);
        IDtProject dtProject = dtProjectManager.getDtProject(PROJECT_NAME);

        IBmModel model = bmModelManager.getModel(dtProject);
        model.execute(new AbstractBmTask<Void>("RemoveObjects") //$NON-NLS-1$
        {
            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
            {
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG1));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG2));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_DOCUMENT));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_DOCUMENT1));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_DOCUMENT2));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CHILD_SUBSYSTEM));

                return null;
            }
        });

        waitForDD(dtProject);

        // Configuration references
        IBmObject object = getTopObjectByFqn(FQN_CONFIGURATION, dtProject);
        Marker marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNotNull(marker);

        // Subsystem content references
        object = getTopObjectByFqn(FQN_SUBSYSTEM, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNotNull(marker);

        // Common attribute references
        CommonAttribute commonAttribute = (CommonAttribute)getTopObjectByFqn(FQN_COMMON_ATTRIBUTE, dtProject);
        for (CommonAttributeContentItem contentItem : commonAttribute.getContent())
        {
            marker = getFirstMarker(CHECK_ID, contentItem, dtProject);
            assertNotNull(marker);
        }

        // Subsystem content reference
        object = getTopObjectByFqn(FQN_SUBSYSTEM, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNotNull(marker);
    }

    /**
     * Test the clearance of error markers on restoration of previously hard-removed objects
     * @throws CoreException
     */
    @Test
    public void testClearanceOnRestoration() throws CoreException
    {
        openProjectAndWaitForValidationFinish(PROJECT_NAME);
        IDtProject dtProject = dtProjectManager.getDtProject(PROJECT_NAME);

        IBmModel model = bmModelManager.getModel(dtProject);
        model.execute(new AbstractBmTask<Void>("RemoveObjects") //$NON-NLS-1$
        {
            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
            {
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG1));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG2));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_DOCUMENT));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_DOCUMENT1));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_DOCUMENT2));
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CHILD_SUBSYSTEM));

                return null;
            }
        });

        waitForDD(dtProject);

        model.execute(new AbstractBmTask<Void>("ReAddObjects") //$NON-NLS-1$
        {
            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
            {
                Catalog catalog = MdClassFactory.eINSTANCE.createCatalog();
                catalog.setName(CATALOG_NAME);
                transaction.attachTopObject((IBmObject)catalog, FQN_CATALOG);
                catalog = MdClassFactory.eINSTANCE.createCatalog();
                catalog.setName(CATALOG_NAME1);
                transaction.attachTopObject((IBmObject)catalog, FQN_CATALOG1);
                catalog = MdClassFactory.eINSTANCE.createCatalog();
                catalog.setName(CATALOG_NAME2);
                transaction.attachTopObject((IBmObject)catalog, FQN_CATALOG2);

                Document document = MdClassFactory.eINSTANCE.createDocument();
                document.setName(DOCUMENT_NAME);
                transaction.attachTopObject((IBmObject)document, FQN_DOCUMENT);
                document = MdClassFactory.eINSTANCE.createDocument();
                document.setName(DOCUMENT_NAME1);
                transaction.attachTopObject((IBmObject)document, FQN_DOCUMENT1);
                document = MdClassFactory.eINSTANCE.createDocument();
                document.setName(DOCUMENT_NAME2);
                transaction.attachTopObject((IBmObject)document, FQN_DOCUMENT2);

                Subsystem subsystem = MdClassFactory.eINSTANCE.createSubsystem();
                subsystem.setName(SUBSYSTEM_NAME);
                transaction.attachTopObject((IBmObject)subsystem, FQN_CHILD_SUBSYSTEM);

                return null;
            }
        });

        waitForDD(dtProject);

        // Configuration references
        IBmObject object = getTopObjectByFqn(FQN_CONFIGURATION, dtProject);
        Marker marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNull(marker);

        // Subsystem content references
        object = getTopObjectByFqn(FQN_SUBSYSTEM, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNull(marker);

        // Common attribute references
        CommonAttribute commonAttribute = (CommonAttribute)getTopObjectByFqn(FQN_COMMON_ATTRIBUTE, dtProject);
        for (CommonAttributeContentItem contentItem : commonAttribute.getContent())
        {
            marker = getFirstMarker(CHECK_ID, contentItem, dtProject);
            assertNull(marker);
        }

        // Subsystem content reference
        object = getTopObjectByFqn(FQN_SUBSYSTEM, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNull(marker);
    }

    /**
     * Test the clearance of the marker in case of the manual removal of the stale reference/holding object
     * @throws CoreException
     */
    @Test
    public void testStaleReferenceClearing() throws CoreException
    {
        openProjectAndWaitForValidationFinish(PROJECT_NAME);
        IDtProject dtProject = dtProjectManager.getDtProject(PROJECT_NAME);

        IBmModel model = bmModelManager.getModel(dtProject);
        // First we need to remove objects to receive stale links
        model.execute(new AbstractBmTask<Void>("RemoveObjects") //$NON-NLS-1$
        {
            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
            {
                transaction.detachTopObject(transaction.getTopObjectByFqn(FQN_CATALOG));

                return null;
            }
        });

        waitForDD(dtProject);

        // Next we need to remove stale links manually
        model.execute(new AbstractBmTask<Void>("RemoveStaleReferences") //$NON-NLS-1$
        {
            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
            {
                Configuration configuration = (Configuration)transaction.getTopObjectByFqn(FQN_CONFIGURATION);
                int idx = 0;
                for (Catalog catalog : configuration.getCatalogs())
                {
                    if (catalog.eIsProxy())
                    {
                        configuration.getCatalogs().remove(idx);
                        break;
                    }

                    idx++;
                }

                Subsystem subsystem = (Subsystem)transaction.getTopObjectByFqn(FQN_SUBSYSTEM);
                idx = 0;
                for (MdObject mdObject : subsystem.getContent())
                {
                    if (mdObject.eIsProxy())
                    {
                        subsystem.getContent().remove(idx);
                        break;
                    }

                    idx++;
                }

                idx = 0;
                for (Subsystem childSubsystem : subsystem.getSubsystems())
                {
                    if (childSubsystem.eIsProxy())
                    {
                        subsystem.getSubsystems().remove(idx);
                        break;
                    }

                    idx++;
                }

                CommonAttribute commonAttribute = (CommonAttribute)transaction.getTopObjectByFqn(FQN_COMMON_ATTRIBUTE);
                idx = 0;
                for (CommonAttributeContentItem item : commonAttribute.getContent())
                {
                    if (item.getMetadata().eIsProxy())
                    {
                        commonAttribute.getContent().remove(idx);
                        break;
                    }

                    idx++;
                }

                return null;
            }
        });

        waitForDD(dtProject);

        // Configuration references
        IBmObject object = getTopObjectByFqn(FQN_CONFIGURATION, dtProject);
        Marker marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNull(marker);

        // Subsystem content references
        object = getTopObjectByFqn(FQN_SUBSYSTEM, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNull(marker);

        // Common attribute references
        CommonAttribute commonAttribute = (CommonAttribute)getTopObjectByFqn(FQN_COMMON_ATTRIBUTE, dtProject);
        for (CommonAttributeContentItem contentItem : commonAttribute.getContent())
        {
            marker = getFirstMarker(CHECK_ID, contentItem, dtProject);
            assertNull(marker);
        }

        // Subsystem content reference
        object = getTopObjectByFqn(FQN_SUBSYSTEM, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        assertNull(marker);
    }
}
