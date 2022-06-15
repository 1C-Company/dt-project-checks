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

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.dt.form.model.AbstractDataPath;
import com._1c.g5.v8.dt.form.model.ExtInfo;
import com._1c.g5.v8.dt.form.model.FormItem;
import com.e1c.dt.check.form.DataPathReferredObjectCheck;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.g5.v8.dt.check.qfix.IFixSession;
import com.e1c.g5.v8.dt.check.qfix.components.BasicModelFixContext;
import com.e1c.g5.v8.dt.check.qfix.components.MultiVariantModelBasicFix;
import com.e1c.g5.v8.dt.check.qfix.components.QuickFix;

/**
 * The multi-variant fix for {@link DataPathReferredObjectCheck} allows to remove bad data path
 * and remove form item with this data path.
 */
@QuickFix(checkId = "form-data-path", supplierId = CorePlugin.PLUGIN_ID)
public class DataPathRemoveFix
    extends MultiVariantModelBasicFix
{

    @Override
    protected void buildVariants()
    {
        VariantBuilder builder = VariantBuilder.create(this);

        builder
            .description(Messages.DataPathRemoveFix_Remove_data_path_title,
                Messages.DataPathRemoveFix_Remove_data_path_description)
            .change(this::removeDataPath)
            .build();

        builder
            .description(Messages.DataPathRemoveFix_Remove_form_item_title,
                Messages.DataPathRemoveFix_Remove_form_item_description)
            .change(this::removeFormItem)
            .build();
    }

    private void removeDataPath(EObject object, EStructuralFeature feature, BasicModelFixContext context,
        IFixSession session)
    {
        if (object instanceof AbstractDataPath)
        {
            removeObject(object);
        }
    }

    private void removeFormItem(EObject object, EStructuralFeature feature, BasicModelFixContext context,
        IFixSession session)
    {
        EObject item = object.eContainer();
        if (item instanceof ExtInfo)
        {
            item = item.eContainer();
        }

        if (item instanceof FormItem)
        {
            removeObject(item);
        }
    }

    private void removeObject(EObject object)
    {
        EObject parent = object.eContainer();
        EStructuralFeature containing = object.eContainmentFeature();
        if (containing == null || parent == null)
        {
            return;
        }

        if (containing.isMany())
        {
            List<?> list = (List<?>)parent.eGet(containing);
            int index = list.indexOf(object);
            if (index > -1)
            {
                list.remove(index);
            }
        }
        else
        {
            parent.eUnset(containing);
        }
    }

}
