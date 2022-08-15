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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.form.model.AutoCommandBar;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.service.FormIdentifierService;
import com._1c.g5.v8.dt.form.service.item.FormItemIterator;
import com.google.inject.Inject;

/**
 * Checks and fixes identifiers of {@link FormItem}s.
 * <p/>
 * {@link FormItem#getId()} is considered to be invalid if its value is {@code 0}
 * which is a default value for {@link org.eclipse.emf.ecore.EObject#eGet(EStructuralFeature, boolean)}
 * in case of {@link java.lang.Integer} feature.
 * <p/>
 * Negative values are not considered to be invalid. That is because such values are perfectly valid
 * at least for some of the cases. For example, {@link AutoCommandBar}
 * might have {@code -1} as in identifier. This can be seen in the implementation of
 * {@code com._1c.g5.v8.dt.internal.form.generator.FormGeneratorCore}.
 * <p/>
 * Additionally, form items have to have a unique identifier value
 * accross all other items on this {@link Form} regardless of how they are nested into each other.
 * When there are multiple form items with the same identifier then one of
 * them is considered to be valid while others are deemed to be problematic duplicates.
 * This is to reduce number of errors shown to user.
 * Identifiers that are already invalid (as described previously) do not paticipate into
 * duplicates check and are just reported as incorrect. This means that if there are two from items
 * with {@code 0} identifiers then both of them will be reported as having incorrect rather than duplicate
 * identifiers.
 * <p/>
 * Only {@link FormItem}`s are checked. {@link com._1c.g5.v8.dt.form.model.FormAttribute}
 * and other child content is ignored.
 * <p/>
 * When fixing, it does not try to understand why identifier is not good enough.
 * It just replaces it with a new value.
 * {@link FormIdentifierService} is used to determine what this new value should be.
 * However, there are exclusions. For example, {@link AutoCommandBar}
 * of a form will get predefined value of {@code -1} while {@link AutoCommandBar} of any
 * other item will get identifer from {@link FormIdentifierService}.
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdServiceImpl
    implements IInvalidItemIdService
{

    /**
     * Service that is used to generate new identifiers for broken form items.
     */
    private final FormIdentifierService formIdentifierService;

    /**
     * Creates an instance.
     *
     * @param formIdentifierService Service to be used to generate
     * new identifiers when fixing broken form items. Must not be {@code null}.
     */
    @Inject
    public InvalidItemIdServiceImpl(FormIdentifierService formIdentifierService)
    {
        this.formIdentifierService = formIdentifierService;
    }

    @Override
    public boolean isValid(Form form)
    {
        Set<Integer> seenIdentifiers = new HashSet<>();
        FormItemIterator itemIterator = new FormItemIterator(form);
        while (itemIterator.hasNext())
        {
            FormItem item = itemIterator.next();
            if (!hasValidId(item))
            {
                return false;
            }
            boolean uniqueId = seenIdentifiers.add(item.getId());
            if (!uniqueId)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<FormItem, String> validate(Form form)
    {
        if (form == null)
        {
            return Collections.emptyMap();
        }
        Map<FormItem, String> itemsWithIssues = new HashMap<>();
        Set<Integer> seenIdentifiers = new HashSet<>();
        FormItemIterator itemIterator = new FormItemIterator(form);
        while (itemIterator.hasNext())
        {
            FormItem item = itemIterator.next();
            if (!hasValidId(item))
            {
                itemsWithIssues.put(item, Messages.InvalidItemIdServiceImpl_InvalidValueOfIdAttribute);
            }
            else
            {
                int itemId = item.getId();
                if (!seenIdentifiers.add(itemId))
                {
                    itemsWithIssues.put(item,
                        MessageFormat.format(Messages.InvalidItemIdServiceImpl_DuplicateValueOfIdAttribute, itemId));
                }
            }
        }
        return itemsWithIssues;
    }

    @Override
    public void fix(FormItem item)
    {
        calculateNewIdFor(item).ifPresent(item::setId);
    }

    /**
     * Checks whether specified form item has a valid identifier.
     *
     * @param item Form item whose identifier is to be checked. Must not be {@code null}.
     * @return {@code true} if specified item has a valid identifier.
     */
    private boolean hasValidId(FormItem item)
    {
        return item.getId() != 0;
    }

    /**
     * Calculates new identifier for the specified form item.
     *
     * Some form items have special rules for identifiers.
     * For example, {@code com._1c.g5.v8.dt.internal.form.generator.FormGeneratorCore}
     * creates new forms with {@link com._1c.g5.v8.dt.form.model.AutoCommandBar}
     * that has identifier of {@code -1}.
     * We try to do the same here.
     *
     * @param item Form item that needs new identifier. Most not be {@code null}.
     * @return New value of identifier for the specified item or an empty holder if
     * unable to determine proper new identifier value.
     */
    private Optional<Integer> calculateNewIdFor(FormItem item)
    {
        boolean commandBarOfForm = item instanceof AutoCommandBar && item.eContainer() instanceof Form;
        if (commandBarOfForm)
        {
            return Optional.of(-1);
        }
        IBmObject topObject = item.bmGetTopObject();
        return topObject instanceof Form ? Optional.of(formIdentifierService.getNextItemId((Form)topObject))
            : Optional.empty();
    }

}
