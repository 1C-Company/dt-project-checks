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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.form.model.AutoCommandBar;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.form.service.FormIdentifierService;
import com._1c.g5.v8.dt.form.service.item.FormItemIterator;
import com.google.inject.Inject;

/**
 * Checks and fixes identifiers of {@link FormItem}s.
 * <p/>
 * {@link FormItem#getId()} is valid if it has a unique value
 * accross all other items on this {@link Form}.
 * <p/>
 * Additionally, an identifier is considered to be invalid if its value is {@code 0}
 * which is a default value for {@link org.eclipse.emf.ecore.EObject#eGet(EStructuralFeature, boolean)}
 * in case of {@link java.lang.Integer} feature.
 * <p/>
 * Negative values are not considered to be invalid. That is because such values are perfectly valid
 * at least for some of the cases. For example, {@link com._1c.g5.v8.dt.form.model.AutoCommandBar}
 * might have {@code -1} as in identifier. This can be seen in the implementation of
 * {@code com._1c.g5.v8.dt.internal.form.generator.FormGeneratorCore}.
 * <p/>
 * Only {@link FormItem}`s are checked. {@link com._1c.g5.v8.dt.form.model.FormAttribute}
 * and other child content is ignored.
 * <p/>
 * When fixing, it does not try to understand why identifier is not good enough.
 * It just replaces it with a new value.
 * {@link FormIdentifierService} is used to determine what this new value should be.
 * However, there are exclusions. For example, command bar of a form will get predefined value of {@code -1}.
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdServiceImpl
    implements IInvalidItemIdService
{

    /**
     * Service that is used to generate new identifiers for broken form items.
     */
    @Inject
    private FormIdentifierService formIdentifierService;

    @Override
    public Map<FormItem, String> validate(Form form)
    {
        CorePlugin.trace(DEBUG_OPTION, "Check: Checking form: {0}", form); //$NON-NLS-1$
        if (form == null)
        {
            return Collections.emptyMap();
        }
        Map<Boolean, List<FormItem>> validAndInvalidIdentifiers =
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(new FormItemIterator(form), 0), false)
                .collect(Collectors.partitioningBy(this::hasValidId));
        Stream<Map.Entry<FormItem, String>> invalidIdIssues = validAndInvalidIdentifiers.get(false)
            .stream()
            .peek(item -> CorePlugin.trace(DEBUG_OPTION, "Check: Form item has an invalid identifier: id={0}, item={1}", //$NON-NLS-1$
                item.getId(), item))
            .map(item -> new AbstractMap.SimpleEntry<>(item,
                Messages.InvalidItemIdServiceImpl_InvalidValueOfIdAttribute));
        Stream<Map.Entry<FormItem, String>> duplicateIdIssues = validAndInvalidIdentifiers.get(true)
            .stream()
            .collect(Collectors.groupingBy(FormItem::getId))
            .entrySet()
            .stream()
            .filter(idAndItems -> idAndItems.getValue().size() > 1)
            .peek(idAndItems -> CorePlugin.trace(DEBUG_OPTION,
                "Check: Form has items with duplicate itentifiers: id={0}, items={1}", //$NON-NLS-1$
                idAndItems.getKey(), idAndItems.getValue()))
            .flatMap(idAndItems -> idAndItems.getValue()
                .stream()
                .skip(1)
                .map(item -> new AbstractMap.SimpleEntry<>(item, MessageFormat
                    .format(Messages.InvalidItemIdServiceImpl_DuplicateValueOfIdAttribute, idAndItems.getKey()))));
        Map<FormItem, String> allIssues = Stream.concat(invalidIdIssues, duplicateIdIssues)
            .collect(Collectors.toMap(item -> item.getKey(), message -> message.getValue()));
        return allIssues;
    }

    @Override
    public void fix(FormItem item)
    {
        boolean wasSet = item.eIsSet(FormPackage.Literals.FORM_ITEM__ID);
        Object oldValue = item.eGet(FormPackage.Literals.FORM_ITEM__ID);
        Optional<Integer> newValue = calculateNewIdFor(item);
        if (newValue.isEmpty())
        {
            CorePlugin.trace(DEBUG_OPTION,
                "Fix: To avoid data corruption, will not change identifier of {0} because unable to determine proper new value", //$NON-NLS-1$
                item);
            return;
        }
        item.setId(newValue.get());
        CorePlugin.trace(DEBUG_OPTION, "Fix: Replaced identifier of {0}: wasSet={1}, oldValue={2}, newValue={3}", //$NON-NLS-1$
            item, wasSet, oldValue, newValue.get());
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
            CorePlugin.trace(DEBUG_OPTION,
                "Fix: This seem as command bar of a form so will use -1 for identifier: item={0}, container={1}", item, //$NON-NLS-1$
                item.eContainer());
            return Optional.of(-1);
        }
        IBmObject topObject = item.bmGetTopObject();
        if (!(topObject instanceof Form))
        {
            CorePlugin.trace(DEBUG_OPTION, "Fix: Unable to determine enclosing form: item={0}, topObject={1}", item, //$NON-NLS-1$
                item.bmGetTopObject());
            return Optional.empty();
        }
        Form form = (Form)item.bmGetTopObject();
        int newValue = formIdentifierService.getNextItemId(form);
        return Optional.of(newValue);
    }

}
