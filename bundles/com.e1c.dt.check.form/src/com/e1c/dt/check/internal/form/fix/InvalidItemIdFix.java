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
package com.e1c.dt.check.internal.form.fix;

import java.text.MessageFormat;
import java.util.Optional;

import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.form.model.AutoCommandBar;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.service.FormIdentifierService;
import com.e1c.dt.check.form.InvalidItemIdCheck;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.g5.v8.dt.check.qfix.IFixSession;
import com.e1c.g5.v8.dt.check.qfix.components.BasicModelFixContext;
import com.e1c.g5.v8.dt.check.qfix.components.QuickFix;
import com.e1c.g5.v8.dt.check.qfix.components.SingleVariantModelBasicFix;
import com.google.inject.Inject;

/**
 * Fixes issues detected by {@link InvalidItemIdCheck}.
 *
 * Expects to get {@link FormItem} with a broken {@link com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM__ID}
 * as the target object. The fix does not try to understand why identifier is not good enough.
 * it just replaces it with a new value.
 * {@link FormIdentifierService} is used to determine what this new value should be.
 * However, there are exclusions. For example, command bar of a form will get predefined value of {@code -1}.
 *
 * @author Nikolay Martynov
 */
@QuickFix(checkId = InvalidItemIdCheck.CHECK_ID, supplierId = CorePlugin.PLUGIN_ID)
public class InvalidItemIdFix
    extends SingleVariantModelBasicFix<FormItem>
{

    /**
     * Service that is used to generate new identifiers for broken form items.
     */
    @Inject
    private FormIdentifierService formIdentifierService;

    @Override
    protected void configureFix(FixConfigurer configurer)
    {
        super.configureFix(configurer);
        configurer.description(Messages.InvalidItemIdFix_AssignNewIdentifierValue);
    }

    @Override
    protected void applyChanges(FormItem modelObject, EStructuralFeature targetFeature, BasicModelFixContext context,
        IFixSession session)
    {
        CorePlugin.trace(InvalidItemIdCheck.DEBUG_OPTION,
            "Fix: Applying fix: object={0}, feature={1}, context={2}, session={3}", //$NON-NLS-1$
            modelObject, targetFeature, context, session);
        boolean wasSet = modelObject.eIsSet(targetFeature);
        Object oldValue = modelObject.eGet(targetFeature);
        Optional<Integer> newValue = calculateNewIdFor(modelObject);
        if (newValue.isEmpty())
        {
            CorePlugin.trace(InvalidItemIdCheck.DEBUG_OPTION,
                "Fix: To avoid data corruption, will not change identifier of {0} because unable to determine proper new value", //$NON-NLS-1$
                modelObject);
            return;
        }
        modelObject.setId(newValue.get());
        CorePlugin.trace(InvalidItemIdCheck.DEBUG_OPTION,
            "Fix: Replaced identifier: newValue={0}, wasSet={1}, oldvalue={2}, object={3}", //$NON-NLS-1$
            newValue.get(), wasSet, oldValue, modelObject);
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
        if (formIdentifierService == null)
        {
            throw new IllegalStateException(MessageFormat.format(Messages.InvalidItemIdFix_ServiceNotAvailable,
                FormIdentifierService.class.getName()));
        }
        boolean commandBarOfForm = item instanceof AutoCommandBar && item.eContainer() instanceof Form;
        if (commandBarOfForm)
        {
            CorePlugin.trace(InvalidItemIdCheck.DEBUG_OPTION,
                "Fix: This seem as command bar of a form so will use -1 for identifier: item={0}, container={1}", item, //$NON-NLS-1$
                item.eContainer());
            return Optional.of(-1);
        }
        IBmObject topObject = item.bmGetTopObject();
        if (!(topObject instanceof Form))
        {
            CorePlugin.trace(InvalidItemIdCheck.DEBUG_OPTION,
                "Fix: Unable to determine enclosing form: item={0}, topObject={1}", item, //$NON-NLS-1$
                item.bmGetTopObject());
            return Optional.empty();
        }
        Form form = (Form)item.bmGetTopObject();
        int newValue = formIdentifierService.getNextItemId(form);
        CorePlugin.trace(InvalidItemIdCheck.DEBUG_OPTION,
            "Fix: Form identifier service says we should use {0} as next identifier value for form {1}", newValue, //$NON-NLS-1$
            form);
        return Optional.of(newValue);
    }

}
