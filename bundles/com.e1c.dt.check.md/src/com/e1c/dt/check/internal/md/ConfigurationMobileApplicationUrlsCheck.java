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
package com.e1c.dt.check.internal.md;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com._1c.g5.v8.dt.common.StringUtils;
import com._1c.g5.v8.dt.metadata.common.AbstractMobileApplicationUrl;
import com._1c.g5.v8.dt.metadata.common.ApplicationUsePurpose;
import com._1c.g5.v8.dt.metadata.common.MobileApplicationUrl;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.ObjectBelonging;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.components.TopObjectFilterExtension;
import com.e1c.g5.v8.dt.check.settings.IssueType;
import com.google.inject.Inject;

/**
 * Configuration mobile application URLs check.
 *
 * @author Andrey Volkov
 */
public final class ConfigurationMobileApplicationUrlsCheck
    extends BasicCheck
{
    @Inject
    private IRuntimeVersionSupport runtimeVersionSupport;

    /**
     * Check Id.
     */
    public static final String CHECK_ID = "configuration-mobile-application-urls"; //$NON-NLS-1$

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        builder.title(Messages.ConfigurationMobileApplicationUrlsCheck_Title)
            .description(Messages.ConfigurationMobileApplicationUrlsCheck_Description)
            .extension(new TopObjectFilterExtension())
            .issueType(IssueType.ERROR)
            .topObject(MdClassPackage.Literals.CONFIGURATION)
            .features(MdClassPackage.Literals.CONFIGURATION__MOBILE_APPLICATION_URLS);
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAceptor, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        Configuration configuration = (Configuration)object;

        if (configuration.getObjectBelonging() == ObjectBelonging.NATIVE
            && configuration.getUsePurposes().contains(ApplicationUsePurpose.MOBILE_DEVICE))
        {
            Version version = runtimeVersionSupport.getRuntimeVersion(configuration);

            if (version.isGreaterThan(Version.V8_3_17)) // since 8.3.18
            {
                checkMobileApplicationUrls(configuration, version, resultAceptor);
            }
        }
    }

    /*
     * Checks mobile application URLs (8.3.18 version)
     */
    private void checkMobileApplicationUrls(Configuration configuration, Version version, ResultAcceptor resultAceptor)
    {
        List<AbstractMobileApplicationUrl> mobileApplicationUrls = configuration.getMobileApplicationUrls();
        for (int i = 0; i < mobileApplicationUrls.size(); i++)
        {
            AbstractMobileApplicationUrl mobileApplicationUrl = mobileApplicationUrls.get(i);
            if (mobileApplicationUrl instanceof MobileApplicationUrl)
            {
                MobileApplicationUrl url = (MobileApplicationUrl)mobileApplicationUrl;
                if (StringUtils.isEmpty(url.getBaseUrl()))
                {
                    resultAceptor.addIssue(Messages.ConfigurationMobileApplicationUrlsCheck_Empty_base_url,
                        configuration, MdClassPackage.Literals.CONFIGURATION__MOBILE_APPLICATION_URLS, i);
                }
            }
        }
    }
}
