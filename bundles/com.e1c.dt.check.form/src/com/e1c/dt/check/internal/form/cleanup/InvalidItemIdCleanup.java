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
package com.e1c.dt.check.internal.form.cleanup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.model.EditingMode;
import com._1c.g5.v8.dt.core.model.IModelEditingSupport;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IConfigurationProvider;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectObjectTasksProvider;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.dt.check.internal.form.IInvalidItemIdService;
import com.google.inject.Inject;

/**
 * Cleans up invalid identifiers of form items.
 * <p/>
 * This implementation delegates actual checking of forms and fixing of found issues to {@link IInvalidItemIdService}.
 * This class contains just the wiring into cleanup infrastructure.
 * <p/>
 * Cleanup will be done only if it`s safe to do so:
 * <ul>
 * <li>project has a model and a configuration (that has been loaded) associated with it</li>
 * <li>the model is allowed to be edited directly as per {@link IModelEditingSupport}</li>
 * </ul>
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdCleanup
    implements ICleanUpProjectObjectTasksProvider
{

    /**
     * Service used to obtain project model.
     */
    private final IBmModelManager bmModelManager;

    /**
     * Service used to check if editing is allowed.
     */
    private final IModelEditingSupport editingSupport;

    /**
     * Service used to obtain project configuration.
     */
    private final IConfigurationProvider configurationProvider;

    /**
     * Service to delegate checking and fixing to.
     */
    private final IInvalidItemIdService invalidItemIdService;

    /**
     * Creates new instance.
     *
     * @param invalidItemIdService Service to which delegate actual checking and fixing of form item identifiers.
     * Must not be {@code null}.
     * @param configurationProvider Service instance that is to be used to obtain configuration.
     * Must not be {@code null}.
     * @param bmModelManager Service instance that is to be used to obtain project model.
     * Must not be {@code null}.
     * @param editingSupport Service instance that is to be used to check if we're allowed to change configuration.
     * Must not be {@code null}.
     */
    @Inject
    public InvalidItemIdCleanup(IInvalidItemIdService invalidItemIdService,
        IConfigurationProvider configurationProvider, IBmModelManager bmModelManager,
        IModelEditingSupport editingSupport)
    {
        Objects.requireNonNull(bmModelManager, "bmModelManager"); //$NON-NLS-1$
        Objects.requireNonNull(editingSupport, "editingSupport"); //$NON-NLS-1$
        Objects.requireNonNull(configurationProvider, "configurationProvider"); //$NON-NLS-1$
        Objects.requireNonNull(invalidItemIdService, "invalidItemIdService"); //$NON-NLS-1$
        this.bmModelManager = bmModelManager;
        this.editingSupport = editingSupport;
        this.configurationProvider = configurationProvider;
        this.invalidItemIdService = invalidItemIdService;
    }

    @Override
    public List<ICleanUpBmObjectTask> getCleanUpProjectTasks(IDtProject project)
    {
        Configuration configuration = configurationProvider.getConfiguration(project);
        IBmModel model = bmModelManager.getModel(project);
        boolean isSafeToClean = configuration != null && !configuration.eIsProxy()
            && editingSupport.canEdit(configuration, EditingMode.DIRECT) && model != null;
        if (!isSafeToClean)
        {
            CorePlugin.trace(IInvalidItemIdService.DEBUG_OPTION,
                "Clean: Skipping cleanup for safety reasons: configuration={0}, model={1}", //$NON-NLS-1$
                configuration, model);
            return Collections.emptyList();
        }
        return model.executeReadonlyTask(new CollectCleanupTasksTask(), false);
    }

    /**
     * Task that checks what needs to be fixed and returns fixing tasks.
     * <p/>
     * To conserve memory this implementation will load each form one-by-one
     * (by asking a transaction to iterate over all top objects that are {@link Form}),
     * check it and then immediately unload ({@link IBmTransaction#evict(long)}) it.
     * <p/>
     * The implementation depends on {@link IInvalidItemIdService#isValid(Form)}
     * to short-circuit after the first issue is detected to minimize double effort
     * to validate form again in fixing tasks - {@link InvalidFormCleanupTask}.
     */
    private class CollectCleanupTasksTask
        extends AbstractBmTask<List<ICleanUpBmObjectTask>>
    {

        /**
         * Creates an instance.
         */
        CollectCleanupTasksTask()
        {
            super(Messages.InvalidItemIdCleanup_Searching_invalid_form_item_identifiers);
        }

        @Override
        public List<ICleanUpBmObjectTask> execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
        {
            CorePlugin.trace(IInvalidItemIdService.DEBUG_OPTION, "Cleanup: Analyzing configuration"); //$NON-NLS-1$
            Iterator<IBmObject> formIterator = transaction.getTopObjectIterator(FormPackage.Literals.FORM);
            List<ICleanUpBmObjectTask> fixTasks = new ArrayList<>();
            while (formIterator.hasNext())
            {
                Form form = (Form)formIterator.next();
                if (!invalidItemIdService.isValid(form))
                {
                    fixTasks.add(new InvalidFormCleanupTask(form.bmGetId()));
                }
                transaction.evict(form.bmGetId());
            }
            return fixTasks;
        }

    }

    /**
     * Task that fixes invalid identifiers of all {@link FormItem} on the specified form.
     * <p/>
     * This implementation first loads the form by its identifier,
     * then delegates to {@link IInvalidItemIdService#validate(Form)} to
     * find all invalid form items and then delegates to {@link IInvalidItemIdService#fix(FormItem)}
     * to fix each one.
     */
    private class InvalidFormCleanupTask
        extends AbstractBmTask<Void>
        implements ICleanUpBmObjectTask
    {

        /**
         * Identifier of the form that needs to be fixed.
         */
        private final long formIdToFix;

        /**
         * Creates new instance.
         * @param formIdToFix Identifier of the form that needs to be fixed.
         */
        InvalidFormCleanupTask(long formIdToFix)
        {
            super(Messages.InvalidItemIdCleanup_Fixing_invalid_form_item_identifier);
            this.formIdToFix = formIdToFix;
        }

        @Override
        public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
        {
            Form form = (Form)transaction.getObjectById(formIdToFix);
            invalidItemIdService.validate(form).keySet().forEach(invalidItemIdService::fix);
            return null;
        }

    }

}
