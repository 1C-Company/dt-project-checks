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
package com.e1c.dt.check.internal.form.cleanup.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.bm.integration.IBmTask;
import com._1c.g5.v8.dt.core.model.EditingMode;
import com._1c.g5.v8.dt.core.model.IModelEditingSupport;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectObjectTasksProvider.ICleanUpBmObjectTask;
import com.e1c.dt.check.internal.form.IInvalidItemIdService;
import com.e1c.dt.check.internal.form.cleanup.InvalidItemIdCleanup;

/**
 * Tests for {@link InvalidItemIdCleanup}.
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdCleanupTest
{

    private static final long FORM3_ID = 3;

    private static final long FORM2_ID = 2;

    private static final long FORM1_ID = 1;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private IInvalidItemIdService invalidItemIdService;

    @Mock
    private IBmModelManager bmModelManager;

    @Mock
    private IModelEditingSupport editingSupport;

    @Mock
    private IDtProject project;

    @Mock
    private IBmModel model;

    @Mock
    private IBmTransaction scanTransaction;

    @Mock
    private IBmTransaction fixTransaction;

    @Mock
    private IProgressMonitor scanProgressMonitor;

    @Mock
    private IProgressMonitor fixProgressMonitor;

    @Mock
    private Form form1;

    @Mock
    private Form form2;

    @Mock
    private Form form3;

    // We're mocking it and so unable to pass iterator's type argument.
    @SuppressWarnings("unchecked")
    @Mock
    Iterator<IBmObject> formsIterator = Mockito.mock(Iterator.class);

    @Captor
    ArgumentCaptor<IBmTask<List<ICleanUpBmObjectTask>>> scanTask;

    @InjectMocks
    private InvalidItemIdCleanup cleanup;

    @Before
    public void mockAsPerContract()
    {
        // If BM instance can be obtained
        Mockito.lenient().when(bmModelManager.getModel(project)).thenReturn(model);
        // non-lightweight read-only scanTransaction is used to
        // There are two alike methods. Implementation is free to use any of them.
        Mockito.lenient()
            .when(model.executeReadonlyTask(scanTask.capture()))
            .then(invocation -> scanTask.getValue().execute(scanTransaction, scanProgressMonitor));
        Mockito.lenient()
            .when(model.executeReadonlyTask(scanTask.capture(), ArgumentMatchers.eq(false)))
            .then(invocation -> scanTask.getValue().execute(scanTransaction, scanProgressMonitor));
        // list forms(via IBmTransaction.getTopObjectIterator(EClass))
        Mockito.lenient().when(formsIterator.hasNext()).thenReturn(true, true, true, false);
        Mockito.lenient().when(formsIterator.next()).thenReturn(form1, form2, form3);
        Mockito.lenient()
            .when(scanTransaction.getTopObjectIterator(FormPackage.Literals.FORM))
            .thenReturn(formsIterator);
        // for each form it is checked if it can be edited
        Mockito.lenient()
            .when(editingSupport.canEdit(
                ArgumentMatchers.argThat(argumentValue -> Set.of(form1, form2, form3).contains(argumentValue)),
                ArgumentMatchers.eq(EditingMode.DIRECT)))
            .thenReturn(true);
        // Both scanning and fixing will be stopped as soon as cancellation is reported.
        Mockito.lenient().when(scanProgressMonitor.isCanceled()).thenReturn(false);
        Mockito.lenient().when(fixProgressMonitor.isCanceled()).thenReturn(false);
        // After analsis of the form, it will be evicted from the transaction
        Mockito.lenient().when(form1.bmGetId()).thenReturn(Long.valueOf(FORM1_ID));
        Mockito.lenient().when(form2.bmGetId()).thenReturn(Long.valueOf(FORM2_ID));
        Mockito.lenient().when(form3.bmGetId()).thenReturn(Long.valueOf(FORM3_ID));
        // Form cleanup tasks will obtain corresponding form (by its identifier) from the transaction
        Mockito.lenient().when(fixTransaction.getObjectById(FORM1_ID)).thenReturn(form1);
        Mockito.lenient().when(fixTransaction.getObjectById(FORM2_ID)).thenReturn(form2);
        Mockito.lenient().when(fixTransaction.getObjectById(FORM3_ID)).thenReturn(form3);
    }

    /**
     * Should safely return empty task list if there is no BM instance.
     */
    @Test
    public final void testSafeExitIfNoModel()
    {
        // given
        Mockito.when(bmModelManager.getModel(project)).thenReturn(null);
        // when
        List<ICleanUpBmObjectTask> actual = cleanup.getCleanUpProjectTasks(project);
        // then
        Assertions.assertThat(actual).isEmpty();
        Mockito.verifyNoMoreInteractions(editingSupport, invalidItemIdService);
    }

    /**
     * Should skip form validation if not allowed to edit it.
     */
    @Test
    public final void testSkipReadOnlyForm()
    {
        // given
        Mockito.when(editingSupport.canEdit(form1, EditingMode.DIRECT)).thenReturn(false);
        Mockito.when(editingSupport.canEdit(form2, EditingMode.DIRECT)).thenReturn(false);
        Mockito.when(editingSupport.canEdit(form3, EditingMode.DIRECT)).thenReturn(false);
        // when
        List<ICleanUpBmObjectTask> actual = cleanup.getCleanUpProjectTasks(project);
        // then
        Assertions.assertThat(actual).isEmpty();
        Mockito.verify(editingSupport, Mockito.atLeastOnce()).canEdit(form1, EditingMode.DIRECT);
        Mockito.verify(editingSupport, Mockito.atLeastOnce()).canEdit(form2, EditingMode.DIRECT);
        Mockito.verify(editingSupport, Mockito.atLeastOnce()).canEdit(form3, EditingMode.DIRECT);
        Mockito.verifyNoMoreInteractions(invalidItemIdService);
    }

    /**
     * Should skip forms without issues.
     */
    @Test
    public final void testSkipNonIssueForms()
    {
        // given
        Mockito
            .when(invalidItemIdService.isValid(
                ArgumentMatchers.argThat(argumentValue -> Set.of(form1, form2, form3).contains(argumentValue))))
            .thenReturn(true);
        // when
        List<ICleanUpBmObjectTask> actual = cleanup.getCleanUpProjectTasks(project);
        // then
        Assertions.assertThat(actual).isEmpty();
        Mockito.verify(invalidItemIdService, Mockito.atLeastOnce()).isValid(form1);
        Mockito.verify(invalidItemIdService, Mockito.atLeastOnce()).isValid(form2);
        Mockito.verify(invalidItemIdService, Mockito.atLeastOnce()).isValid(form3);
        Mockito.verifyNoMoreInteractions(invalidItemIdService);
    }

    /**
     * Should immediately evict a form before going to quick check the next one to conserve memory.
     */
    @Test
    public final void testEvictsFormsImmediately()
    {
        // given
        Mockito
            .when(invalidItemIdService.isValid(
                ArgumentMatchers.argThat(argumentValue -> Set.of(form1, form2, form3).contains(argumentValue))))
            .thenReturn(true);
        // when
        cleanup.getCleanUpProjectTasks(project);
        // then
        InOrder order = Mockito.inOrder(formsIterator, scanTransaction, invalidItemIdService);
        order.verify(scanTransaction).getTopObjectIterator(ArgumentMatchers.any());
        order.verify(formsIterator).next();
        order.verify(invalidItemIdService).isValid(form1);
        order.verify(scanTransaction).evict(FORM1_ID);
        order.verify(formsIterator).next();
        order.verify(invalidItemIdService).isValid(form2);
        order.verify(scanTransaction).evict(FORM2_ID);
        order.verify(formsIterator).next();
        order.verify(invalidItemIdService).isValid(form3);
        order.verify(scanTransaction).evict(FORM3_ID);
        Assert.assertFalse(order.verify(formsIterator).hasNext());
        order.verifyNoMoreInteractions();
    }

    /**
     * For each form with issues, a separate cleanup task should be returned.
     */
    @Test
    public final void testReturnsForEachFormWithIssues()
    {
        // given
        Mockito.when(invalidItemIdService.isValid(form1)).thenReturn(true);
        Mockito.when(invalidItemIdService.isValid(form2)).thenReturn(false);
        Mockito.when(invalidItemIdService.isValid(form3)).thenReturn(false);
        // when
        List<ICleanUpBmObjectTask> actual = cleanup.getCleanUpProjectTasks(project);
        // then
        Assertions.assertThat(actual).size().isEqualTo(2);
    }

    /**
     * Form cleanup tasks will obtain corresponding form (by its identifier) from the transaction,
     * perform full validation thanks to IInvalidItemIdService and
     * fix each malformed form item identifierusing the same service.
     */
    @Test
    public final void testCleanupTasksLoadAndValidateFormsThenFixItems()
    {
        // given
        FormItem item1 = Mockito.mock(FormItem.class);
        FormItem item2 = Mockito.mock(FormItem.class);
        FormItem item3 = Mockito.mock(FormItem.class);
        FormItem item4 = Mockito.mock(FormItem.class);
        FormItem item5 = Mockito.mock(FormItem.class);
        Mockito.when(invalidItemIdService.isValid(ArgumentMatchers.any())).thenReturn(false);
        Mockito.when(invalidItemIdService.validate(form1)).thenReturn(Map.of(item1, "1"));
        Mockito.when(invalidItemIdService.validate(form2)).thenReturn(Map.of(item2, "2"));
        Mockito.when(invalidItemIdService.validate(form3)).thenReturn(Map.of(item3, "3", item4, "4", item5, "5"));
        // when
        cleanup.getCleanUpProjectTasks(project).forEach(fixTask -> fixTask.execute(fixTransaction, fixProgressMonitor));
        // then
        Mockito.verify(fixTransaction, Mockito.times(1)).getObjectById(FORM1_ID);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).fix(item1);
        Mockito.verify(fixTransaction, Mockito.times(1)).getObjectById(FORM2_ID);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).fix(item2);
        Mockito.verify(fixTransaction, Mockito.times(1)).getObjectById(FORM3_ID);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).fix(item3);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).fix(item4);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).fix(item5);
    }

    /**
     * Scanning should stop immediately when user has requested to cancel progress.
     */
    @Test
    public final void testScanningInterruptsWhenCancelled()
    {
        // given
        Mockito.when(invalidItemIdService.isValid(ArgumentMatchers.any())).thenReturn(false);
        Mockito.when(scanProgressMonitor.isCanceled()).thenReturn(false, true);
        // when
        List<ICleanUpBmObjectTask> actual = cleanup.getCleanUpProjectTasks(project);
        // then
        Assertions.assertThat(actual).size().isEqualTo(1);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).isValid(ArgumentMatchers.any());
        Mockito.verify(scanProgressMonitor, Mockito.times(2)).isCanceled();
    }

    /**
     * Fixing should stop immediately when user has requested to cancel progress.
     * As per contract: before loading form to do full validation and before fixing each form item.
     */
    @Test
    public final void testFixingInterruptsWhenCancelled()
    {
        // given
        FormItem item1 = Mockito.mock(FormItem.class);
        FormItem item2 = Mockito.mock(FormItem.class);
        Mockito.when(invalidItemIdService.isValid(ArgumentMatchers.any())).thenReturn(false);
        Mockito.when(invalidItemIdService.validate(form1)).thenReturn(Map.of(item1, "1", item2, "2"));
        // Before loading form1, before fixing item1, before fixing item2, before loading form2, before loading form3
        Mockito.when(fixProgressMonitor.isCanceled()).thenReturn(false, false, true);
        // when
        for (IBmTask<Void> fixTask : cleanup.getCleanUpProjectTasks(project))
        {
            fixTask.execute(fixTransaction, fixProgressMonitor);
        }
        // then
        Mockito.verify(fixTransaction, Mockito.times(1)).getObjectById(FORM1_ID);
        Mockito.verify(invalidItemIdService, Mockito.times(1)).validate(form1);
        // Only one of the items is fixed.
        // Second item and the rest of the tasks are skipped.
        Mockito.verify(invalidItemIdService, Mockito.times(1)).fix(ArgumentMatchers.any());
        Mockito.verify(fixTransaction, Mockito.never()).getObjectById(FORM2_ID);
        Mockito.verify(invalidItemIdService, Mockito.never()).validate(form2);
        Mockito.verify(fixTransaction, Mockito.never()).getObjectById(FORM3_ID);
        Mockito.verify(invalidItemIdService, Mockito.never()).validate(form3);
    }
}
