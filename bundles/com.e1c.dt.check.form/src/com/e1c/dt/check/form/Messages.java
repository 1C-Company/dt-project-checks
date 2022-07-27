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

import org.eclipse.osgi.util.NLS;

/**
 * @author Dmitriy Marmyshev
 *
 */
final class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.e1c.dt.check.form.messages"; //$NON-NLS-1$
    public static String DataPathReferredObjectCheck_Data_path;
    public static String DataPathReferredObjectCheck_description;
    public static String DataPathReferredObjectCheck_message;
    public static String DataPathReferredObjectCheck_title;
    public static String NamedElementNameCheck_description;
    public static String NamedElementNameCheck_Form_named_element_name__N__is_not_valid_name;
    public static String NamedElementNameCheck_Form_named_element_name_is_empty;
    public static String NamedElementNameCheck_title;
    public static String InvalidItemIdCheck_description;
    public static String InvalidItemIdCheck_DuplicateValueOfIdAttribute;
    public static String InvalidItemIdCheck_InvalidValueOfIdAttribute;
    public static String InvalidItemIdCheck_title;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
