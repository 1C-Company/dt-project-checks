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
package com.e1c.dt.check.internal.test;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com.e1c.dt.check.internal.IBmIntegrityService;

/**
 * Tests for {@link com.e1c.dt.check.internal.IBmIntegrityService}.
 *
 */
public class IBmIntegrityServiceTest
{

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private IBmIntegrityService<Form, FormItem, String> integrityService;

    @Mock
    private Form form;

    /**
     * Default implementation delegates to {@link IBmIntegrityService#validate(com._1c.g5.v8.bm.core.IBmObject)}
     * and says object is valid if there are no issues.
     */
    @Test
    public final void testIsValidValid()
    {
        // given
        Mockito.when(integrityService.validate(form)).thenReturn(Collections.emptyMap());
        Mockito.when(integrityService.isValid(ArgumentMatchers.any())).thenCallRealMethod();
        // when
        boolean actual = integrityService.isValid(form);
        // then
        Assert.assertTrue(actual);
    }

    /**
     * Default implementation delegates to {@link IBmIntegrityService#validate(com._1c.g5.v8.bm.core.IBmObject)}
     * and says object is invalid if there are issues.
     */
    @Test
    public final void testIsValidInvalid()
    {
        // given
        FormItem child = Mockito.mock(FormItem.class);
        Mockito.when(integrityService.validate(form)).thenReturn(Map.of(child, "an issue"));
        Mockito.when(integrityService.isValid(ArgumentMatchers.any())).thenCallRealMethod();
        // when
        boolean actual = integrityService.isValid(form);
        // then
        Assert.assertFalse(actual);
    }

    /**
     * When object to validate is {@code null} then default implementation still delegates to
     * {@link IBmIntegrityService#validate(com._1c.g5.v8.bm.core.IBmObject)}
     * for actual validation.
     */
    @Test
    public final void testIsValidNull()
    {
        // given
        Mockito.when(integrityService.validate(ArgumentMatchers.isNull())).thenReturn(Collections.emptyMap());
        Mockito.when(integrityService.isValid(ArgumentMatchers.any())).thenCallRealMethod();
        // when
        boolean actual = integrityService.isValid(null);
        // then
        Mockito.verify(integrityService, Mockito.times(1)).validate(ArgumentMatchers.isNull());
        Assert.assertTrue(actual);
    }
}
