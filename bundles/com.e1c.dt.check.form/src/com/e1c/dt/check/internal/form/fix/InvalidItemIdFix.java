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
import com.e1c.dt.check.form.InvalidItemIdCheck;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.dt.check.internal.form.IInvalidItemIdService;
import com.e1c.g5.v8.dt.check.qfix.IFixSession;
import com.e1c.g5.v8.dt.check.qfix.components.BasicModelFixContext;
import com.e1c.g5.v8.dt.check.qfix.components.QuickFix;
import com.e1c.g5.v8.dt.check.qfix.components.SingleVariantModelBasicFix;
import com.google.inject.Inject;

/**
 * Fixes issues detected by {@link InvalidItemIdCheck}.
 * <p/>
 * This implementation delegates actual fixing to {@link IInvalidItemIdService}.
 * This class contains just a wiring necessary for {@link com.e1c.g5.v8.dt.check.qfix.IFix}.
 * <p/>
 * Expects to get {@link FormItem} with a broken {@link com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM__ID}
 * as the target object.
 *
 * @author Nikolay Martynov
 */
@QuickFix(checkId = IInvalidItemIdService.CHECK_ID, supplierId = CorePlugin.PLUGIN_ID)
public class InvalidItemIdFix
    extends SingleVariantModelBasicFix<FormItem>
{

    /**
     * Service to delegate fixing to.
     */
    @Inject
    private IInvalidItemIdService service;

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
        service.fix(modelObject);
    }

}
