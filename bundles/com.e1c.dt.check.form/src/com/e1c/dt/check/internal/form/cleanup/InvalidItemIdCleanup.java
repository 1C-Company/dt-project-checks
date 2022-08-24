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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.model.EditingMode;
import com._1c.g5.v8.dt.core.model.IModelEditingSupport;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.AutoCommandBar;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.form.service.FormIdentifierService;
import com._1c.g5.v8.dt.form.service.item.FormItemIterator;
import com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectObjectTasksProvider;
import com.google.inject.Inject;

/**
 * Cleans up invalid identifiers of form items.
 * <p/>
 * <ul>
 * <li>If BM instance can be obtained then non-lightweight read-only transaction is used to list forms
 * (via {@link IBmTransaction#getTopObjectIterator(EClass)}) and</li>
 * <li>for each form it is checked if it can be edited. Ineditable froms are omitted from validation and cleanup.</li>
 * <li>Quick form checking is performed to skip forms without issues.</li>
 * <li>For each form with issues, a separate cleanup task will be returned for further management by
 * {@code com._1c.g5.v8.dt.internal.migration.cleanup.CleanUpProjectSourcesManager}.</li>
 * <li>After analsis of the form, it will be evicted from the transaction to conserve memory</li>
 * <li>Form cleanup tasks will obtain corresponding form (by its identifier) from the transaction,
 * perform full validation and fix each malformed form item identifier.</li>
 * </ul>
 * Both scanning (before quick-checking each form) and
 * fixing (before loading form to do full validation and before fixing each form item)
 * will be stopped as soon as cancellation is reported.
 * <p/>
 * Checking.
 * <ul>
 * <li>Each {@link FormItem} on a form is checked regardless of how deep it is nested.</li>
 * <li>{@link FormItem#getId()} is considered to be invalid if its value is {@code 0}
 * which is a default value for {@link org.eclipse.emf.ecore.EObject#eGet(EStructuralFeature, boolean)}
 * in case of {@link java.lang.Integer} feature.
 * <li>Negative values are not considered to be invalid. That is because such values are perfectly valid
 * at least for some of the cases. For example, {@link AutoCommandBar}
 * might have {@code -1} as in identifier. This can be seen in the implementation of
 * {@code com._1c.g5.v8.dt.internal.form.generator.FormGeneratorCore}.</li>
 * <li>Additionally, form items have to have a unique identifier value
 * accross all other items on this {@link Form} regardless of how they are nested into each other.
 * When there are multiple form items with the same identifier then one of
 * them is considered to be valid while others are deemed to be problematic duplicates.</li>
 * <li>Only {@link FormItem}`s are checked. {@link com._1c.g5.v8.dt.form.model.FormAttribute}
 * and other child content is ignored.</li>
 * </ul>
 * Fixing.
 * <ul>
 * <li>Fixing {@link FormItem} means replacing its identifier with a new value.</li>
 * <li>{@link FormIdentifierService} is used to determine what this new value should be.
 * However, there are exclusions. For example, {@link AutoCommandBar}
 * of a form will get predefined value of {@code -1} while {@link AutoCommandBar} of any
 * other item will get identifer from {@link FormIdentifierService}.</li>
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
     * Service that is used to generate new identifiers for broken form items.
     */
    private final FormIdentifierService formIdentifierService;

    /**
     * Creates new instance.
     *
     * @param bmModelManager Service instance that is to be used to obtain project model.
     * Must not be {@code null}.
     * @param editingSupport Service instance that is to be used to check if we're allowed to change forms.
     * Must not be {@code null}.
     * @param formIdentifierService Service to be used to generate
     * new identifiers when fixing broken form items. Must not be {@code null}.
     */
    @Inject
    public InvalidItemIdCleanup(IBmModelManager bmModelManager, IModelEditingSupport editingSupport,
        FormIdentifierService formIdentifierService)
    {
        this.bmModelManager = bmModelManager;
        this.editingSupport = editingSupport;
        this.formIdentifierService = formIdentifierService;
    }

    @Override
    public List<ICleanUpBmObjectTask> getCleanUpProjectTasks(IDtProject project)
    {
        IBmModel model = bmModelManager.getModel(project);
        if (model == null)
        {
            return Collections.emptyList();
        }
        return model.executeReadonlyTask(new CollectCleanupTasksTask(), false);
    }

    /**
     * Checks whether specified form item has a valid identifier.
     *
     * @param item Form item whose identifier is to be checked. Must not be {@code null}.
     * @return {@code true} if specified item has a valid identifier.
     */
    private boolean hasValidId(FormItem item)
    {
        return item.getId() != 0;
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
            Iterator<IBmObject> formIterator = transaction.getTopObjectIterator(FormPackage.Literals.FORM);
            List<ICleanUpBmObjectTask> fixTasks = new ArrayList<>();
            while (formIterator.hasNext() && !progressMonitor.isCanceled())
            {
                Form form = (Form)formIterator.next();
                if (editingSupport.canEdit(form, EditingMode.DIRECT) && !isValid(form))
                {
                    fixTasks.add(new InvalidFormCleanupTask(form.bmGetId()));
                }
                transaction.evict(form.bmGetId());
            }
            return fixTasks;
        }

        /**
         * Checks if there are issues with the specified form.
         *
         * This is a short-circuit operation
         * that should return as soon as it is obvious that there are issues.
         *
         * @param form Form to validate. Must not be {@code null}.
         * @return {@code true} if there are issues to be fixed.
         */
        private boolean isValid(Form form)
        {
            Set<Integer> seenIdentifiers = new HashSet<>();
            FormItemIterator itemIterator = new FormItemIterator(form);
            while (itemIterator.hasNext())
            {
                FormItem item = itemIterator.next();
                if (!hasValidId(item) || !seenIdentifiers.add(item.getId()))
                {
                    return false;
                }
            }
            return true;
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
            for (FormItem itemToFix : validate(form))
            {
                if (progressMonitor.isCanceled())
                {
                    return null;
                }
                fix(itemToFix);
            }
            return null;
        }

        /**
         * Calculates new identifier for the specified form item.
         *
         * Some form items have special rules for identifiers.
         * For example, {@code com._1c.g5.v8.dt.internal.form.generator.FormGeneratorCore}
         * creates new forms with {@link com._1c.g5.v8.dt.form.model.AutoCommandBar}
         * that has identifier of {@code -1}.
         * We try to do the same here.
         *
         * @param item Form item that needs new identifier. Most not be {@code null}.
         * @return New value of identifier for the specified item or an empty holder if
         * unable to determine proper new identifier value.
         */
        private Optional<Integer> calculateNewIdFor(FormItem item)
        {
            boolean commandBarOfForm = item instanceof AutoCommandBar && item.eContainer() instanceof Form;
            if (commandBarOfForm)
            {
                return Optional.of(-1);
            }
            IBmObject topObject = item.bmGetTopObject();
            return topObject instanceof Form ? Optional.of(formIdentifierService.getNextItemId((Form)topObject))
                : Optional.empty();
        }

        /**
         * Validates specified form.
         *
         * @param form Form to validate. May be {@code null}.
         * @return Collection of form items that have issues or an empty collection if {@code form} is {@code null}
         * or there are no issue with any of the {@link FormItem}s on the specified form.
         * The result must never be {@code null}.
         */
        private Collection<FormItem> validate(Form form)
        {
            if (form == null)
            {
                return Collections.emptyList();
            }
            Collection<FormItem> itemsWithIssues = new ArrayList<>();
            Set<Integer> seenIdentifiers = new HashSet<>();
            FormItemIterator itemIterator = new FormItemIterator(form);
            while (itemIterator.hasNext())
            {
                FormItem item = itemIterator.next();
                if (!hasValidId(item) || !seenIdentifiers.add(item.getId()))
                {
                    itemsWithIssues.add(item);
                }
            }
            return itemsWithIssues;
        }

        /**
         * Fixes specified form item.
         *
         * @param formItemToFix A particular form item with an issue
         * (one of the keys in result returned by {@link #validate(Form)})
         * that is to be fixed. Must not be {@code null}.
         */
        private void fix(FormItem item)
        {
            calculateNewIdFor(item).ifPresent(item::setId);
        }

    }

}
