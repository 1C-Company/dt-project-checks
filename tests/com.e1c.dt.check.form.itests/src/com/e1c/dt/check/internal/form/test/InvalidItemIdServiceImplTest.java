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
package com.e1c.dt.check.internal.form.test;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com._1c.g5.v8.dt.form.model.AutoCommandBar;
import com._1c.g5.v8.dt.form.model.CommandBarHolder;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormAttribute;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormItemContainer;
import com._1c.g5.v8.dt.form.model.Table;
import com._1c.g5.v8.dt.form.service.FormIdentifierService;
import com.e1c.dt.check.internal.form.IInvalidItemIdService;
import com.e1c.dt.check.internal.form.InvalidItemIdServiceImpl;

/**
 * Tests for {@link InvalidItemIdServiceImpl}.
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdServiceImplTest
{

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private FormIdentifierService formIdentifierServiceMock;

    @Mock
    private Form formMock;

    @InjectMocks
    private InvalidItemIdServiceImpl invalidItemIdServiceImpl;

    /**
     * {@link com.e1c.dt.check.internal.IBmIntegrityService#validate} allows {@code null}
     * as object to be validated. So it should not cause an error. Instead, we should just tell there are
     * no issues.
     */
    @Test
    public final void testValidateNull()
    {
        // given
        Form form = null;
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(form);
        // then
        Assertions.assertThat(actual).isEmpty();
    }

    /**
     * When form is empty then the result should be empty too.
     */
    @Test
    public final void testValidateEmptyForm()
    {
        // given
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>());
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).isEmpty();
    }

    /**
     * Zero identifier of form item should lead to an issue with non-blank description.
     */
    @Test
    public final void testValidateZeroId()
    {
        // given
        int id = 0;
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(item)));
        Mockito.when(item.getId()).thenReturn(id);
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual)
            .isNotNull()
            .containsOnlyKeys(item)
            .allSatisfy((issueItem, issueDescription) -> Assertions.assertThat(issueDescription).isNotBlank());
    }

    /**
     * Negative identifier of form item should not lead to an issue.
     */
    @Test
    public final void testValidateNegativeId()
    {
        // given
        int id = -2;
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(item)));
        Mockito.when(item.getId()).thenReturn(id);
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).isEmpty();
    }

    /**
     * Positive identifier of form item should not lead to an issue.
     */
    @Test
    public final void testValidatePositiveId()
    {
        // given
        int id = 1;
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(item)));
        Mockito.when(item.getId()).thenReturn(id);
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).isEmpty();
    }

    /**
     * When form has multiple items with issues then all problematic items are returned but non-problematic
     * items are omitted.
     */
    @Test
    public final void testValidateReturnsMultipleIssues()
    {
        // given
        FormItem invalidItem1 = Mockito.mock(FormItem.class);
        Mockito.when(invalidItem1.getId()).thenReturn(0);
        FormItem validItem2 = Mockito.mock(FormItem.class);
        Mockito.when(validItem2.getId()).thenReturn(1);
        FormItem invalidItem3 = Mockito.mock(FormItem.class);
        Mockito.when(invalidItem3.getId()).thenReturn(0);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(invalidItem1, validItem2, invalidItem3)));
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).doesNotContainKey(validItem2).containsOnlyKeys(invalidItem1, invalidItem3);
    }

    /**
     * When one form item contains another form item and this nested form item has an issue
     * then this issue is reported.
     */
    @Test
    public final void testValidateNestedIssues()
    {
        // given
        FormItem innerItem = Mockito.mock(FormItem.class);
        Mockito.when(innerItem.getId()).thenReturn(0);
        FormItem outerItem = Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(FormItemContainer.class));
        Mockito.when(outerItem.getId()).thenReturn(1);
        Mockito.when(((FormItemContainer)outerItem).getItems()).thenReturn(new BasicEList<>(List.of(innerItem)));
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(outerItem)));
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).containsOnlyKeys(innerItem);
    }

    /**
     * When there are two items with duplicate but otherwise correct identifiers
     * then one of them should be reported as an issue.
     */
    @Test
    public final void testValidateDuplicate()
    {
        // given
        FormItem duplicate1 = Mockito.mock(FormItem.class);
        Mockito.when(duplicate1.getId()).thenReturn(1);
        FormItem duplicate2 = Mockito.mock(FormItem.class);
        Mockito.when(duplicate2.getId()).thenReturn(1);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(duplicate1, duplicate2)));
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual)
            .satisfiesAnyOf(result -> Assertions.assertThat(result).containsOnlyKeys(duplicate1),
                result -> Assertions.assertThat(result).containsOnlyKeys(duplicate2));
    }

    /**
     * When there are two items with the same incorrect identifier value
     * then both of them should be reported as an issue.
     */
    @Test
    public final void testValidateDuplicateAndIncorrect()
    {
        // given
        FormItem incorrectAndDuplicate1 = Mockito.mock(FormItem.class);
        Mockito.when(incorrectAndDuplicate1.getId()).thenReturn(0);
        FormItem incorrectAndDuplicate2 = Mockito.mock(FormItem.class);
        Mockito.when(incorrectAndDuplicate2.getId()).thenReturn(0);
        Mockito.when(formMock.getItems())
            .thenReturn(new BasicEList<>(List.of(incorrectAndDuplicate1, incorrectAndDuplicate2)));
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).containsOnlyKeys(incorrectAndDuplicate1, incorrectAndDuplicate2);
    }

    /**
     * It should not matter how form items are nested into each other when looking for
     * duplicate identifiers. When there are two duplicates in different hierarchies
     * then still one of them should be reported as an issue.
     */
    @Test
    public final void testValidateDuplicatesInDifferentHierarchies()
    {
        // given
        int id = 0;
        FormItem duplicateInnerItem1 = Mockito.mock(FormItem.class);
        Mockito.when(duplicateInnerItem1.getId()).thenReturn(++id);
        FormItem outerItem1 =
            Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(FormItemContainer.class));
        Mockito.when(outerItem1.getId()).thenReturn(++id);
        Mockito.when(((FormItemContainer)outerItem1).getItems())
            .thenReturn(new BasicEList<>(List.of(duplicateInnerItem1)));
        FormItem duplicateInnerItem2 = Mockito.mock(FormItem.class);
        // We do not increment id here creating duplication at another nesting level.
        Mockito.when(duplicateInnerItem2.getId()).thenReturn(id);
        FormItem outerItem2 =
            Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(FormItemContainer.class));
        Mockito.when(outerItem2.getId()).thenReturn(++id);
        Mockito.when(((FormItemContainer)outerItem2).getItems())
            .thenReturn(new BasicEList<>(List.of(duplicateInnerItem2)));
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(outerItem1, outerItem2)));
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual)
            .satisfiesAnyOf(result -> Assertions.assertThat(result).containsOnlyKeys(duplicateInnerItem1),
                result -> Assertions.assertThat(result).containsOnlyKeys(duplicateInnerItem2));
    }

    /**
     * When there are issues of different types (invalid vs duplicate) then all issues of all types are reported
     * while good items are not reported.
     */
    @Test
    public final void testValidateMixed()
    {
        // given
        FormItem duplicate1 = Mockito.mock(FormItem.class);
        Mockito.when(duplicate1.getId()).thenReturn(1);
        FormItem duplicate2 = Mockito.mock(FormItem.class);
        Mockito.when(duplicate2.getId()).thenReturn(1);
        FormItem incorrect = Mockito.mock(FormItem.class);
        Mockito.when(incorrect.getId()).thenReturn(0);
        FormItem correct = Mockito.mock(FormItem.class);
        Mockito.when(correct.getId()).thenReturn(2);
        Mockito.when(formMock.getItems())
            .thenReturn(new BasicEList<>(List.of(duplicate1, duplicate2, incorrect, correct)));
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual)
            .doesNotContainKey(correct)
            .satisfiesAnyOf(result -> Assertions.assertThat(result).containsOnlyKeys(duplicate1, incorrect),
                result -> Assertions.assertThat(result).containsOnlyKeys(duplicate2, incorrect));
    }

    /**
     * Validation should ignore non-{@link FormItem} content like attributes.
     */
    @Test
    public final void testValidateIgnoresNonItems()
    {
        // given
        FormAttribute duplicate1 = Mockito.mock(FormAttribute.class);
        Mockito.lenient().when(duplicate1.getId()).thenReturn(1);
        FormAttribute duplicate2 = Mockito.mock(FormAttribute.class);
        Mockito.lenient().when(duplicate2.getId()).thenReturn(1);
        FormAttribute incorrect = Mockito.mock(FormAttribute.class);
        Mockito.lenient().when(incorrect.getId()).thenReturn(0);
        Mockito.lenient()
            .when(formMock.getAttributes())
            .thenReturn(new BasicEList<>(List.of(duplicate1, duplicate2, incorrect)));
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>());
        // when
        Map<FormItem, String> actual = invalidItemIdServiceImpl.validate(formMock);
        // then
        Assertions.assertThat(actual).isEmpty();
    }

    /**
     * When fixing an item, should ask identifeirs service for new identifier value and assign it.
     * Identifeirs serice wants to inspect a form. It should be obtained as top object of the item.
     */
    @Test
    public final void testFixIncorrectId()
    {
        // given
        int oldId = 0;
        int newId = 1;
        Mockito.when(formIdentifierServiceMock.getNextItemId(formMock)).thenReturn(newId);
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.lenient().when(item.getId()).thenReturn(oldId);
        Mockito.when(item.bmGetTopObject()).thenReturn(formMock);
        // when
        invalidItemIdServiceImpl.fix(item);
        // then
        Mockito.verify(item).setId(newId);
    }

    /**
     * When fixing an item, it should not matter if current identifier value seems correct
     * (because it might be a duplicate).
     * We still should ask identifeirs service for new value and assign it.
     * Identifiers serice wants to inspect a form. It should be obtained as top object of the item.
     */
    @Test
    public final void testFixCorrectId()
    {
        // given
        int oldId = 1;
        int newId = 2;
        Mockito.when(formIdentifierServiceMock.getNextItemId(formMock)).thenReturn(newId);
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.lenient().when(item.getId()).thenReturn(oldId);
        Mockito.when(item.bmGetTopObject()).thenReturn(formMock);
        // when
        invalidItemIdServiceImpl.fix(item);
        // then
        Mockito.verify(item).setId(newId);
    }

    /**
     * When fixing auto command bar of a form, predefined value of {@code -1} is to be used
     * as new identifier. EMF containement feature is to be probed to determine if auto command bar
     * is directly on a form or somewhere else.
     * Identifeirs serice is not supposed to be used.
     */
    @Test
    public final void testFixAutoCommandBarOfForm()
    {
        // given
        int newId = -1;
        AutoCommandBar commandBar = Mockito.mock(AutoCommandBar.class);
        Mockito.when(commandBar.eContainer()).thenReturn(formMock);
        // when
        invalidItemIdServiceImpl.fix(commandBar);
        // then
        Mockito.verify(commandBar).setId(newId);
        Mockito.verify(formIdentifierServiceMock, Mockito.never()).getNextItemId(ArgumentMatchers.any());
        Mockito.verify(commandBar, Mockito.never()).bmGetTopObject();
    }

    /**
     * When fixing auto command bar of something different than a form
     * (as per EMF containment feature),
     * then the usual algorithm applies: ask item for a form, pass this form to
     * identifiers service and set new value as identifier of the item.
     */
    @Test
    public final void testFixAutoCommandBarOfNotForm()
    {
        // given
        int newId = 1;
        Mockito.when(formIdentifierServiceMock.getNextItemId(formMock)).thenReturn(newId);
        CommandBarHolder notForm = Mockito.mock(CommandBarHolder.class);
        AutoCommandBar commandBar = Mockito.mock(AutoCommandBar.class);
        Mockito.when(commandBar.eContainer()).thenReturn(notForm);
        Mockito.when(commandBar.bmGetTopObject()).thenReturn(formMock);
        // when
        invalidItemIdServiceImpl.fix(commandBar);
        // then
        Mockito.verify(commandBar).setId(newId);
    }

    /**
     * {@link com.e1c.dt.check.internal.IBmIntegrityService#isValid()} allows {@code null}
     * as object to be validated. So it should not cause an error. Instead, we should just tell there are
     * no issues.
     */
    @Test
    public final void testIsvalidNull()
    {
        // given
        Form form = null;
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(form);
        // then
        Assert.assertTrue(actual);
    }

    /**
     * When form is empty then it is valid.
     */
    @Test
    public final void testIsValidEmptyForm()
    {
        // given
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>());
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertTrue(actual);
    }

    /**
     * Zero identifier of form item means form is not valid.
     */
    @Test
    public final void testIsValidZeroId()
    {
        // given
        int id = 0;
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(item)));
        Mockito.when(item.getId()).thenReturn(id);
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertFalse(actual);
    }

    /**
     * Negative identifier of form item does not mean that form is invalid.
     */
    @Test
    public final void testIsValideNegativeId()
    {
        // given
        int id = -2;
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(item)));
        Mockito.when(item.getId()).thenReturn(id);
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertTrue(actual);
    }

    /**
     * Positive identifier of form item does not invalidate form.
     */
    @Test
    public final void testIsValidPositiveId()
    {
        // given
        int id = 1;
        FormItem item = Mockito.mock(FormItem.class);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(item)));
        Mockito.when(item.getId()).thenReturn(id);
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertTrue(actual);
    }

    /**
     * When form has multiple items with issues then {@link IInvalidItemIdService#isValid(Form)}
     * tells the form is invalid.
     */
    @Test
    public final void testIsValidMultipleIssues()
    {
        // given
        FormItem invalidItem1 = Mockito.mock(FormItem.class);
        Mockito.when(invalidItem1.getId()).thenReturn(0);
        FormItem validItem2 = Mockito.mock(FormItem.class);
        Mockito.lenient().when(validItem2.getId()).thenReturn(1);
        FormItem invalidItem3 = Mockito.mock(FormItem.class);
        Mockito.lenient().when(invalidItem3.getId()).thenReturn(0);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(invalidItem1, validItem2, invalidItem3)));
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertFalse(actual);
    }

    /**
     * When one form item contains another form item and this nested form item has an issue
     * then the form is reported as invalid.
     */
    @Test
    public final void testIsValidNestedIssues()
    {
        // given
        FormItem innerItem = Mockito.mock(FormItem.class);
        Mockito.when(innerItem.getId()).thenReturn(0);
        FormItem outerItem = Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(FormItemContainer.class));
        Mockito.when(outerItem.getId()).thenReturn(1);
        Mockito.when(((FormItemContainer)outerItem).getItems()).thenReturn(new BasicEList<>(List.of(innerItem)));
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(outerItem)));
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertFalse(actual);
    }

    /**
     * When there are two items with duplicate but otherwise correct identifiers
     * then the form is reported as invalid.
     */
    @Test
    public final void testIsValidDuplicate()
    {
        // given
        FormItem duplicate1 = Mockito.mock(FormItem.class);
        Mockito.when(duplicate1.getId()).thenReturn(1);
        FormItem duplicate2 = Mockito.mock(FormItem.class);
        Mockito.when(duplicate2.getId()).thenReturn(1);
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(duplicate1, duplicate2)));
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertFalse(actual);
    }

    /**
     * It should not matter how form items are nested into each other when looking for
     * duplicate identifiers. When there are two duplicates in different hierarchies
     * then form should be reported as invalid.
     */
    @Test
    public final void testIsValidDuplicatesInDifferentHierarchies()
    {
        // given
        int id = 0;
        FormItem duplicateInnerItem1 = Mockito.mock(FormItem.class);
        Mockito.when(duplicateInnerItem1.getId()).thenReturn(++id);
        FormItem outerItem1 =
            Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(FormItemContainer.class));
        Mockito.when(outerItem1.getId()).thenReturn(++id);
        Mockito.when(((FormItemContainer)outerItem1).getItems())
            .thenReturn(new BasicEList<>(List.of(duplicateInnerItem1)));
        FormItem duplicateInnerItem2 = Mockito.mock(FormItem.class);
        // We do not increment id here creating duplication at another nesting level.
        Mockito.when(duplicateInnerItem2.getId()).thenReturn(id);
        FormItem outerItem2 =
            Mockito.mock(Table.class, Mockito.withSettings().extraInterfaces(FormItemContainer.class));
        Mockito.when(outerItem2.getId()).thenReturn(++id);
        Mockito.when(((FormItemContainer)outerItem2).getItems())
            .thenReturn(new BasicEList<>(List.of(duplicateInnerItem2)));
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>(List.of(outerItem1, outerItem2)));
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertFalse(actual);
    }

    /**
    * Validation should ignore non-{@link FormItem} content like attributes.
    */
    @Test
    public final void testIsValidIgnoresNonItems()
    {
        // given
        FormAttribute duplicate1 = Mockito.mock(FormAttribute.class);
        Mockito.lenient().when(duplicate1.getId()).thenReturn(1);
        FormAttribute duplicate2 = Mockito.mock(FormAttribute.class);
        Mockito.lenient().when(duplicate2.getId()).thenReturn(1);
        FormAttribute incorrect = Mockito.mock(FormAttribute.class);
        Mockito.lenient().when(incorrect.getId()).thenReturn(0);
        Mockito.lenient()
            .when(formMock.getAttributes())
            .thenReturn(new BasicEList<>(List.of(duplicate1, duplicate2, incorrect)));
        Mockito.when(formMock.getItems()).thenReturn(new BasicEList<>());
        // when
        boolean actual = invalidItemIdServiceImpl.isValid(formMock);
        // then
        Assert.assertTrue(actual);
    }

}
