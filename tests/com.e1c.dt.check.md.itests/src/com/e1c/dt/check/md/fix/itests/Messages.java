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
package com.e1c.dt.check.md.fix.itests;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 *
 * @author Alexandr Sanarov
 *
 */
final class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.e1c.dt.check.internal.md.fix.messages"; //$NON-NLS-1$

    public static String MdReferenceIntegrity_FixDescription;
    public static String MdReferenceIntegrity_FixTitle;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
