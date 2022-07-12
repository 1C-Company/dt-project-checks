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
package com.e1c.dt.check.internal.md.fix;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.dt.scc.model.StandaloneContentMdObjectAware;
import com.e1c.dt.check.internal.md.CorePlugin;
import com.e1c.dt.check.internal.md.check.ConfigurationStandaloneContentCheck;
import com.e1c.g5.v8.dt.check.qfix.IFixSession;
import com.e1c.g5.v8.dt.check.qfix.components.BasicModelFixContext;
import com.e1c.g5.v8.dt.check.qfix.components.MultiVariantModelBasicFix;
import com.e1c.g5.v8.dt.check.qfix.components.QuickFix;

/**
 * The multi-variant fix for {@link ConfigurationStandaloneContentCheck} allows to remove bad content item
 * from used, unused and priority items.
 *
 * @author Andrey Volkov
 */
@QuickFix(checkId = "configuration-standalone-content", supplierId = CorePlugin.PLUGIN_ID)
public class ConfigurationStandaloneContentFix
    extends MultiVariantModelBasicFix
{

    @Override
    protected void buildVariants()
    {
        VariantBuilder builder = VariantBuilder.create(this);
        builder
            .description(Messages.ConfigurationStandaloneContentFix_Remove_bad_content_item_title,
                Messages.ConfigurationStandaloneContentFix_Remove_bad_content_item_description)
            .change(this::removeBadContentItem)
            .build();
    }

    private void removeBadContentItem(EObject object, EStructuralFeature feature, BasicModelFixContext context,
        IFixSession session)
    {
        if (object instanceof StandaloneContentMdObjectAware)
        {
            EObject parent = object.eContainer();
            EStructuralFeature containing = object.eContainmentFeature();
            if (containing == null || parent == null)
            {
                return;
            }

            List<?> list = (List<?>)parent.eGet(containing);
            int index = list.indexOf(object);
            if (index > -1)
            {
                list.remove(index);
            }
        }
    }
}
