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
 *     1C-Soft LLC - initial implementation
 *******************************************************************************/
package com.e1c.dt.check.internal.md;

import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.COMMON_ATTRIBUTE;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.COMMON_ATTRIBUTE_CONTENT_ITEM;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.COMMON_ATTRIBUTE_CONTENT_ITEM__METADATA;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__ACCOUNTING_REGISTERS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__ACCUMULATION_REGISTERS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__BOTS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__BUSINESS_PROCESSES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__CALCULATION_REGISTERS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__CATALOGS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__CHARTS_OF_ACCOUNTS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__CHARTS_OF_CALCULATION_TYPES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__CHARTS_OF_CHARACTERISTIC_TYPES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMAND_GROUPS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_ATTRIBUTES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_COMMANDS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_FORMS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_MODULES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_PICTURES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_SETTINGS_STORAGE;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__COMMON_TEMPLATES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__CONSTANTS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__DATA_PROCESSORS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__DOCUMENTS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__DOCUMENT_JOURNALS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__DOCUMENT_NUMERATORS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__ENUMS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__EVENT_SUBSCRIPTIONS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__EXCHANGE_PLANS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__EXTERNAL_DATA_SOURCES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__FILTER_CRITERIA;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__FUNCTIONAL_OPTIONS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__FUNCTIONAL_OPTIONS_PARAMETERS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__HTTP_SERVICES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__INFORMATION_REGISTERS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__INTEGRATION_SERVICES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__REPORTS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__ROLES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__SCHEDULED_JOBS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__SEQUENCES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__SESSION_PARAMETERS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__SETTINGS_STORAGES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__STYLE_ITEMS;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.EXCHANGE_PLAN;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.EXCHANGE_PLAN_CONTENT_ITEM;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.EXCHANGE_PLAN_CONTENT_ITEM__MD_OBJECT;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.SUBSYSTEM;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.SUBSYSTEM__CONTENT;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.SUBSYSTEM__SUBSYSTEMS;
import static com._1c.g5.v8.dt.scc.model.StandaloneContentPackage.Literals.STANDALONE_CONTENT;
import static com._1c.g5.v8.dt.scc.model.StandaloneContentPackage.Literals.STANDALONE_CONTENT_MD_OBJECT_AWARE__METADATA;
import static com._1c.g5.v8.dt.scc.model.StandaloneContentPackage.Literals.STANDALONE_CONTENT_PRIORITY_ITEM;
import static com._1c.g5.v8.dt.scc.model.StandaloneContentPackage.Literals.STANDALONE_CONTENT_UNUSED_ITEM;
import static com._1c.g5.v8.dt.scc.model.StandaloneContentPackage.Literals.STANDALONE_CONTENT_USED_ITEM;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com._1c.g5.v8.bm.core.IBmCrossReference;
import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.core.event.BmAssociationEvent;
import com._1c.g5.v8.bm.core.event.BmSubEvent;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com.e1c.g5.v8.dt.check.EIssue;
import com.e1c.g5.v8.dt.check.ICheck;
import com.e1c.g5.v8.dt.check.ICheckDefinition;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.ICheckResultAcceptor;
import com.e1c.g5.v8.dt.check.context.CheckContextCollectingSession;
import com.e1c.g5.v8.dt.check.context.OnModelFeatureChangeContextCollector;
import com.e1c.g5.v8.dt.check.context.OnModelObjectAssociationContextCollector;
import com.e1c.g5.v8.dt.check.context.OnModelObjectRemovalContextCollector;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;

/**
 * MD object referential integrity checks. Checks all important relations which are required by the 1C:Enterprise to
 * public updates into the IB
 *
 * @author Alexander Tretyakevich
 */
