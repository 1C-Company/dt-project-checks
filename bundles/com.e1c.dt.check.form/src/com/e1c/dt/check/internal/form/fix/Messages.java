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

import org.eclipse.osgi.util.NLS;

/**
 * @author Dmitriy Marmyshev
 *
 */
final class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.e1c.dt.check.internal.form.fix.messages"; //$NON-NLS-1$
    public static String DataPathRemoveFix_Remove_data_path_description;
    public static String DataPathRemoveFix_Remove_data_path_title;
    public static String DataPathRemoveFix_Remove_form_item_description;
    public static String DataPathRemoveFix_Remove_form_item_title;
    public static String InvalidItemIdFix_AssignNewIdentifierValue;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
