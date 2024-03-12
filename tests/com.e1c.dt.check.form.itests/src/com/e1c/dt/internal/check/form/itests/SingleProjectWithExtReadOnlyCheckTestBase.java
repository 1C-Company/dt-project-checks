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
package com.e1c.dt.internal.check.form.itests;

import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;

import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.platform.version.Version;
import com._1c.g5.v8.dt.testing.TestingPlatformSupport;
import com.e1c.g5.v8.dt.testing.check.SingleProjectReadOnlyCheckTestBase;

/**
 * Base implementation for the check tests that aren't change the single test configuration with optional extension
 *
 * @author Vadim Geraskin
 */
public abstract class SingleProjectWithExtReadOnlyCheckTestBase
    extends SingleProjectReadOnlyCheckTestBase
{
    @ClassRule
    public static TestingPlatformSupport testingPlatformSupport =
        new TestingPlatformSupport(Version.V8_3_8, Version.V8_3_9, Version.V8_3_10, Version.V8_3_11, Version.V8_3_12,
            Version.V8_3_13, Version.V8_3_14, Version.V8_3_15, Version.V8_3_16, Version.V8_3_17, Version.V8_3_18,
            Version.V8_3_19, Version.V8_3_20, Version.V8_3_21, Version.V8_3_22, Version.V8_3_23, Version.V8_3_24);

    private static final ThreadLocal<IDtProject> dtProjectExtHolder = new ThreadLocal<>();
    private static Object lock = new Object();

    /**
     * Gets the extension project being asscoiated with the current test case
     *
     * @return The {@link IDtProject} reference. May not be {@code null} in case of calling within the @Test - marked
     * methods
     */
    public IDtProject getExtProject()
    {
        return dtProjectExtHolder.get();
    }

    @Override
    @Before
    public void setUp() throws CoreException
    {
        super.setUp();
        synchronized (lock)
        {
            if (dtProjectExtHolder.get() == null)
            {
                dtProjectExtHolder.set(openProjectAndWaitForValidationFinish(getTestConfigurationExtName()));
            }
        }
    }

    @AfterClass
    public static void cleanUp() throws CoreException
    {
        synchronized (lock)
        {
            dtProjectExtHolder.remove();
        }
    }

    /**
     * Gets the name of the test extension configuration
     *
     * @return The name of the test configuration. Never {@code null}
     */
    protected abstract String getTestConfigurationExtName();
}
