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
package com.e1c.dt.check.internal.form;

import java.util.Map;

import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;

/**
 * A service that can check and fix identifiers of form items.
 * <p/>
 * This service
 * gets a {@link Form} for checking and returns
 * an issue description as a {@code String}
 * for every child {@link FormItem} on this form (regardless of how deep it is nested)
 * that has a problem with its identifier.
 * It can also fix identifiers of those broken {@link FormItem}s one-by-one.
 *
 * @author Nikolay Martynov
 */
public interface IInvalidItemIdService
{

    /**
     * Name of the option that determines if tracing is enabled.
     */
    String DEBUG_OPTION = "/debug/InvalidItemId"; //$NON-NLS-1$

    /**
     * Identifier of the check.
     */
    String CHECK_ID = "form-invalid-item-id"; //$NON-NLS-1$

    /**
     * Validates specified form.
     *
     * @param form Form to validate. Must not be {@code null}.
     * @return A map where keys are form items with issues and values describe those issues.
     * If a form item is missing among the keys then it means that this form item has no issues.
     * The result should not contain {@code null} keys or values.
     * Only form items with issues should be among the keys.
     * An empty map means no issues with any of the form items.
     * The result must never be {@code null}.
     */
    Map<FormItem, String> validate(Form form);

    /**
     * Checks if there are issues with the specified form.
     *
     * The difference from {@link #validate(Form)} is that this is a short-circuit operation
     * that should return as soon as it is obvious that there are issues.
     *
     * @param form Form to validate. Must not be {@code null}.
     * @return {@code true} if there are issues to be fixed.
     */
    boolean isValid(Form form);

    /**
     * Fixes specified form item.
     *
     * @param formItemToFix A particular form item with an issue
     * (one of the keys in result returned by {@link #validate(Form)})
     * that is to be fixed. Must not be {@code null}.
     */
    void fix(FormItem formItemToFix);
}
