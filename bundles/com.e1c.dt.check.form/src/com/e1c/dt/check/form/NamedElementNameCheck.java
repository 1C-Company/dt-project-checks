/*******************************************************************************
 * Copyright (C) 2021, 1C-Soft LLC and others.
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
package com.e1c.dt.check.form;

import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.ABSTRACT_FORM_ATTRIBUTE;
import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM;
import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_COMMAND;
import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM;
import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_PARAMETER;
import static com._1c.g5.v8.dt.mcore.McorePackage.Literals.NAMED_ELEMENT__NAME;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EClass;

import com._1c.g5.v8.dt.common.StringUtils;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.mcore.NamedElement;
import com.e1c.g5.v8.dt.check.CheckComplexity;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.e1c.g5.v8.dt.check.settings.IssueType;

/**
 * Checks that each form named element has name and it is valid.
 *
 * @author Dmitriy Marmyshev
 */
public class NamedElementNameCheck
    extends BasicCheck
{
    private static final String CHECK_ID = "form-named-element-name"; //$NON-NLS-1$
    private static final Set<EClass> EXCLUDED_CLASSES = Set.of(FormPackage.Literals.AUTO_COMMAND_BAR);

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        builder.extension(new SkipBaseFormExtension())
            .title(Messages.NamedElementNameCheck_title)
            .description(Messages.NamedElementNameCheck_description)
            .complexity(CheckComplexity.NORMAL)
            .severity(IssueSeverity.MAJOR)
            .issueType(IssueType.ERROR);
        builder.topObject(FORM).containment(ABSTRACT_FORM_ATTRIBUTE).features(NAMED_ELEMENT__NAME);
        builder.topObject(FORM).containment(FORM_COMMAND).features(NAMED_ELEMENT__NAME);
        builder.topObject(FORM).containment(FORM_PARAMETER).features(NAMED_ELEMENT__NAME);
        builder.topObject(FORM).containment(FORM_ITEM).features(NAMED_ELEMENT__NAME);
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAceptor, ICheckParameters parameters,
        IProgressMonitor monitor)
    {
        NamedElement named = (NamedElement)object;
        String name = named.getName();
        if (StringUtils.isEmpty(name))
        {
            if (!EXCLUDED_CLASSES.contains(named.eClass()))
            {
                resultAceptor.addIssue(Messages.NamedElementNameCheck_Form_named_element_name_is_empty,
                    NAMED_ELEMENT__NAME);
            }
        }
        else if (!StringUtils.isValidName(name))
        {
            String message = MessageFormat
                .format(Messages.NamedElementNameCheck_Form_named_element_name__N__is_not_valid_name, name);
            resultAceptor.addIssue(message, NAMED_ELEMENT__NAME);
        }
    }
}
