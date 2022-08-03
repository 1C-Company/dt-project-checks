/**
 * Copyright (C) 2022, 1C
 */
package com.e1c.dt.check.internal.md.fix;

import org.eclipse.osgi.util.NLS;

/**
 * @author alexandr
 *
 */
public class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = "com.e1c.dt.check.internal.md.messages"; //$NON-NLS-1$

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
