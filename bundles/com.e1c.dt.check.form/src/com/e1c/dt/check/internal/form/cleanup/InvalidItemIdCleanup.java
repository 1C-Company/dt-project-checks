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
import org.eclipse.emf.ecore.EClass;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.model.EditingMode;
import com._1c.g5.v8.dt.core.model.IModelEditingSupport;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectObjectTasksProvider;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.dt.check.internal.form.IInvalidItemIdService;
import com.google.inject.Inject;

/**
 * Cleans up invalid identifiers of form items.
 * <p/>
 * This implementation delegates actual checking of forms and fixing of found issues to {@link IInvalidItemIdService}.
 * This class contains just the wiring into cleanup infrastructure:
 * <ul>
 * <li>If BM instance can be obtained then non-lightweight read-only transaction is used to list forms
 * (via {@link IBmTransaction#getTopObjectIterator(EClass)}) and</li>
 * <li>for each form it is checked if it can be edited. Ineditable froms are omitted from validation and cleanup.</li>
 * <li>Quick form checking is delegated to {@link IInvalidItemIdService}</li>
 * <li>For each form with issues, a separate cleanup task will be returned for further management by
 * {@code com._1c.g5.v8.dt.internal.migration.cleanup.CleanUpProjectSourcesManager}.</li>
 * <li>After analsis of the form, it will be evicted from the transaction to conserve memory</li>
 * <li>Form cleanup tasks will obtain corresponding form (by its identifier) from the transaction,
 * perform full validation thanks to {@link IInvalidItemIdService} and fix each malformed form item identifier
 * using the same service.</li>
 * </ul>
 * Both scanning (before quick-checking each form) and
 * fixing (before loading form to do full validation and before fixing each form item)
 * will be stopped as soon as cancellation is reported.
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
     * Service to delegate checking and fixing to.
     */
    private final IInvalidItemIdService invalidItemIdService;

    /**
     * Creates new instance.
     *
     * @param invalidItemIdService Service to which delegate actual checking and fixing of form item identifiers.
     * Must not be {@code null}.
     * @param bmModelManager Service instance that is to be used to obtain project model.
     * Must not be {@code null}.
     * @param editingSupport Service instance that is to be used to check if we're allowed to change forms.
     * Must not be {@code null}.
     */
    @Inject
    public InvalidItemIdCleanup(IInvalidItemIdService invalidItemIdService, IBmModelManager bmModelManager,
        IModelEditingSupport editingSupport)
    {
        Objects.requireNonNull(bmModelManager, "bmModelManager"); //$NON-NLS-1$
        Objects.requireNonNull(editingSupport, "editingSupport"); //$NON-NLS-1$
        Objects.requireNonNull(invalidItemIdService, "invalidItemIdService"); //$NON-NLS-1$
        this.bmModelManager = bmModelManager;
        this.editingSupport = editingSupport;
        this.invalidItemIdService = invalidItemIdService;
    }

    @Override
    public List<ICleanUpBmObjectTask> getCleanUpProjectTasks(IDtProject project)
    {
        IBmModel model = bmModelManager.getModel(project);
        if (model == null)
        {
            CorePlugin.trace(IInvalidItemIdService.DEBUG_OPTION,
                "Clean: Skipping cleanup because cannot obtain BM instance"); //$NON-NLS-1$
            return Collections.emptyList();
        }
        return model.executeReadonlyTask(new CollectCleanupTasksTask(), false);
    }

    /**
     * Task that checks what needs to be fixed and returns fixing tasks.
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
            while (formIterator.hasNext() && !progressMonitor.isCanceled())
            {
                Form form = (Form)formIterator.next();
                if (editingSupport.canEdit(form, EditingMode.DIRECT) && !invalidItemIdService.isValid(form))
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
            if (progressMonitor.isCanceled())
            {
                return null;
            }
            Form form = (Form)transaction.getObjectById(formIdToFix);
            for (FormItem itemToFix : invalidItemIdService.validate(form).keySet())
            {
                if (progressMonitor.isCanceled())
                {
                    return null;
                }
                invalidItemIdService.fix(itemToFix);
            }
            return null;
        }

    }

}
