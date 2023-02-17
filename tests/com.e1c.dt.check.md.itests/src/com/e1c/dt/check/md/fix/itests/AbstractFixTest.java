package com.e1c.dt.check.md.fix.itests;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.naming.ISymbolicLinkLocalizer;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.metadata.mdclass.Catalog;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.testing.GuiceModules;
import com._1c.g5.v8.dt.ui.util.OpenHelper;
import com._1c.g5.v8.dt.ui.validation.BmMarkerWrapper;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.itests.ExternalDependenciesModule;
import com.e1c.g5.v8.dt.check.qfix.FixVariantDescriptor;
import com.e1c.g5.v8.dt.check.qfix.IFixManager;
import com.e1c.g5.v8.dt.testing.check.CheckTestBase;
import com.google.inject.Inject;

/**
 *
 *  Abstract tests for the quick fix.
 *
 *  Example of test:
 *  <ol>
 *  <li>Create an IDtProject dtProject</li>
 *  <li>Prepare an IBmModel with all necessary data</li>
 *  <li>Then get a marker Marker marker = getFirstMarker(CHECK_ID, object, dtProject);</li>
 *  <li>And applyFix(marker, dtProject, "Fix Title");</li>
 *  <li>Get the marker again marker = getFirstMarker(CHECK_ID, object, dtProject);</li>
 *  <li>Check that the marker is null assertNull(marker);</li>
 *  </ol>
 *
 *  @author Alexandr Sanarov
 *
 */
@GuiceModules(modules = { ExternalDependenciesModule.class })
public abstract class AbstractFixTest
    extends CheckTestBase
{
    private final OpenHelper openHelper = new OpenHelper();

    @Inject
    protected IV8ProjectManager projectManager;
    @Inject
    private ISymbolicLinkLocalizer symbolicLinkLocalizer;
    @Inject
    private IFixManager fixManager;

    /**
     *
     * Method which applies a quick fix for error on the marker.
     *
     * @param marker {@link Marker} marker with error. May not be {@code null}.
     * @param dtProject {@link IDtProject} EDT project. May not be {@code null}. Should match the sub-folder name of the 'workspaces' folder
     * @param targetFixDescription {@link String} Fix title. May not be {@code null}.
     */
    public void applyFix(Marker marker, IDtProject dtProject, String targetFixDescription)
    {
        var markerWrapper = new BmMarkerWrapper(marker, dtProject.getWorkspaceProject(), bmModelManager,
            projectManager, symbolicLinkLocalizer, openHelper);

        var handle = fixManager.prepareFix(markerWrapper, dtProject);
        var<FixVariantDescriptor> variants = fixManager.getApplicableFixVariants(handle);

        var variant = variants.iterator().next();
        fixManager.selectFixVariant(variant, handle);
        fixManager.executeFix(handle, new NullProgressMonitor());
        fixManager.finishFix(handle);
    }

    /**
     *
     * Asserts that an quick fix has done well.
     *
     * @param object
     * @param dtProject
     * @param marker
     */
    public void assertQuickFixResult(IBmObject object, IDtProject dtProject, Marker marker)
    {
        boolean check = true;
        Configuration configuration = (Configuration)object;
        for (Catalog catalog : configuration.getCatalogs())
        {
            if (catalog.eIsProxy())
            {
                check = false;
                break;
            }
        }
        waitForDD(dtProject);
        assertNull(marker);
        assertTrue(check);
    }
}