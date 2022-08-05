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
package com.e1c.dt.check.internal;

import java.util.Map;

import com._1c.g5.v8.bm.core.IBmObject;

/**
 * Combines logic to validate and fix model objects.
 * <p/>
 * Validation ({@link com.e1c.g5.v8.dt.check.ICheck})
 * and fixing ({@link com.e1c.g5.v8.dt.check.qfix.IFix}) logic could be tightly related to each other.
 * Also, when the same problem is addressed by both cleanups
 * ({@link com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectObjectTasksProvider}) and check+fix
 * then, most likely, the same algorithms are to be shared between the two mechanism.
 * However, they're different in terms of how they're called.
 * <p/>
 * This interface is supposed to reflect the fact that some model-based cleanup could be represented
 * as a validation followed by a series of fixes - the same steps that are needed by checks and quick fixes.
 * It allows to co-locate the logic related to finding and fixing the issue itself without any restriction to
 * how it is going to be used later (from check, fix or cleanup).
 * <p/>
 * Specializations of this interface could be a base for service used in check, fix and cleanup
 * of issues of the same type.
 * <p/>
 * It is up to specific implementation how to traverse model:
 * <ul>
 * <li>top-to-bottom - For example, validation starts from {@link com._1c.g5.v8.dt.form.model.Form} and then
 * checks every {@link com._1c.g5.v8.dt.form.model.FormItem} within this form.</li>
 * <li>bottom-to-top - For example, validation starts from {@link com._1c.g5.v8.dt.form.model.FormItem} then
 * reports that {@link com._1c.g5.v8.dt.form.model.Form} has an issue.</li>
 * <li>single - For example, validation could check {@link com._1c.g5.v8.dt.form.model.FormItem} itself
 * and return result of checking this single item.</li>
 * </ul>
 * <p/>
 * NOTE: This is just implementation convenience when you need check, fix and cleanup.
 * You do not have to use it. It is not a replacement of
 * {@link com.e1c.g5.v8.dt.check.ICheck},
 * {@link com.e1c.g5.v8.dt.check.qfix.IFix} and
 * {@link com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectObjectTasksProvider}.
 * This is purely optional to keep things together in a cleaner OOP manner when you do check and fix and cleanup
 * for the same issue.
 *
 * @param S Type of objects from which validation is going to start.
 * For example, this could be a {@link com._1c.g5.v8.dt.form.model.Form} as a container or other elements.
 * @param T Type of objects that could be deemed as having issues.
 * For example, this could be {@link com._1c.g5.v8.dt.form.model.FormItem}s that have issues.
 * @param R Type of the issue.
 * For example, this could be {@code String} if all we need is just an error message describing
 * the issue related to the particular invalid object.
 *
 * @author Nikolay Martynov
 */
public interface IBmIntegrityService<S extends IBmObject, T extends IBmObject, R>
{

    /**
     * Checks for issues.
     *
     * Validation starts from {@code startingObject} and then traverses according to the algorithm
     * specific to the particular implementation.
     *
     * @param startingObject Object to start validation from. May be {@code null}
     * which by itself could be an issue to report in the result or not depending on validation logic.
     * @return A map where keys are objects with issues and values describe those issues.
     * If an object is missing among the keys then it means that this object has no issues.
     * The result should not contain {@code null} keys or values. Only objects with issues should be among the keys.
     * An empty map means no issues with any of the objects. The result must never be {@code null}.
     */
    Map<T, R> validate(S startingObject);

    /**
     * Fixes specified object.
     *
     * A way to fix the object is implementation specific.
     *
     * @param objectToBeFixed A particular object with an issue
     * (one of the keys in result returned by {@link #validate(IBmObject)})
     * that is to be fixed. Must not be {@code null}.
     */
    void fix(T objectToBeFixed);

}
