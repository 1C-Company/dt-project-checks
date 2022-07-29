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
package com.e1c.dt.check.form.itests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.resources.IProject;
import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.common.StringUtils;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.form.model.FormField;
import com._1c.g5.v8.dt.mcore.NamedElement;
import com._1c.g5.v8.dt.validation.marker.BmObjectMarker;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com._1c.g5.v8.dt.validation.marker.MarkerSeverity;
import com.e1c.dt.check.form.InvalidItemIdCheck;
import com.e1c.g5.v8.dt.testing.check.SingleProjectReadOnlyCheckTestBase;
import com.google.common.base.Preconditions;

/**
 * Tests for {@link InvalidItemIdCheck} check.
 *
 * @author Nikolay Martynov
 */
public class InvalidItemIdCheckTest
    extends SingleProjectReadOnlyCheckTestBase
{
    /**
     * Identifier of check under test.
     *
     * Synchronize it with {@code CHECK_ID} in {@link InvalidItemIdCheck}.
     */
    private static final String CHECK_ID = "form-invalid-item-id";

    /**
     * Test that correct form does not cause markers.
     *
     * The test expects that when new form is created then it is correct. At least, in terms of identifiers
     * being present and having correct values.
     *
     * @throws Exception When not all elements have markers or markers do not have specific
     * form element set as their target.
     */
    @Test
    public void testDefaultFormNoIssues() throws Exception
    {
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.DefaultListForm.Form", getProject());
        List<Marker> markers = streamAllMarkers(getWorkspaceProject().get()).filter(byForm(form))
            .filter(byNaturalCheckId(CHECK_ID, getWorkspaceProject().get()))
            .collect(Collectors.toList());
        assertEquals("Markers found", 0, markers.size());
    }

    /**
     * Test that when Form`s immeddiate child item has id set to 0 then a marker will be added.
     *
     * @throws Exception When no marker is detected.
     */
    @Test
    public void testId0ImmediateChildCausesIssue() throws Exception
    {
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.Id0ImmediateChild.Form", getProject());
        Optional<Marker> marker = streamAllMarkers(getWorkspaceProject().get()).filter(byForm(form))
            .filter(byTarget((FormField)findChildByName(form, "Code")))
            .filter(byNaturalCheckId(CHECK_ID, getWorkspaceProject().get()))
            .findFirst();
        assertMarkerIsCorrect(marker);
    }

    /**
     * Test that when Form`s nested child item has id set to 0 then a marker will be added.
     *
     * @throws Exception When no marker is detected.
     */
    @Test
    public void testId0NestedChildCausesIssue() throws Exception
    {
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.Id0NestedChild.Form", getProject());
        Optional<Marker> marker = streamAllMarkers(getWorkspaceProject().get()).filter(byForm(form))
            .filter(byTarget(((FormField)findChildByName(form, "Code")).getContextMenu()))
            .filter(byNaturalCheckId(CHECK_ID, getWorkspaceProject().get()))
            .findFirst();
        assertMarkerIsCorrect(marker);
    }

    /**
     * Test that when Form`s child item does not have id element then a marker will be added.
     *
     * @throws Exception When no marker is detected.
     */
    @Test
    public void testMissingIdCausesIssue() throws Exception
    {
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.Id0ImmediateChild.Form", getProject());
        Optional<Marker> marker = streamAllMarkers(getWorkspaceProject().get()).filter(byForm(form))
            .filter(byTarget((FormField)findChildByName(form, "Code")))
            .filter(byNaturalCheckId(CHECK_ID, getWorkspaceProject().get()))
            .findFirst();
        assertMarkerIsCorrect(marker);
    }

    /**
     * Test that when Form`s child item has a negative id value then no marker is added.
     *
     * @throws Exception When a marker is detected.
     */
    @Test
    public void testNegativeIdCausesNoIssue() throws Exception
    {
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.NegativeId.Form", getProject());
        Optional<Marker> marker = streamAllMarkers(getWorkspaceProject().get()).filter(byForm(form))
            .filter(byTarget((FormField)findChildByName(form, "Code")))
            .filter(byNaturalCheckId(CHECK_ID, getWorkspaceProject().get()))
            .findFirst();
        assertTrue("Marker found", marker.isEmpty());
    }

    /**
     * Test that when there are multiple elements on a form with a missing identifier then
     * there is a marker for each of the elements.
     *
     * @throws Exception When not all elements have markers or markers do not have specific
     * form element set as their target.
     */
    @Test
    public void testMultipleMissingIdsCauseSeparateIssues() throws Exception
    {
        int brokenElementsCount = 22;
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.MissingIdMultiple.Form", getProject());
        List<Marker> markers = streamAllMarkers(getWorkspaceProject().get()).filter(byForm(form))
            .filter(byNaturalCheckId(CHECK_ID, getWorkspaceProject().get()))
            .collect(Collectors.toList());
        assertEquals("Markers count", brokenElementsCount, markers.size());
        assertEquals("Not all markers target their own object", brokenElementsCount,
            markers.stream()
                .filter(BmObjectMarker.class::isInstance)
                .map(BmObjectMarker.class::cast)
                .map(marker -> marker.getObjectId())
                .distinct()
                .count());
    }

    /**
     * Test that when Form`s child items have duplicate identifiers then each duplicated item except the first one
     * has a marker.
     *
     * @throws Exception When first duplicated element has a marker
     * or some of the consequent elements do not have a marker.
     */
    @Test
    public void testDuplicateIdCausesIssue() throws Exception
    {
        int duplicateElementsCount = 3;
        IBmObject form = getTopObjectByFqn("Catalog.Catalog.Form.BadMerge.Form", getProject());
        FormField user1Field = (FormField)findChildByName(form, "DescriptionByUser1");
        Marker[] originalMarkers = markerManager.getNestedMarkers(getWorkspaceProject().get(), user1Field.bmGetId());
        assertEquals("Should be no markers on first/original field", 0, originalMarkers.length);
        FormField user2Field = (FormField)findChildByName(form, "DescriptionByUser2");
        Marker[] duplicateMarkers = markerManager.getNestedMarkers(getWorkspaceProject().get(), user2Field.bmGetId());
        assertEquals("Each duplicated field should have a marker", duplicateElementsCount, duplicateMarkers.length);
        Arrays.stream(duplicateMarkers).forEach(marker -> assertMarkerIsCorrect(Optional.of(marker)));
    }

    @Override
    protected String getTestConfigurationName()
    {
        return InvalidItemIdCheck.class.getSimpleName();
    }

    /**
     * Asserts that marker context is as expected.
     *
     * Expected means that:
     * <ul>
     * <li>marker is not {@code null}
     * <li>originates from {@link #CHECK_ID}
     * <li>indicates a major issue
     * <li>has non-blank message
     * <li>specifies identifier as a feature
     * </ul>
     *
     * @param optionalMarker Marker to be checked.
     * @throws AssertionError if expected conditions are not met.
     */
    private void assertMarkerIsCorrect(Optional<Marker> optionalMarker)
    {
        Marker marker = optionalMarker.orElseThrow(() -> new AssertionError("Marker not found"));
        assertEquals("Severity is unexpected", MarkerSeverity.MAJOR, marker.getSeverity());
        assertTrue("Check is unexpected",
            checkRepository.toUid(CHECK_ID, getProject().getWorkspaceProject())
                .stream()
                .map(uid -> checkRepository.getShortUid(uid, getProject()))
                .collect(Collectors.toSet())
                .contains(marker.getCheckId()));
        assertFalse("Message is blank", StringUtils.isBlank(marker.getMessage()));
        assertEquals("Target feature is not identifier",
            com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM__ID.getFeatureID(), marker.getFeatureId());
    }

    /**
     * Finds first child with the specified name.
     *
     * <p/>
     * Usage:
     * <pre><code>
     * IBmObject form = getTopObjectByFqn("Catalog.Catalog1.Form.Form1.Form", project);
     * FormField field1 = (FormField)findChildByName(form, "Field1");
     * </code></pre>
     *
     * @param parent Parent where to search child with the specified name. Must not be {@code null}.
     * @param name Name of the child to search for. May be {@code null}.
     * @return Child item with the specified name. Never {@code null}.
     * @throws NoSuchElementException if child with the specified name does not exist.
     */
    private NamedElement findChildByName(IBmObject parent, String name)
    {
        Preconditions.checkNotNull(parent, "parent"); //$NON-NLS-1$
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(parent.eAllContents(), 0), false)
            .filter(NamedElement.class::isInstance)
            .map(NamedElement.class::cast)
            .filter(v -> Objects.equals(name, v.getName()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(String.valueOf(name)));
    }

    /**
     * Creates stream of all validation markers for the specified project.
     *
     * @param project Project to get markers from. Must not be {@code null}.
     * @return Stream of all markers from the specified project. Never {@code null}.
     */
    private Stream<Marker> streamAllMarkers(IProject project)
    {
        Preconditions.checkNotNull(project, "project"); //$NON-NLS-1$
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(markerManager.iterator(project), 0), false);
    }

    /**
     * Tests if a marker corresponds to the specified form.
     *
     * When issue is added to an element of a form then
     * <ul>
     * <li>{@link Marker#getSourceObjectId()} points to whole form
     * via {@link com._1c.g5.v8.bm.core.Form#bmGetId()}
     * <li>and {@link BmObjectMarker#getObjectId()} points to target element of a form
     * via {@link com._1c.g5.v8.dt.form.model.FormItem#getId()}.
     *</ul>
     *
     * This predicate checks if a marker originates from the specified form regardless of its target element.
     *
     * @param form Form to test for. Must not be null.
     * @return Predicate that matches markers by the specified form. Never {@code null}.
     */
    private Predicate<Marker> byForm(IBmObject form)
    {
        Preconditions.checkNotNull(form, "form"); //$NON-NLS-1$
        return marker -> Objects.equals(marker.getSourceObjectId(), form.bmGetId());
    }

    /**
     * Tests if a marker corresponds to the specified target element of a form.
     *
     * When issue is added to an element of a form then
     * <ul>
     * <li>{@link Marker#getSourceObjectId()} points to whole form
     * via {@link com._1c.g5.v8.bm.core.Form#bmGetId()}
     * <li>and {@link BmObjectMarker#getObjectId()} points to target element of a form
     * via {@link com._1c.g5.v8.dt.form.model.FormItem#getId()}.
     *</ul>
     *
     * This predicate checks if a marker originates from the specified target element of a form.
     *
     * Note that you might also need to filter by a form as well to avoid getting markers from multiple forms
     * in case forms have elements with the same identifiers.
     *
     * @param target Target form element to test for. Must not be {@code null}.
     * @return Predicate that matches markers of type {@link BmObjectMarker} that have been added with
     * the specified form element as a target. Never {@code null}.
     */
    private Predicate<Marker> byTarget(IBmObject target)
    {
        Preconditions.checkNotNull(target, "target"); //$NON-NLS-1$
        return marker -> marker instanceof BmObjectMarker
            && Objects.equals(((BmObjectMarker)marker).getObjectId(), target.bmGetId());
    }

    /**
     * Tests if marker corresponds to the specified check.
     *
     * {@link Marker#getCheckId()} returns short version of identifier that is not the same as specified
     * in an implementation of the corresponding check. This method will convert specified
     * natural check identifier to short identifier before matching markers.
     *
     * @param naturalCheckId Natural (long, as specified in implementation of check`s class) identifier of the check.
     * Must not be {@code null}.
     * @param project Project being tested. Must not be {@code null}.
     * @return Predicate that matches markers originating from the specified check. Never {@code null}.
     */
    private Predicate<Marker> byNaturalCheckId(String naturalCheckId, IProject project)
    {
        Preconditions.checkNotNull(naturalCheckId, "naturalCheckId"); //$NON-NLS-1$
        Preconditions.checkNotNull(project, "project"); //$NON-NLS-1$
        Set<String> checkUids = checkRepository.toUid(naturalCheckId, project)
            .stream()
            .map(uid -> checkRepository.getShortUid(uid, project))
            .collect(Collectors.toSet());
        return marker -> checkUids.contains(marker.getCheckId());
    }

    /**
     * Gets Eclipse project associated with the current test case.
     *
     * @return Optional project that could be empty when called outside of
     * {@linkplain} junit.framework.Test}-annotated methods or
     * DT project has no associated Eclipse project.
     *
     * @see IDtProject#getWorkspaceProject()
     */
    private Optional<IProject> getWorkspaceProject()
    {
        return Optional.ofNullable(getProject()).map(dtProject -> dtProject.getWorkspaceProject());
    }

}