public final class MdReferenceIntegrity
    implements ICheck
{
    // @formatter:off
    private static final Map<EClass, CheckDef> CHECKED_COLLECTIONS = Map.of(
        SUBSYSTEM, new CheckDef(Set.of(SUBSYSTEM__CONTENT, SUBSYSTEM__SUBSYSTEMS)),
        CONFIGURATION, new CheckDef(Set.of(
            CONFIGURATION__ACCOUNTING_REGISTERS,
            CONFIGURATION__ACCUMULATION_REGISTERS,
            CONFIGURATION__BOTS,
            CONFIGURATION__BUSINESS_PROCESSES,
            CONFIGURATION__CALCULATION_REGISTERS,
            CONFIGURATION__CATALOGS,
            CONFIGURATION__CHARTS_OF_ACCOUNTS,
            CONFIGURATION__CHARTS_OF_CALCULATION_TYPES,
            CONFIGURATION__CHARTS_OF_CHARACTERISTIC_TYPES,
            CONFIGURATION__COMMAND_GROUPS,
            CONFIGURATION__COMMON_ATTRIBUTES,
            CONFIGURATION__COMMON_COMMANDS,
            CONFIGURATION__COMMON_FORMS,
            CONFIGURATION__COMMON_MODULES,
            CONFIGURATION__COMMON_PICTURES,
            CONFIGURATION__COMMON_SETTINGS_STORAGE,
            CONFIGURATION__COMMON_TEMPLATES,
            CONFIGURATION__CONSTANTS,
            CONFIGURATION__DATA_PROCESSORS,
            CONFIGURATION__DOCUMENT_JOURNALS,
            CONFIGURATION__DOCUMENT_NUMERATORS,
            CONFIGURATION__DOCUMENTS,
            CONFIGURATION__ENUMS,
            CONFIGURATION__EVENT_SUBSCRIPTIONS,
            CONFIGURATION__EXCHANGE_PLANS,
            CONFIGURATION__EXTERNAL_DATA_SOURCES,
            CONFIGURATION__FILTER_CRITERIA,
            CONFIGURATION__FUNCTIONAL_OPTIONS,
            CONFIGURATION__FUNCTIONAL_OPTIONS_PARAMETERS,
            CONFIGURATION__HTTP_SERVICES,
            CONFIGURATION__INFORMATION_REGISTERS,
            CONFIGURATION__INTEGRATION_SERVICES,
            CONFIGURATION__REPORTS,
            CONFIGURATION__ROLES,
            CONFIGURATION__SCHEDULED_JOBS,
            CONFIGURATION__SEQUENCES,
            CONFIGURATION__SESSION_PARAMETERS,
            CONFIGURATION__SETTINGS_STORAGES,
            CONFIGURATION__STYLE_ITEMS)),
        COMMON_ATTRIBUTE, new CheckDef(Map.of(
            COMMON_ATTRIBUTE_CONTENT_ITEM,
                Set.of(COMMON_ATTRIBUTE_CONTENT_ITEM__METADATA))),
        EXCHANGE_PLAN, new CheckDef(Map.of(
            EXCHANGE_PLAN_CONTENT_ITEM,
                Set.of(EXCHANGE_PLAN_CONTENT_ITEM__MD_OBJECT))),
        STANDALONE_CONTENT, new CheckDef(Map.of(
            STANDALONE_CONTENT_USED_ITEM,
                Set.of(STANDALONE_CONTENT_MD_OBJECT_AWARE__METADATA),
            STANDALONE_CONTENT_UNUSED_ITEM,
                Set.of(STANDALONE_CONTENT_MD_OBJECT_AWARE__METADATA),
            STANDALONE_CONTENT_PRIORITY_ITEM,
                Set.of(STANDALONE_CONTENT_MD_OBJECT_AWARE__METADATA))));
    // @formatter:on

    @Override
    public void check(Object object, ICheckResultAcceptor resultRegistrar, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        if (!(object instanceof IBmObject))
        {
            return;
        }

        IBmObject bmObject = (IBmObject)object;
        IBmObject topBmObject = bmObject.bmGetTopObject();
        boolean isTopTarget = topBmObject.bmGetId() == bmObject.bmGetId();

        CheckDef checkDef = CHECKED_COLLECTIONS.get(topBmObject.eClass());
        if (checkDef == null)
        {
            return;
        }

        Collection<EStructuralFeature> checkedFeatures;
        if (isTopTarget)
        {
            checkedFeatures = checkDef.features;
        }
        else
        {
            checkedFeatures = checkDef.containments.get(bmObject.eClass());
        }

        if (checkedFeatures != null)
        {
            for (EStructuralFeature feature : checkedFeatures)
            {
                checkMdObjectReferences((EObject)object, feature, resultRegistrar);
            }
        }
    }

    @Override
    public void configureContextCollector(ICheckDefinition definition)
    {
        // Added to track all removals/additions of top MD objects
        definition.addCheckedModelObjects(MdClassPackage.Literals.MD_OBJECT, true, Collections.emptySet());

        // Add explicit tracking and checking for collections
        for (Entry<EClass, CheckDef> entry : CHECKED_COLLECTIONS.entrySet())
        {
            EClass topObjectEClass = entry.getKey();
            CheckDef checkDef = entry.getValue();

            if (checkDef.containments != null)
            {
                // Add tracked containment types
                definition.addCheckedModelObjects(topObjectEClass, checkDef.features != null,
                    checkDef.containments.keySet());
            }
        }

        definition.addModelAssociationContextCollector(new ObjectAssociationChangeContextCollector(),
            MdClassPackage.Literals.MD_OBJECT);
        definition.addModelFeatureChangeContextCollector(new ObjectCollectionFeatureChangeContextCollector(),
            MdClassPackage.Literals.MD_OBJECT);
        definition.addModelRemovalContextCollector(new ObjectRemovalContextCollector(),
            MdClassPackage.Literals.MD_OBJECT);

        definition.setTitle(Messages.MdReferenceIntegrity_Title);
        definition.setDescription(Messages.MdReferenceIntegrity_Description);
        definition.setDefaultSeverity(IssueSeverity.CRITICAL);
    }

    @Override
    public String getCheckId()
    {
        return "md-reference-intergrity"; //$NON-NLS-1$
    }

    /*
     * Checks the specified collection of MD Objects being specified using the profided structural feature
     */
    @SuppressWarnings("unchecked")
    private void checkMdObjectReferences(EObject object, EStructuralFeature feature,
        ICheckResultAcceptor resultRegistrar)
    {
        if (feature.isMany())
        {
            EList<? extends EObject> objects = (EList<? extends EObject>)object.eGet(feature);
            if (objects != null)
            {
                int idx = 0;
                for (EObject collectionObject : objects)
                {
                    if (collectionObject.eIsProxy())
                    {
                        // The lost reference is found - need to report
                        EIssue issue = new EIssue(MessageFormat.format(Messages.MdReferenceIntegrity_LostReference_Many,
                            feature.getName(), EcoreUtil.getURI(collectionObject), idx), feature, idx);
                        resultRegistrar.addIssue(object, issue);
                    }

                    idx++;
                }
            }
        }
        else
        {
            EObject value = (EObject)object.eGet(feature);
            if (value != null && value.eIsProxy())
            {
                // The lost reference is found - need to report
                EIssue issue = new EIssue(MessageFormat.format(Messages.MdReferenceIntegrity_LostReference_Single,
                    feature.getName(), EcoreUtil.getURI(value), 0), feature);
                resultRegistrar.addIssue(object, issue);
            }
        }
    }

    /*
     * Schedules the check for the back reference holder in case if collections of this holder are being
     * checked by this check
     */
    private static void scheduleCheckIfFeaturesAreTracked(URI targetUri, IBmTransaction bmTransaction,
        CheckContextCollectingSession contextSession)
    {
        Collection<IBmCrossReference> references = bmTransaction.getReferences(targetUri);
        if (!references.isEmpty())
        {
            for (IBmCrossReference reference : references)
            {
                IBmObject referenceHolder = reference.getObject();
                if (referenceHolder == null)
                {
                    // The object may be removed at the time the processing is being done so we simply ignore it,
                    // as removed object is not a target for this check
                    continue;
                }

                IBmObject topBmObject = referenceHolder.bmGetTopObject();
                boolean isTopTarget = topBmObject.bmGetId() == referenceHolder.bmGetId();
                CheckDef checkDef = CHECKED_COLLECTIONS.get(topBmObject.eClass());
                if (checkDef == null)
                {
                    continue;
                }

                Collection<EStructuralFeature> checkedFeatures;
                if (isTopTarget)
                {
                    checkedFeatures = checkDef.features;
                }
                else
                {
                    checkedFeatures = checkDef.containments.get(referenceHolder.eClass());
                }

                if (checkedFeatures != null && checkedFeatures.contains(reference.getFeature()))
                {
                    // The back reference is checkable - we need to schedule the check for the corresponding
                    // top object
                    contextSession.addModelCheck(referenceHolder);
                }
            }
        }
    }

    /*
     * Re-schedules validation for MD objects which hold references to the attached object.
     * It's used to clear previous findings of state references in case if they are fixed by this association
     */
    private static final class ObjectAssociationChangeContextCollector
        implements OnModelObjectAssociationContextCollector
    {

        @Override
        public void collectContextOnObjectAssociation(IBmObject bmObject, BmSubEvent bmEvent,
            CheckContextCollectingSession contextSession)
        {
            if (!bmObject.bmIsTop())
            {
                // At the moment we are checking only top object references
            }

            IBmTransaction bmTransaction = bmObject.bmGetEngine().getCurrentTransaction();
            scheduleCheckIfFeaturesAreTracked(EcoreUtil.getURI(bmObject), bmTransaction, contextSession);
        }
    }

    /*
     * Re-schedules validation for MD objects which are potential holders of stale references in cases then
     * checked collections are changed. It's used to update markers with proper indexes of clearing in case if
     * the stale refernce is removed manually
     */
    private static final class ObjectCollectionFeatureChangeContextCollector
        implements OnModelFeatureChangeContextCollector
    {
        @Override
        public void collectContextOnFeatureChange(IBmObject bmObject, EStructuralFeature feature, BmSubEvent bmEvent,
            CheckContextCollectingSession contextSession)
        {
            IBmObject topBmObject = bmObject.bmGetTopObject();
            CheckDef checkDef = CHECKED_COLLECTIONS.get(topBmObject.eClass());
            if (checkDef == null)
            {
                return;
            }

            boolean isTopTarget = topBmObject.bmGetId() == bmObject.bmGetId();

            Collection<EStructuralFeature> features;
            if (isTopTarget)
            {
                features = checkDef.features;
            }
            else
            {
                features = checkDef.containments.get(bmObject.eClass());
            }

            if (features != null && features.contains(feature))
            {
                contextSession.addModelCheck(bmObject.bmGetTopObject());
            }
        }
    }

    /*
     * Schedules the check for all holders of leftover references of the removed object.
     * Only checked references are being taken into the account
     */
    private static final class ObjectRemovalContextCollector
        implements OnModelObjectRemovalContextCollector
    {
        @Override
        public void collectContextOnObjectRemoval(URI removedObjectUri, EClass removedObjectEClass, BmSubEvent bmEvent,
            CheckContextCollectingSession contextSession, IBmTransaction bmTransaction)
        {
            if (!(bmEvent instanceof BmAssociationEvent))
            {
                return;
            }

            scheduleCheckIfFeaturesAreTracked(removedObjectUri, bmTransaction, contextSession);
        }
    }

    /*
     * Check scope definition for the top object EClass
     */
    private static final class CheckDef
    {
        private final Collection<EStructuralFeature> features;
        private final Map<EClass, Collection<EStructuralFeature>> containments;

        CheckDef(Collection<EStructuralFeature> features)
        {
            this.features = features;
            containments = Collections.emptyMap();
        }

        CheckDef(Map<EClass, Collection<EStructuralFeature>> containments)
        {
            this.features = null;
            this.containments = containments;
        }
    }
}
