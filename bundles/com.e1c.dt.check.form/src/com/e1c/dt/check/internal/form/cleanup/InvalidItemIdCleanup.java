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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    @Inject
    private IBmModelManager bmModelManager;

    /**
     * Service used to check if editing is allowed.
     */
    @Inject
    private IModelEditingSupport editingSupport;

    /**
     * Service used to obtain project configuration.
     */
    @Inject
    private IConfigurationProvider configurationProvider;

    /**
     * Service to delegate checking and fixing to.
     */
    @Inject
    private IInvalidItemIdService service;

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
     * This is a read-only task that does not modify model.
     * However, it will {@link IBmTransaction#evict()} every form
     * so they're not modified until cleanup process is finished.
     * The fixing itself happens in tasks that this task returns.
     * <p/>
     * The implementation asks a transaction for all top objects that are {@link Form}
     * and passes each of those to {@link IInvalidItemIdService#validate(Form)} for validation.
     * Each resulting {@link FormItem} is passed to separate {@link InvalidItemIdCleanupTask}
     * and those tasks are then returned as a result.
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
            List<Form> forms = StreamSupport.stream(Spliterators.spliteratorUnknownSize(formIterator, 0), false)
                .filter(Form.class::isInstance)
                .map(Form.class::cast)
                .collect(Collectors.toList());
            forms.forEach(form -> transaction.evict(form.bmGetId()));
            return forms.stream()
                .flatMap(this::validateForm)
                .map(InvalidItemIdCleanupTask::new)
                .collect(Collectors.toList());
        }

        /**
         * Validates a form and returns items that need fixing.
         *
         * @param form Form to be validated. Must not be {@code null}.
         * @return Items that need to be fixed. Never {@code null}.
         */
        private Stream<FormItem> validateForm(Form form)
        {
            return service.validate(form).keySet().stream();
        }

    }

    /**
     * Task that fixes invalid identifier of the individual specified {@link FormItem}.
     * <p/>
     * Actual fixing is delegated to {@link IInvalidItemIdService#fix(FormItem)}.
     */
    private class InvalidItemIdCleanupTask
        extends AbstractBmTask<Void>
        implements ICleanUpBmObjectTask
    {

        /**
         * Form item whose identifier needs fixing.
         */
        private final FormItem itemToFix;

        /**
         * Creates new instance.
         * @param itemToFix Form item whose identifier needs fixing. Must not be {@code null}.
         */
        InvalidItemIdCleanupTask(FormItem itemToFix)
        {
            super(Messages.InvalidItemIdCleanup_Fixing_invalid_form_item_identifier);
            this.itemToFix = itemToFix;
        }

        @Override
        public Void execute(IBmTransaction transaction, IProgressMonitor progressMonitor)
        {
            service.fix(itemToFix);
            return null;
        }

    }

}
