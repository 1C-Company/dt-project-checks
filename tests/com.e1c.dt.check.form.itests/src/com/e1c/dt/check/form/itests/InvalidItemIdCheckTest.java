/*******************************************************************************
 * Copyright (C) 2021, 1C-Soft LLC and others.
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
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.common.StringUtils;
import com._1c.g5.v8.dt.form.model.FormField;
import com._1c.g5.v8.dt.validation.marker.BmObjectMarker;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com._1c.g5.v8.dt.validation.marker.MarkerSeverity;
import com.e1c.dt.check.form.InvalidItemIdCheck;
import com.e1c.g5.v8.dt.testing.check.SingleProjectReadOnlyCheckTestBase;

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

}
