/**
 * Copyright (C) 2022, 1C
 */
package com.e1c.dt.check.form;

import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM;
import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM__ID;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.service.item.FormItemIterator;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.g5.v8.dt.check.CheckComplexity;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.e1c.g5.v8.dt.check.settings.IssueType;

/**
 * Checks that form items have valid identifiers.
 *
 * {@link com._1c.g5.v8.dt.form.model.FormItem#getId()} is valid if it has a unique value
 * accross all other items on this form.
 *
 * Additionally, an identifier is considered to be invalid if its value is {@code 0}
 * which is a default value for {@link org.eclipse.emf.ecore.EObject#eGet(EStructuralFeature, boolean)}
 * in case of {@link java.langInteger} feature.
 *
 * Negative values are not considered to be invalid. That is because such values are perfectly valid
 * at least for some of the cases. For example, {@link com._1c.g5.v8.dt.form.model.AutoCommandBar}
 * might have {@link -1} as in identifier. This can be seen in the implementation of
 * {@code com._1c.g5.v8.dt.internal.form.generator.FormGeneratorCore}.
 *
 * Only {@link FormItem}`s are checked. {@link com._1c.g5.v8.dt.form.model.FormAttribute}
 * and other child content is ignored.
 *
 * @author Nikolay Martynov
 *
 */
public class InvalidItemIdCheck
    extends BasicCheck
{

    private static final String CHECK_ID = "form-invalid-item-id"; //$NON-NLS-1$
    private static final String DEBUG_OPTION = "/debug/InvalidItemIdCheck"; //$NON-NLS-1$

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer configurationBuilder)
    {
        // TODO: Verify which common extensions should be used.
        configurationBuilder.extension(new SkipBaseFormExtension())
            .title(Messages.InvalidItemIdCheck_title)
            .description(Messages.InvalidItemIdCheck_description)
            .complexity(CheckComplexity.NORMAL)
            .severity(IssueSeverity.MAJOR)
            .issueType(IssueType.ERROR)
            .topObject(FORM)
            .checkTop();
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAcceptor, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        if (!(object instanceof Form))
        {
            CorePlugin.trace(DEBUG_OPTION, "Received something that is not a Form: class={0}", object.getClass()); //$NON-NLS-1$
            return;
        }
        Form form = (Form)object;
        CorePlugin.trace(DEBUG_OPTION, "Checking form: {0}", form); //$NON-NLS-1$
        if (CorePlugin.getDefault().isDebugging())
        {
            // We're not interested in attributes because their identifier does not have effect on anything.
            form.getAttributes()
                .forEach(attribute -> CorePlugin.trace(DEBUG_OPTION, "Got form attribute: id={0}, class={1}", //$NON-NLS-1$
                    attribute.getId(), attribute.getClass()));
        }
        Set<Integer> seenIdentifiers = new HashSet<>();
        for (FormItemIterator iterator = new FormItemIterator(form); iterator.hasNext();)
        {
            if (progressMonitor.isCanceled())
            {
                CorePlugin.trace(DEBUG_OPTION, "Check has been cancelled"); //$NON-NLS-1$
                return;
            }
            FormItem item = iterator.next();
            CorePlugin.trace(DEBUG_OPTION, "Got form item: id={0}, class={1}", item.getId(), item.getClass()); //$NON-NLS-1$
            if (hasValidId(item))
            {
                boolean isUniqueId = seenIdentifiers.add(item.getId());
                if (!isUniqueId)
                {
                    CorePlugin.trace(DEBUG_OPTION, "Form item has a duplicate identifier: id={0}, item={1}, seen={2}", //$NON-NLS-1$
                        item.getId(), item, seenIdentifiers);
                    resultAcceptor.addIssue(
                        MessageFormat.format(Messages.InvalidItemIdCheck_DuplicateValueOfIdAttribute, item.getId()),
                        item, FORM_ITEM__ID);
                }
            }
            else
            {
                CorePlugin.trace(DEBUG_OPTION, "Form item has an invalid identifier: id={0}, item={1}", item.getId(), //$NON-NLS-1$
                    item);
                resultAcceptor.addIssue(Messages.InvalidItemIdCheck_InvalidValueOfIdAttribute, item, FORM_ITEM__ID);
            }
        }
    }

    /**
     * Checks whether specified form item has a valid identifier.
     *
     * @param item Form item whose identifier is to be checked. Must not be null.
     * @return {@code true} if specified item has a valid identifier.
     */
    private boolean hasValidId(FormItem item)
    {
        return item.getId() != 0;
    }

}
