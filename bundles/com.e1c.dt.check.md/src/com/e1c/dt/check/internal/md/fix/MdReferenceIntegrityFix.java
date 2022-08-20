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
 *     1C-Soft LLC - initial implementation
 *******************************************************************************/
package com.e1c.dt.check.internal.md.fix;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.e1c.dt.check.internal.md.CorePlugin;
import com.e1c.g5.v8.dt.check.qfix.IFixSession;
import com.e1c.g5.v8.dt.check.qfix.components.BasicModelFixContext;
import com.e1c.g5.v8.dt.check.qfix.components.MultiVariantModelBasicFix;
import com.e1c.g5.v8.dt.check.qfix.components.QuickFix;

/**
 * @author Alexandr Sanarov
 *
 */
@QuickFix(checkId = "md-reference-intergrity", supplierId = CorePlugin.PLUGIN_ID)
public class MdReferenceIntegrityFix
    extends MultiVariantModelBasicFix
{

    private void removeProxyReferences(EObject object, EStructuralFeature feature, BasicModelFixContext context,
        IFixSession session)
    {
        if (feature.isMany())
        {
            @SuppressWarnings("unchecked")
            EList<? extends EObject> objects = (EList<? extends EObject>)object.eGet(feature);
            if (objects != null)
            {
                objects.removeIf(proxyObject -> proxyObject.eIsProxy());
            }
        }
        else
        {
            EObject value = (EObject)object.eGet(feature);
            if (value != null && value.eIsProxy())
            {
                EcoreUtil.delete(value);
            }
        }

    }

    @Override
    protected void buildVariants()
    {
        VariantBuilder builder = VariantBuilder.create(this);
        builder
            .description(Messages.MdReferenceIntegrity_FixTitle, Messages.MdReferenceIntegrity_FixDescription)
            .change(this::removeProxyReferences)
            .build();

    }

}
