package com.e1c.dt.check.md.fix.itests;

import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;

import com._1c.g5.v8.dt.core.naming.ISymbolicLinkLocalizer;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IV8ProjectManager;
import com._1c.g5.v8.dt.testing.GuiceModules;
import com._1c.g5.v8.dt.ui.util.OpenHelper;
import com._1c.g5.v8.dt.ui.validation.BmMarkerWrapper;
import com._1c.g5.v8.dt.validation.marker.IMarkerWrapper;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.dt.check.internal.md.itests.ExternalDependenciesModule;
import com.e1c.g5.v8.dt.check.qfix.FixProcessHandle;
import com.e1c.g5.v8.dt.check.qfix.FixVariantDescriptor;
import com.e1c.g5.v8.dt.check.qfix.IFixManager;
import com.e1c.g5.v8.dt.testing.check.CheckTestBase;
import com.google.inject.Inject;

/**
 *  @author alexandr
 *
 *  Abstract tests for the quick fix.
 *
 *  Example of test
 *  1. Create an IDtProject dtProject
 *  2. Prepare an IBmModel with all necessary data
 *  3. Then get a marker Marker marker = getFirstMarker(CHECK_ID, object, dtProject);
 *  4. And applyFix(marker, dtProject, "Fix Title");
 *  5. Get the marker again marker = getFirstMarker(CHECK_ID, object, dtProject);
 *  6. Check that the marker is null assertNull(marker);
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
        IMarkerWrapper markerWrapper = new BmMarkerWrapper(marker, dtProject.getWorkspaceProject(), bmModelManager,
            projectManager, symbolicLinkLocalizer, openHelper);

        FixProcessHandle handle = fixManager.prepareFix(markerWrapper, dtProject);

        Collection<FixVariantDescriptor> variants = fixManager.getApplicableFixVariants(handle);
        variants.stream()
            .filter(variant -> variant.getDescription().matches(targetFixDescription))
            .forEach(variant -> {
                fixManager.selectFixVariant(variant, handle);
                fixManager.executeFix(handle, new NullProgressMonitor());
                fixManager.finishFix(handle);
            });
    }
}