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
package com.e1c.dt.check.internal.md.check;

import org.eclipse.core.runtime.IProgressMonitor;

import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;
import com._1c.g5.v8.dt.scc.ConfigurationStandaloneContentValidator;
import com._1c.g5.v8.dt.scc.model.StandaloneContent;
import com._1c.g5.v8.dt.scc.model.StandaloneContentPackage;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.components.TopObjectFilterExtension;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.google.inject.Inject;

/**
 * Configuration standalone content check.
 *
 * @author Andrey Volkov
 */
public class ConfigurationStandaloneContentCheck
    extends BasicCheck
{
    @Inject
    private IRuntimeVersionSupport runtimeVersionSupport;

    /**
     * Check Id.
     */
    public static final String CHECK_ID = "configuration-standalone-content"; //$NON-NLS-1$

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        builder.title(Messages.ConfigurationStandaloneContentCheck_Title)
            .description(Messages.ConfigurationStandaloneContentCheck_Description)
            .extension(new TopObjectFilterExtension())
            .severity(IssueSeverity.CRITICAL)
            .topObject(StandaloneContentPackage.Literals.STANDALONE_CONTENT)
            .features(StandaloneContentPackage.Literals.STANDALONE_CONTENT__PRIORITY_ITEM,
                StandaloneContentPackage.Literals.STANDALONE_CONTENT__UNUSED_ITEM,
                StandaloneContentPackage.Literals.STANDALONE_CONTENT__USED_ITEM);
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAceptor, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        StandaloneContent standaloneContent = (StandaloneContent)object;
        Version version = runtimeVersionSupport.getRuntimeVersion(standaloneContent);

        ConfigurationStandaloneContentValidator.validate(standaloneContent, version)
            .entrySet()
            .forEach(
                error -> error.getValue()
                    .forEach(errorMessage -> resultAceptor.addIssue(errorMessage, error.getKey())));
    }
}
