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

import org.eclipse.emf.ecore.EStructuralFeature;

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
 * <p/>
 * When fixing, it does not try to understand why identifier is not good enough.
 * It just replaces it with a new value.
 * {@link FormIdentifierService#calculateNewIdFor(FormItem)} is used to determine
 * what this new value should be.
 * <p/>
 * Expects to get {@link FormItem} with a broken {@link com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM__ID}
 * as the target object.
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
    private final FormIdentifierService formIdentifierService;

    /**
     * Creates new instnce.
     *
     * @param formIdentifierService Service to be used to generate
     * new identifiers when fixing broken form items. Must not be {@code null}.
     */
    @Inject
    public InvalidItemIdFix(FormIdentifierService formIdentifierService)
    {
        this.formIdentifierService = formIdentifierService;
    }

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
        formIdentifierService.calculateNewIdFor(modelObject).ifPresent(modelObject::setId);
    }

}
