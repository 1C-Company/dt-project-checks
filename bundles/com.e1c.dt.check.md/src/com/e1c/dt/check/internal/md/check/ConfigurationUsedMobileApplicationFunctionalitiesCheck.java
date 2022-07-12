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

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;

import com._1c.g5.v8.dt.common.Functions;
import com._1c.g5.v8.dt.md.availability.MobileApplicationFunctionalitiesVersionAvailability;
import com._1c.g5.v8.dt.md.availability.RequiredMobileApplicationPermissionMessagesVersionAvailability;
import com._1c.g5.v8.dt.md.availability.RequiredMobileApplicationPermissionsVersionAvailability;
import com._1c.g5.v8.dt.metadata.common.ApplicationUsePurpose;
import com._1c.g5.v8.dt.metadata.common.CommonPackage;
import com._1c.g5.v8.dt.metadata.common.MobileApplicationFunctionalities;
import com._1c.g5.v8.dt.metadata.common.RequiredMobileApplicationPermissionMessages;
import com._1c.g5.v8.dt.metadata.common.RequiredMobileApplicationPermissions;
import com._1c.g5.v8.dt.metadata.common.RequiredPermission;
import com._1c.g5.v8.dt.metadata.common.RequiredPermissionMessage;
import com._1c.g5.v8.dt.metadata.common.UsedFunctionality;
import com._1c.g5.v8.dt.metadata.common.UsedFunctionalityFlag;
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
 * Configuration used mobile application functionalities check.
 *
 * @author Andrey Volkov
 */
public final class ConfigurationUsedMobileApplicationFunctionalitiesCheck
    extends BasicCheck
{
    @Inject
    private IRuntimeVersionSupport runtimeVersionSupport;

    /**
     * Check Id.
     */
    public static final String CHECK_ID = "configuration-used-mobile-application-functionalities"; //$NON-NLS-1$

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        builder.title(Messages.ConfigurationUsedMobileApplicationFunctionalitiesCheck_Title)
            .description(Messages.ConfigurationUsedMobileApplicationFunctionalitiesCheck_Description)
            .extension(new TopObjectFilterExtension())
            .issueType(IssueType.CRITICAL_DATA_INTEGRITY)
            .topObject(MdClassPackage.Literals.CONFIGURATION)
            .features(MdClassPackage.Literals.CONFIGURATION__REQUIRED_MOBILE_APPLICATION_PERMISSIONS,
                MdClassPackage.Literals.CONFIGURATION__REQUIRED_MOBILE_APPLICATION_PERMISSIONS,
                MdClassPackage.Literals.CONFIGURATION__USED_MOBILE_APPLICATION_FUNCTIONALITIES);
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

            if (version.isLessThan(Version.V8_3_15))
            {
                checkMobilePermissions(configuration, version, resultAceptor);
            }
            else if (version.isLessThan(Version.V8_3_18))
            {
                checkMobilePermissionsSince8315(configuration, version, resultAceptor);
            }
            else
            {
                checkUsedMobileApplicationFunctionalities(configuration, version, resultAceptor);
            }
        }
    }

    /*
     * Checks mobile permissions (pre 8.3.15 version)
     */
    private void checkMobilePermissions(Configuration configuration, Version version, ResultAcceptor resultAceptor)
    {
        EList<RequiredMobileApplicationPermissions> permissions =
            configuration.getRequiredMobileApplicationPermissions();
        for (int i = 0; i < permissions.size(); i++)
        {
            RequiredMobileApplicationPermissions permission = permissions.get(i);
            if (!RequiredMobileApplicationPermissionsVersionAvailability.isAvailable(permission, version))
            {
                String label = Functions.literalToLabel()
                    .apply(CommonPackage.Literals.REQUIRED_MOBILE_APPLICATION_PERMISSIONS
                        .getEEnumLiteral(permission.getLiteral()));

                resultAceptor.addIssue(MessageFormat.format(
                    Messages.ConfigurationUsedMobileApplicationFunctionalitiesCheck_Unsupported_property__0,
                    label), configuration,
                    MdClassPackage.Literals.CONFIGURATION__REQUIRED_MOBILE_APPLICATION_PERMISSIONS, i);
            }
        }
    }

    /*
     * Checks mobile permissions (8.3.15 version)
     */
    private void checkMobilePermissionsSince8315(Configuration configuration, Version version,
        ResultAcceptor resultAceptor)
    {
        EList<RequiredPermission> permissions = configuration.getRequiredMobileApplicationPermissions8315();
        for (int i = 0; i < permissions.size(); i++)
        {
            RequiredPermission permission = permissions.get(i);
            if (!RequiredMobileApplicationPermissionsVersionAvailability.isAvailable(permission.getPermission(),
                version))
            {
                String label = Functions.literalToLabel()
                    .apply(CommonPackage.Literals.REQUIRED_MOBILE_APPLICATION_PERMISSIONS
                        .getEEnumLiteral(permission.getPermission().getLiteral()));

                resultAceptor.addIssue(MessageFormat.format(
                    Messages.ConfigurationUsedMobileApplicationFunctionalitiesCheck_Unsupported_property__0,
                    label), configuration,
                    MdClassPackage.Literals.CONFIGURATION__REQUIRED_MOBILE_APPLICATION_PERMISSIONS8315, i);
            }
        }
    }

    /*
     * Checks used mobile application functionalities (8.3.18 version)
     */
    private void checkUsedMobileApplicationFunctionalities(Configuration configuration, Version version,
        ResultAcceptor resultAceptor)
    {
        UsedFunctionality usedFunctionality = configuration.getUsedMobileApplicationFunctionalities();

        if (usedFunctionality != null)
        {
            // functionalities
            List<UsedFunctionalityFlag> flags = usedFunctionality.getFunctionality();
            for (int i = 0; i < flags.size(); i++)
            {
                UsedFunctionalityFlag flag = flags.get(i);
                MobileApplicationFunctionalities functionality = flag.getFunctionality();
                if (!MobileApplicationFunctionalitiesVersionAvailability.isAvailable(functionality, version))
                {
                    String label = Functions.literalToLabel()
                        .apply(CommonPackage.Literals.MOBILE_APPLICATION_FUNCTIONALITIES
                            .getEEnumLiteral(functionality.getLiteral()));

                    resultAceptor.addIssue(MessageFormat.format(
                        Messages.ConfigurationUsedMobileApplicationFunctionalitiesCheck_Unsupported_property__0,
                        label), configuration,
                        MdClassPackage.Literals.CONFIGURATION__USED_MOBILE_APPLICATION_FUNCTIONALITIES, i);
                }
            }

            // messages
            List<RequiredPermissionMessage> messages = usedFunctionality.getPermissionMessage();
            for (int i = 0; i < messages.size(); i++)
            {
                RequiredPermissionMessage message = messages.get(i);
                RequiredMobileApplicationPermissionMessages permission = message.getPermission();
                if (!RequiredMobileApplicationPermissionMessagesVersionAvailability.isAvailable(permission, version))
                {
                    String label = Functions.literalToLabel()
                        .apply(CommonPackage.Literals.REQUIRED_MOBILE_APPLICATION_PERMISSION_MESSAGES
                            .getEEnumLiteral(permission.getLiteral()));

                    resultAceptor.addIssue(MessageFormat.format(
                        Messages.ConfigurationUsedMobileApplicationFunctionalitiesCheck_Unsupported_property__0,
                        label), configuration,
                        MdClassPackage.Literals.CONFIGURATION__USED_MOBILE_APPLICATION_FUNCTIONALITIES, i);
                }
            }
        }
    }
}
