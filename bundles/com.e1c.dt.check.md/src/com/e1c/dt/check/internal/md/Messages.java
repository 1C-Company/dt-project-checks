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

import org.eclipse.osgi.util.NLS;

/**
 * Messages.
 *
 * @author Andrey Volkov
 */
/* package */ final class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.e1c.dt.check.internal.md.messages"; //$NON-NLS-1$

    public static String ConfigurationCompatibilityModeCheck_Title;
    public static String ConfigurationCompatibilityModeCheck_Description;
    public static String ConfigurationCompatibilityModeCheck_Unsupported_compatibility_mode_min;
    public static String ConfigurationCompatibilityModeCheck_Unsupported_compatibility_mode_max__0;

    public static String ConfigurationUsedMobileApplicationFunctionalitiesCheck_Title;
    public static String ConfigurationUsedMobileApplicationFunctionalitiesCheck_Description;
    public static String ConfigurationUsedMobileApplicationFunctionalitiesCheck_Unsupported_property__0__for_current_version_of_the_platform;

    public static String ConfigurationMobileApplicationUrlsCheck_Title;
    public static String ConfigurationMobileApplicationUrlsCheck_Description;
    public static String ConfigurationMobileApplicationUrlsCheck_Empty_base_url;

    public static String MdObjectUniqueNameCheck_Title;
    public static String MdObjectUniqueNameCheck_Description;
    public static String MdObjectUniqueNameCheck__Invalid_property__0__duplicate_name__1;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
