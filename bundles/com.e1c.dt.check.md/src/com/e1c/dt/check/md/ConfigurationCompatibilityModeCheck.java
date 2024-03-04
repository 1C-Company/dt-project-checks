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
package com.e1c.dt.check.md;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import com._1c.g5.v8.dt.metadata.mdclass.CompatibilityMode;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.ObjectBelonging;
import com._1c.g5.v8.dt.metadata.mdclass.util.MdClassUtil;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.components.TopObjectFilterExtension;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.e1c.g5.v8.dt.check.settings.IssueType;
import com.google.inject.Inject;

/**
 * Configuration сompatibility mode check.
 *
 * @author Andrey Volkov
 */
public final class ConfigurationCompatibilityModeCheck
    extends BasicCheck
{
    @Inject
    private IRuntimeVersionSupport runtimeVersionSupport;

    /**
     * Check Id.
     */
    public static final String CHECK_ID = "configuration-compatibility-mode"; //$NON-NLS-1$

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        builder.title(Messages.ConfigurationCompatibilityModeCheck_Title)
            .description(Messages.ConfigurationCompatibilityModeCheck_Description)
            .extension(new TopObjectFilterExtension())
            .issueType(IssueType.CRITICAL_DATA_INTEGRITY)
            .severity(IssueSeverity.CRITICAL)
            .criticalDataIntegrityCheck()
            .topObject(MdClassPackage.Literals.CONFIGURATION)
            .features(MdClassPackage.Literals.CONFIGURATION__COMPATIBILITY_MODE);
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAceptor, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        Configuration configuration = (Configuration)object;

        if (configuration.getObjectBelonging() == ObjectBelonging.NATIVE)
        {
            Version version = runtimeVersionSupport.getRuntimeVersion(configuration);
            CompatibilityMode maxCompatibilityMode =
                MdClassUtil.getCompatibilityMode(version.getMajor(), version.getMinor(), version.getMicro());
            CompatibilityMode currentCompatibilityMode = configuration.getCompatibilityMode();

            if (currentCompatibilityMode.getValue() < CompatibilityMode.VERSION8_38_VALUE)
            {
                resultAceptor.addIssue(Messages.ConfigurationCompatibilityModeCheck_Unsupported_compatibility_mode_min,
                    configuration, MdClassPackage.Literals.CONFIGURATION__COMPATIBILITY_MODE);
            }

            if (currentCompatibilityMode.getValue() > maxCompatibilityMode.getValue())
            {
                resultAceptor.addIssue(MessageFormat.format(
                    Messages.ConfigurationCompatibilityModeCheck_Unsupported_compatibility_mode_max__0,
                    version.toString()), configuration, MdClassPackage.Literals.CONFIGURATION__COMPATIBILITY_MODE);
            }
        }
    }
}
