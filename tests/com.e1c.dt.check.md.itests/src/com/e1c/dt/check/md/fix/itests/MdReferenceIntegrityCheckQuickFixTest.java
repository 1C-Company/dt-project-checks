/**
 * Copyright (C) 2022, 1C
 */
package com.e1c.dt.check.md.fix.itests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.validation.marker.Marker;

/**
 * @author Alexandr Sanarov
 *
 */
public class MdReferenceIntegrityCheckQuickFixTest
    extends AbstractFixTest
{
    private static final String CHECK_ID = "md-reference-intergrity";

    private static final String PROJECT_NAME = "MdIntegrity";

    private static final String CATALOG_TYPE = "Catalog.";
    private static final String CATALOG_NAME = "Catalog";
    private static final String FQN_CATALOG = CATALOG_TYPE + CATALOG_NAME;
    private static final String FQN_CONFIGURATION = "Configuration";

    @Test
    public void testMdObjectRemovalByQuickFix() throws CoreException
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

        // Configuration references
        IBmObject object = getTopObjectByFqn(FQN_CONFIGURATION, dtProject);
        Marker marker = getFirstMarker(CHECK_ID, object, dtProject);

        // make fix
        applyFix(marker, dtProject, "Удаление прокси-ссылки"); //$NON-NLS-1$
        waitForDD(dtProject);
        object = getTopObjectByFqn(FQN_CONFIGURATION, dtProject);
        marker = getFirstMarker(CHECK_ID, object, dtProject);
        waitForDD(dtProject);
        assertFalse(model.isDisposed());
        assertNull(marker);
    }
}
