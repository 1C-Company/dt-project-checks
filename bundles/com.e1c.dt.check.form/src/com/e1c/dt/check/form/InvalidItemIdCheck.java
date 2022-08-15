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
package com.e1c.dt.check.form;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.event.BmChangeEvent;
import com._1c.g5.v8.bm.core.event.BmSubEvent;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com.e1c.dt.check.internal.form.IInvalidItemIdService;
import com.e1c.g5.v8.dt.check.CheckComplexity;
import com.e1c.g5.v8.dt.check.ICheckDefinition;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.components.IBasicCheckExtension;
import com.e1c.g5.v8.dt.check.components.TopObjectFilterExtension;
import com.e1c.g5.v8.dt.check.context.CheckContextCollectingSession;
import com.e1c.g5.v8.dt.check.context.OnModelFeatureChangeContextCollector;
import com.e1c.g5.v8.dt.check.context.OnModelObjectRemovalContextCollector;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.e1c.g5.v8.dt.check.settings.IssueType;
import com.google.inject.Inject;

/**
 * Checks that form items have valid identifiers.
 * <p/>
 * This implementation delegates actual verification to {@link IInvalidItemIdService}.
 * This class itself contains only the wiring into {@link com.e1c.g5.v8.dt.check.ICheck}
 * and the rest of the infrastructure that triggers validation.
 * <p/>
 * Since the check tries to find child items with duplicate identifiers, it has to walk through whole
 * child content. To make sure we do not try to walk the whole form when validating every form item,
 * the check specifies the form itself as an object to be validated.
 * When user changes identifier of an item or removes an item, we need to trigger form re-validation
 * because duplicates might have gone away. For example, first object with the same id (which did not have markers)
 * might have been deleted and we need to cleanup markers on a second object that has those markers.
 * For this purpose implementation uses {@link AdditionalRevalidationRules} extension to specify extra rules.
 * <p/>
 * This check will put a marker for each {@link FormItem} as target object with a
 * description of the issue (as specified by {@link IInvalidItemIdService}).
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdCheck
    extends BasicCheck
{

    /**
     * Service to delegate checking to.
     */
    private final IInvalidItemIdService invalidItemIdservice;

    /**
     * Creates new instance.
     * @param invalidItemIdService Service that will be used to delegate checks to. Must not be {@code null}.
     */
    @Inject
    public InvalidItemIdCheck(IInvalidItemIdService invalidItemIdService)
    {
        this.invalidItemIdservice = invalidItemIdService;
    }

    @Override
    public String getCheckId()
    {
        return IInvalidItemIdService.CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer configurationBuilder)
    {
        configurationBuilder.extension(new SkipBaseFormExtension())
            .extension(new TopObjectFilterExtension())
            .extension(new AdditionalRevalidationRules())
            .title(Messages.InvalidItemIdCheck_title)
            .description(Messages.InvalidItemIdCheck_description)
            .complexity(CheckComplexity.NORMAL)
            .severity(IssueSeverity.MAJOR)
            .issueType(IssueType.ERROR)
            .topObject(FormPackage.Literals.FORM)
            .checkTop();
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAcceptor, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        if (!(object instanceof Form))
        {
            // We will get all kinds of child content here due to call
            // to ICheckDefinition.addCheckedModelObjects()
            // in InvalidItemIdCheck.AdditionalRevalidationRules.configureContextCollector().
            // We're not insterested in this child content here and need to ignore all those calls.
            return;
        }
        if (progressMonitor.isCanceled())
        {
            return;
        }
        Form form = (Form)object;
        for (Entry<FormItem, String> itemAndMesage : invalidItemIdservice.validate(form).entrySet())
        {
            resultAcceptor.addIssue(itemAndMesage.getValue(), itemAndMesage.getKey(),
                FormPackage.Literals.FORM_ITEM__ID);
        }
    }

    /**
     * Finds parent form of the given object.
     *
     * @param bmObject Object for which to find parent form. Must not be {@code null}.
     * @return Parent form of the given object or an empty value
     * if there is no top object or top object is not a form. Never {@code null}.
     *
     * @see IBmObject#bmGetTopObject()
     */
    private static Optional<Form> findFormOf(IBmObject bmObject)
    {
        IBmObject topObject = bmObject.bmGetTopObject();
        return topObject instanceof Form ? Optional.of((Form)topObject) : Optional.empty();
    }

    /**
     * An extension that configures context collectors so that form is re-validated when child items change.
     *
     * When you register delete handler using {@link ICheckDefinition#addModelRemovalContextCollector}
     * then {@link OnModelObjectRemovalContextCollector#collectContextOnObjectRemoval()}
     * is not triggered when user deletes elements from a form. This is because those elements have
     * no URIs and for them delete handles are ignored.
     *
     * Instead, we rely on change handler both to detect when identifier of an item has been changed
     * thanks to Quick Fix and when user has deleted form items with duplicated identifiers.
     *
     * Since we do not place markers on the first duplicate but only on 2nd, 3rd and so on,
     * there are two cases for deletion:
     * <ol>
     * <li>User has deleted the first duplicate which had no markers.
     * Generic marker cleanup mechanism will try to remove markers from deleted item but there were none.
     * The markers were on the 2nd duplicate. Keeping them on this 2nd duplicate is incorrect since
     * the problem does not exist anymore. To fix the issue, we trigger re-validation of the form.</li>
     * <li>User has deleted 2nd, 3rd any further duplicate which had markers.
     * Here the generic mechanism responsible for cleaning markers from deleted items will trigger
     * and remove all markers from the deleted item. If there were just two duplicates then, correctly, the first
     * one had no markers and will have no markers. If there were more than two duplicates, the 3rd one will still
     * have markers, correctly.</li>
     * </ol>
     *
     * @see OnItemIdChangeTriggerFormValidation
     * @see OnItemRemovalTriggerFormValidation
     */
    private static class AdditionalRevalidationRules
        implements IBasicCheckExtension
    {
        /**
         * Classes that can contain {@link FormItem}`s.
         */
        private static final Set<EClass> CONTAINER_CLASSES = Set.of(
        // @formatter:off
            FormPackage.Literals.FORM_ITEM_CONTAINER,
            FormPackage.Literals.CONTEXT_MENU_HOLDER,
            FormPackage.Literals.COMMAND_BAR_HOLDER,
            FormPackage.Literals.EXTENDED_TOOLTIP_HOLDER,
            FormPackage.Literals.TABLE_HOLDER,
            FormPackage.Literals.ADDITION_CONTAINER,
            FormPackage.Literals.TOOLTIP_CONTAINER
            // @formatter:on
        );

        @Override
        public void configureContextCollector(ICheckDefinition definition)
        {
            IBasicCheckExtension.super.configureContextCollector(definition);
            // Without this call, we do not receive some of the events when item are deleted
            // by user using form editor.
            // With this call, we get parasite calls to check() with non-Form objects
            // which we need to suppress.
            // If G5V8DT-22389 is fixed
            // then the call can be removed and parasite calls suppression
            // should be removed from check().
            definition.addCheckedModelObjects(FormPackage.Literals.FORM, true, CONTAINER_CLASSES);
            //
            definition.addModelFeatureChangeContextCollector(new OnItemIdChangeTriggerFormValidation(),
                FormPackage.Literals.FORM_ITEM);
            // We use here separate instances to assign each one a distinct label for tracing and debug purposes.
            // If G5V8DT-22389 is fixed in a way that
            // each instance of the collector is called only once per-event
            // then additional optimisation should be done here:
            // Single instance should be created outside of loop and registered for all classes.
            OnModelFeatureChangeContextCollector onDelete = new OnItemRemovalTriggerFormValidation();
            CONTAINER_CLASSES
                .forEach(containerClass -> definition.addModelFeatureChangeContextCollector(onDelete, containerClass));
        }
    }

    /**
     * Triggers form re-validation when identifier changes in child item.
     *
     * This is needed because source object of the check is the form while target object of the marker is
     * specific form item. The check will not be triggered again automatically
     * upon id change unless we trigger it here manually.
     *
     * We'll try to be super safe and trigger form re-validation only if it was a form item whose id has
     * changed and old value is not the same as new value.
     */
    private static class OnItemIdChangeTriggerFormValidation
        implements OnModelFeatureChangeContextCollector
    {
        @Override
        public void collectContextOnFeatureChange(IBmObject bmObject, EStructuralFeature feature, BmSubEvent bmEvent,
            CheckContextCollectingSession contextSession)
        {
            boolean formItemIdHasChanged = FormPackage.Literals.FORM_ITEM__ID.equals(feature)
                && bmObject instanceof FormItem && bmEvent instanceof BmChangeEvent;
            if (formItemIdHasChanged)
            {
                BmChangeEvent changeEvent = (BmChangeEvent)bmEvent;
                for (Notification notification : changeEvent.getNotifications(FormPackage.Literals.FORM_ITEM__ID))
                {
                    boolean valueChanged = !Objects.equals(notification.getOldValue(), notification.getNewValue());
                    if (valueChanged)
                    {
                        findFormOf(bmObject).ifPresent(contextSession::addModelCheck);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Triggers form re-validation when a form item is removed.
     *
     * This implementation checks if an event contains a notification (for any of the features) that
     * <ul>
     * <li>a value has been removed and this old removed value has been of type {@link FormItem}</li>
     * <ul>
     * <li>one item has been deleted ({@link Notification#REMOVE})</li>
     * <li>multiple items have been deleted at once ({@link Notification#REMOVE_MANY})</li>
     * </ul>
     * </ul>
     * This is an alternative to watching for specific features of various containers like
     * <ul>{@link com._1c.g5.v8.dt.form.model.FormItemContainer#getItems()}
     * <li>{@link com._1c.g5.v8.dt.form.model.CommandBarHolder#getAutoCommandBar()}
     * <li>{@link com._1c.g5.v8.dt.form.model.ContextMenuHolder#getContextMenu()}
     * <li>{@link com._1c.g5.v8.dt.form.model.ExtendedTooltipHolder#getExtendedTooltip()}
     * <li>and so on
     * </ul>
     */
    private static class OnItemRemovalTriggerFormValidation
        implements OnModelFeatureChangeContextCollector
    {

        @Override
        public void collectContextOnFeatureChange(IBmObject bmObject, EStructuralFeature feature, BmSubEvent bmEvent,
            CheckContextCollectingSession contextSession)
        {
            if (!(bmEvent instanceof BmChangeEvent))
            {
                return;
            }
            BmChangeEvent changeEvent = (BmChangeEvent)bmEvent;
            for (List<Notification> notifications : changeEvent.getNotifications().values())
            {
                for (Notification notification : notifications)
                {
                    if (isOneFormItemRemoved(notification) || isManyFormItemsRemoved(notification))
                    {
                        findFormOf(bmObject).ifPresent(contextSession::addModelCheck);
                        return;
                    }
                }
            }
        }

        /**
         * Checks if notification indicates that one form item has been removed.
         *
         * @param notification Notification to check. Must not be {@code null}.
         * @return {@code true} if notification indicates that one object has been removed and it was a form item.
         */
        private boolean isOneFormItemRemoved(Notification notification)
        {
            return notification.getEventType() == Notification.REMOVE && notification.getOldValue() instanceof FormItem;
        }

        /**
         * Checks if notification indicates that a form item has been deleted among other multiple objects.
         *
         * @param notification Notification to check. Must not be {@code null}.
         * @return {@code true} if notification indicates that multiple items have been removed and a form
         * item was among them.
         */
        private boolean isManyFormItemsRemoved(Notification notification)
        {
            if (notification.getEventType() != Notification.REMOVE_MANY
                || !(notification.getOldValue() instanceof Collection))
            {
                return false;
            }
            for (Object removedObject : (Collection<?>)notification.getOldValue())
            {
                if (removedObject instanceof FormItem)
                {
                    return true;
                }
            }
            return false;
        }
    }

}
