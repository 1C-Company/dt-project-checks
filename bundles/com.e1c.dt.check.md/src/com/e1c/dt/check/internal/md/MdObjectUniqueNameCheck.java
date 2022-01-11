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
package com.e1c.dt.check.internal.md;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import com._1c.g5.v8.dt.core.platform.IConfigurationProvider;
import com._1c.g5.v8.dt.metadata.IMdAnnotations;
import com._1c.g5.v8.dt.metadata.mdclass.Configuration;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.metadata.mdclass.Subsystem;
import com._1c.g5.v8.dt.metadata.mdclass.impl.ValidationUtil;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.components.TopObjectFilterExtension;
import com.e1c.g5.v8.dt.check.settings.IssueType;
import com.google.inject.Inject;

/**
 * Md-oObject unique name check.
 *
 * @author Andrey Volkov
 */
public final class MdObjectUniqueNameCheck
    extends BasicCheck
{
    @Inject
    private IConfigurationProvider configurationProvider;

    /**
     * Check Id.
     */
    public static final String CHECK_ID = "md-object-unique-name"; //$NON-NLS-1$

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        builder.title(Messages.MdObjectUniqueNameCheck_Title)
            .description(Messages.MdObjectUniqueNameCheck_Description)
            .extension(new TopObjectFilterExtension())
            .issueType(IssueType.ERROR)
            .topObject(MdClassPackage.Literals.MD_OBJECT)
            .features(MdClassPackage.Literals.MD_OBJECT__NAME);
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAceptor, ICheckParameters parameters,
        IProgressMonitor progressMonitor)
    {
        MdObject mdObject = (MdObject)object;

        if (!mdObject.eIsProxy())
        {
            EObject eContainer = mdObject.eContainer();
            if (eContainer == null && !(mdObject instanceof Configuration))
            {
                Configuration configuration = configurationProvider.getConfiguration(mdObject);
                if (configuration != null)
                {
                    EReference mdObjectCollectionFeature = getMdObjectCollectionFeature(mdObject.eClass());
                    if (mdObjectCollectionFeature != null)
                    {
                        List<MdObject> checkedObjects = getMdObjectList(configuration, mdObjectCollectionFeature);
                        String name = ValidationUtil.getName(mdObject);
                        String lowerCaseName = name.toLowerCase();
                        for (MdObject checkedObject : checkedObjects)
                        {
                            if (!checkedObject.equals(mdObject)
                                && lowerCaseName.equals(ValidationUtil.getName(checkedObject).toLowerCase()))
                            {
                                if (mdObject instanceof Subsystem)
                                {
                                    Subsystem mdObjectParentSubsystem = ((Subsystem)mdObject).getParentSubsystem();
                                    Subsystem checkedObjectParentSubsystem =
                                        ((Subsystem)checkedObject).getParentSubsystem();
                                    if ((mdObjectParentSubsystem != null
                                        && !mdObjectParentSubsystem.equals(checkedObjectParentSubsystem))
                                        || (mdObjectParentSubsystem == null && checkedObjectParentSubsystem == null))
                                    {
                                        continue;
                                    }
                                }
                                resultAceptor.addIssue(
                                    MessageFormat.format(
                                        Messages.MdObjectUniqueNameCheck__Invalid_property__0__duplicate_name__1,
                                        MdClassPackage.Literals.MD_OBJECT__NAME.getName(), name),
                                    mdObject, MdClassPackage.Literals.MD_OBJECT__NAME);
                                break;
                            }
                        }
                    }
                }
            }
            for (EReference reference : mdObject.eClass().getEAllContainments())
            {
                if (reference.isMany())
                {
                    EClass eType = (EClass)reference.getEType();
                    if (MdClassPackage.Literals.MD_OBJECT.isSuperTypeOf(eType))
                    {
                        Map<String, List<MdObject>> lowerCaseNames = new HashMap<>();
                        for (MdObject subobject : getMdObjectList(mdObject, reference))
                        {
                            String lowerCaseName = ValidationUtil.getName(subobject).toLowerCase();
                            if (!lowerCaseNames.containsKey(lowerCaseName))
                            {
                                lowerCaseNames.put(lowerCaseName, new ArrayList<>());
                            }
                            lowerCaseNames.get(lowerCaseName).add(subobject);
                        }
                        for (List<MdObject> duplicates : lowerCaseNames.values())
                        {
                            if (duplicates.size() > 1)
                            {
                                for (MdObject subobject : duplicates)
                                {
                                    resultAceptor.addIssue(
                                        MessageFormat.format(
                                            Messages.MdObjectUniqueNameCheck__Invalid_property__0__duplicate_name__1,
                                            MdClassPackage.Literals.MD_OBJECT__NAME.getName(),
                                            ValidationUtil.getName(subobject)),
                                        subobject, MdClassPackage.Literals.MD_OBJECT__NAME);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<MdObject> getMdObjectList(MdObject mdObject, EReference reference)
    {
        return (List<MdObject>)mdObject.eGet(reference);
    }

    private EReference getMdObjectCollectionFeature(EClass eClass)
    {
        for (EReference eReference : MdClassPackage.Literals.CONFIGURATION.getEAllReferences())
        {
            if (eReference.isMany() && !eReference.isContainment()
                && eReference.getEAnnotation(IMdAnnotations.MD_CLASS) != null && eReference.getEType() == eClass)
            {
                return eReference;
            }
        }
        return null;
    }
}
