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

import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com.e1c.dt.check.internal.IBmIntegrityService;

/**
 * A service that can check and fix identifiers of form items.
 * <p/>
 * This specialization of {@link IBmIntegrityService}
 * gets a {@link Form} for checking and returns
 * an issue description as a {@code String}
 * for every child {@link FormItem} on this form (regardless of how deep it is nested)
 * that has an problem with its identifier.
 * It can also fix identifiers of those broken {@link FormItem}s one-by-one.
 *
 * @author Nikolay Martynov
 */
// This is not a marker interface. It is a specialization with the specific type arguments.
@SuppressWarnings("checkstyle:InterfaceIsType")
public interface IInvalidItemIdService
    extends IBmIntegrityService<Form, FormItem, String>
{

    /**
     * Name of the option that determines if tracing is enabled.
     */
    String DEBUG_OPTION = "/debug/InvalidItemId"; //$NON-NLS-1$

    /**
     * Identifier of the check.
     */
    String CHECK_ID = "form-invalid-item-id"; //$NON-NLS-1$
}