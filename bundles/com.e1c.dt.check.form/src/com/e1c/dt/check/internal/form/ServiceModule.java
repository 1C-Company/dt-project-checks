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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Module with internal services within the plugin.
 *
 * @author Nikolay Martynov
 */
public class ServiceModule
    extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(IInvalidItemIdService.class).to(InvalidItemIdServiceImpl.class).in(Singleton.class);
    }

}
