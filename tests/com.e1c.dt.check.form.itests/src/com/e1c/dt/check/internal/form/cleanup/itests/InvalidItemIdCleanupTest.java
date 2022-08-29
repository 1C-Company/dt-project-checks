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
package com.e1c.dt.check.internal.form.cleanup.itests;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com._1c.g5.v8.dt.core.operations.ProjectPipelineJob;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IDerivedDataManagerProvider;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IDtProjectManager;
import com._1c.g5.v8.dt.core.platform.IWorkspaceOrchestrator;
import com._1c.g5.v8.dt.migration.cleanup.ICleanUpProjectSourcesManager;
import com._1c.g5.v8.dt.platform.version.Version;
import com._1c.g5.v8.dt.testing.GuiceModules;
import com._1c.g5.v8.dt.testing.JUnitGuiceRunner;
import com._1c.g5.v8.dt.testing.TestingPlatformSupport;
import com._1c.g5.v8.dt.testing.TestingWorkspace;
import com._1c.g5.wiring.AbstractServiceAwareModule;
import com.e1c.dt.check.internal.form.CorePlugin;
import com.e1c.dt.check.internal.form.cleanup.InvalidItemIdCleanup;
import com.google.inject.Inject;

/**
 * Tests for {@link InvalidItemIdCleanup}.
 */
@RunWith(JUnitGuiceRunner.class)
@GuiceModules(modules = InvalidItemIdCleanupTest.Dependencies.class)
public class InvalidItemIdCleanupTest
{
    /**
     * XPath to get id-values of all non-attributes.
     */
    private static final String ALL_NON_ATTRIBUTE_ID_TEXT = "//id[not(parent::*[local-name()='attributes'])]/text()";

    @ClassRule
    public static final TestingPlatformSupport testingPlatformSupport = new TestingPlatformSupport(Version.V8_3_19);

    @Rule
    public TestingWorkspace testingWorkspace = new TestingWorkspace(false, true);

    @Inject
    private IWorkspaceOrchestrator workspaceOrchestrator;

    @Inject
    private IDtProjectManager dtProjectManager;

    @Inject
    private ICleanUpProjectSourcesManager cleanupService;

    private IProject testProject;

    @Before
    public void loadTestProject() throws CoreException
    {
        testProject = testingWorkspace.setUpProject("InvalidItemIdCheck", getClass());
    }

    /**
     * Test that correct form does not receive modifications.
     *
     * The test expects that when new form is created then it is correct. At least, in terms of identifiers
     * being present and having correct values.
     *
     * @throws Exception When some of the identifiers have changed their values.
     */
    @Test
    public void testDefaultFormNoChanges()
    {
        String file = "src/Catalogs/Catalog/Forms/DefaultListForm/Form.form";
        List<String> beforeIds =
            xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(loadProjectPathAsDom(testProject, file))
                .stream()
                .map(Node::getTextContent)
                .collect(Collectors.toList());
        cleanup();
        List<String> afterIds =
            xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(loadProjectPathAsDom(testProject, file))
                .stream()
                .map(Node::getTextContent)
                .collect(Collectors.toList());
        Assertions.assertThat(afterIds).containsExactlyInAnyOrderElementsOf(beforeIds);
    }

    /**
     * Test that when Form`s immeddiate child item has id set to 0 then its identifier gets replaced.
     *
     * @throws Exception When item does not receive positive identifier or there are duplicates or some identifiers
     * are missing.
     */
    @Test
    public void testId0ImmediateChildReplaced()
    {
        String file = "src/Catalogs/Catalog/Forms/Id0ImmediateChild/Form.form";
        Document beforeForm = loadProjectPathAsDom(testProject, file);
        List<String> correctIds =
            xpathAsNodeList("//id[not(parent::*[local-name()='attributes']) and ../name/text()!='id0']/text()")
                .apply(beforeForm)
                .stream()
                .map(Node::getTextContent)
                .collect(Collectors.toList());
        String offendingPath = "//id[../name/text()='id0']/text()";
        Assertions.assertThat(beforeForm).extracting(xpathAsString(offendingPath)).isEqualTo("0");
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file)).satisfies(afterForm -> {
            Assertions.assertThat(afterForm)
                .extracting(xpathAsString(offendingPath))
                .extracting(Integer::parseInt, InstanceOfAssertFactories.INTEGER)
                .isPositive();
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT), InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .satisfies(newIds -> {
                    Assertions.assertThat(newIds).map(String.class::cast).containsAll(correctIds);
                    Assertions.assertThat(newIds).doesNotHaveDuplicates();
                });
        });
    }

    /**
     * Test that when Form`s nested child item has id set to 0 then its identifier gets replaced.
     *
     * @throws Exception When item does not receive positive identifier or there are duplicates or some identifiers
     * are missing.
     */
    @Test
    public void testId0NestedChildReplaced()
    {
        String file = "src/Catalogs/Catalog/Forms/Id0NestedChild/Form.form";
        Document beforeForm = loadProjectPathAsDom(testProject, file);
        List<String> correctIds =
            xpathAsNodeList("//id[not(parent::*[local-name()='attributes']) and ../name/text()!='id0']/text()")
                .apply(beforeForm)
                .stream()
                .map(Node::getTextContent)
                .collect(Collectors.toList());
        String offendingPath = "//id[../name/text()='id0']/text()";
        Assertions.assertThat(beforeForm).extracting(xpathAsString(offendingPath)).isEqualTo("0");
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file)).satisfies(afterForm -> {
            Assertions.assertThat(afterForm)
                .extracting(xpathAsString(offendingPath))
                .extracting(Integer::parseInt, InstanceOfAssertFactories.INTEGER)
                .isPositive();
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT), InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .satisfies(newIds -> {
                    Assertions.assertThat(newIds).map(String.class::cast).containsAll(correctIds);
                    Assertions.assertThat(newIds).doesNotHaveDuplicates();
                });
        });
    }

    /**
     * Test that when Form`s child item does not have id element then it gets one with a positive value.
     *
     * @throws Exception When item does not receive positive identifier or there are duplicates or some identifiers
     * are missing.
     */
    @Test
    public void testMissingIdAdded()
    {
        String file = "src/Catalogs/Catalog/Forms/MissingId/Form.form";
        Document beforeForm = loadProjectPathAsDom(testProject, file);
        List<String> correctIds = xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(beforeForm)
            .stream()
            .map(Node::getTextContent)
            .collect(Collectors.toList());
        String offendingPath = "//id[../name/text()='missing']";
        Assertions.assertThat(beforeForm).extracting(xpathAsNode(offendingPath)).isNull();
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file)).satisfies(afterForm -> {
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNode(offendingPath))
                .isNotNull()
                .extracting(Node::getTextContent)
                .extracting(Integer::parseInt, InstanceOfAssertFactories.INTEGER)
                .isPositive();
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT), InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .satisfies(newIds -> {
                    Assertions.assertThat(newIds).map(String.class::cast).containsAll(correctIds);
                    Assertions.assertThat(newIds).doesNotHaveDuplicates();
                });
        });
    }

    /**
     * Test that when Form`s child item has a negative id value then it does not get replaced.
     *
     * @throws Exception When some of the identifiers have changed their values.
     */
    @Test
    public void testNegativeIdNoChanges()
    {
        String file = "src/Catalogs/Catalog/Forms/NegativeId/Form.form";
        Document beforeForm = loadProjectPathAsDom(testProject, file);
        List<String> beforeIds = xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(beforeForm)
            .stream()
            .map(Node::getTextContent)
            .collect(Collectors.toList());
        Assertions.assertThat(beforeForm)
            .extracting(xpathAsNode("//id[../name/text()='negative']"))
            .isNotNull()
            .extracting(Node::getTextContent)
            .extracting(Integer::parseInt, InstanceOfAssertFactories.INTEGER)
            .isNegative();
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file))
            .extracting(xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT), InstanceOfAssertFactories.LIST)
            .map(Node.class::cast)
            .map(Node::getTextContent)
            .containsAll(beforeIds);
    }

    /**
     * Test that when there are multiple elements on a form with a missing identifier then
     * unique identifiers are added for every of them.
     *
     * @throws Exception When not all items with missing identifiers have got their new identifiers
     * or there are duplicates.
     */
    @Test
    public void testMultipleMissingIdsGetAdded()
    {
        String file = "src/Catalogs/Catalog/Forms/MissingIdMultiple/Form.form";
        Document beforeForm = loadProjectPathAsDom(testProject, file);
        int correctItemsCount = xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(beforeForm).size();
        List<Node> itemsWithMissingId =
            xpathAsNodeList("//*[starts-with(child::name/text(),'missing')]").apply(beforeForm);
        Assertions.assertThat(itemsWithMissingId).isNotEmpty();
        Assertions.assertThat(beforeForm)
            .extracting(xpathAsNodeList("//id[starts-with(../name/text(),'missing')]"), InstanceOfAssertFactories.LIST)
            .isEmpty();
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file)).satisfies(afterForm -> {
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList("//id[starts-with(../name/text(),'missing')]"),
                    InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .map(Integer::parseInt)
                .allSatisfy(id -> Assertions.assertThat(id).isNotZero())
                .size()
                .isEqualTo(itemsWithMissingId.size());
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT), InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .doesNotHaveDuplicates()
                .size()
                .isEqualTo(correctItemsCount + itemsWithMissingId.size());
        });
    }

    /**
     * Test that when Form`s child items have duplicate identifiers then each duplicated item except the first one
     * gets new identifier.
     *
     * @throws Exception When there are still duplicates
     * or when all old duplicates are replaced (should not replace first duplicate).
     */
    @Test
    public void testDuplicateIdReplaced()
    {
        String file = "src/Catalogs/Catalog/Forms/BadMerge/Form.form";
        Document beforeForm = loadProjectPathAsDom(testProject, file);
        int itemsCount = xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(beforeForm).size();
        List<String> duplicateIds = xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT).apply(beforeForm)
            .stream()
            .collect(Collectors.groupingBy(Node::getTextContent))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> {
                Assertions.assertThat(entry.getValue()).size().isEqualTo(2);
                return entry;
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        Assertions.assertThat(duplicateIds).isNotEmpty();
        Assertions.assertThat(beforeForm)
            .extracting(xpathAsNodeList("//id[starts-with(../name/text(),'DescriptionByUser')]"),
                InstanceOfAssertFactories.LIST)
            .map(Node.class::cast)
            .map(Node::getTextContent)
            .containsAll(duplicateIds)
            .isSubsetOf(duplicateIds);
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file)).satisfies(afterForm -> {
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList("//id[starts-with(../name/text(),'DescriptionByUser')]"),
                    InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .map(Integer::parseInt)
                .allSatisfy(id -> Assertions.assertThat(id).isNotZero())
                .size()
                .isEqualTo(duplicateIds.size() * 2);
            Assertions.assertThat(afterForm)
                .extracting(xpathAsNodeList(ALL_NON_ATTRIBUTE_ID_TEXT), InstanceOfAssertFactories.LIST)
                .map(Node.class::cast)
                .map(Node::getTextContent)
                .containsAll(duplicateIds)
                .doesNotHaveDuplicates()
                .size()
                .isEqualTo(itemsCount);
        });
    }

    /**
     * Test that when fixing Forms`s immediate autoCommandBar then it gets {@code -1} as new identifier.
     *
     * @throws Exception When autoCommandBar identifier is not replaced with {@code -1}.
     */
    @Test
    public void testFormAutoCommandBarGetsMinusOne()
    {
        String file = "src/Catalogs/Catalog/Forms/MissingIdMultiple/Form.form";
        String offendingPath = "/*[local-name()='Form']/autoCommandBar/id";
        Assertions.assertThat(loadProjectPathAsDom(testProject, file)).extracting(xpathAsNode(offendingPath)).isNull();
        cleanup();
        Assertions.assertThat(loadProjectPathAsDom(testProject, file))
            .extracting(xpathAsNode(offendingPath))
            .isNotNull()
            .extracting(Node::getTextContent)
            .isEqualTo("-1");
    }

    /**
     * Cleans up test project so that it can be verified.
     *
     * When this method completes, it is made sure that project sources have been saved on disk.
     */
    private void cleanup()
    {
        IDtProject dtProject = dtProjectManager.getDtProject(testProject);
        cleanupService.cleanUp(testProject, new NullProgressMonitor());
        // From CleanUpProjectSourceApi.cleanUpProjectSources(Path)
        Object handle = workspaceOrchestrator.beginExclusiveOperation("Wait for export to finish", //$NON-NLS-1$
            Collections.singleton(dtProject), ProjectPipelineJob.BEFORE_BUILD_DD);
        workspaceOrchestrator.endOperation(handle);
    }

    /**
     * Loads project source file as XML document.
     *
     * @param project Test project from which to load file. Must not be {@code null}.
     * @param relativePath Path to file to load that is relative to test project root. Must not be {@code null}.
     * @return Specified file as DOM tree. Never {@code null}.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws AssertionError If specified file cannot be loaded or is not a well-formed XML document.
     */
    private Document loadProjectPathAsDom(IProject project, String relativePath)
    {
        try
        {
            try (InputStream content = project.getFile(relativePath).getContents(false))
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                // Sonar: XML parsers should not be vulnerable to XXE attacks (java:S2755)
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                return factory.newDocumentBuilder().parse(content);
            }
        }
        catch (ParserConfigurationException | CoreException | IOException | SAXException e)
        {
            throw new AssertionError("Unable to load XML file: " + relativePath, e);
        }
    }

    /**
     * Creates functon that evaluates specified XPath expression as {@link String}.
     *
     * This method is supposed to be used with {@link org.assertj.core.api.AbstractObjectAssert#extracting(Function)}.
     *
     * Returned function will throw an error when expression cannot be compiled or evaluated.
     *
     * @param xpath XPath to evaluate. Must not be {@code null}.
     * @return Function that evaluates specified expression on provided {@link Document} which must not be {@code null}.
     * Never {@code null}.
     */
    private Function<Document, String> xpathAsString(String xpath)
    {
        return document -> {
            try
            {
                return compileXpath(xpath).evaluate(document);
            }
            catch (XPathExpressionException e)
            {
                throw new AssertionError("Cannot evaluate XPATH as string: " + xpath, e);
            }
        };
    }

    /**
     * Creates functon that evaluates specified XPath expression as {@link Node}.
     *
     * This method is supposed to be used with {@link org.assertj.core.api.AbstractObjectAssert#extracting(Function)}.
     *
     * Returned function will throw an error when expression cannot be compiled or evaluated.
     *
     * @param xpath XPath to evaluate. Must not be {@code null}.
     * @return Function that evaluates specified expression on provided {@link Document} which must not be {@code null}.
     * Never {@code null}.
     */
    private Function<Document, Node> xpathAsNode(String xpath)
    {
        return document -> {
            try
            {
                return (Node)compileXpath(xpath).evaluate(document, XPathConstants.NODE);
            }
            catch (XPathExpressionException e)
            {
                throw new AssertionError("Cannot evaluate XPATH as node: " + xpath, e);
            }
        };
    }

    /**
     * Creates functon that evaluates specified XPath expression as {@link List} of {@link Node}.
     *
     * This method is supposed to be used with {@link org.assertj.core.api.AbstractObjectAssert#extracting(Function)}.
     *
     * Returned function will throw an error when expression cannot be compiled or evaluated.
     *
     * @param xpath XPath to evaluate. Must not be {@code null}.
     * @return Function that evaluates specified expression on provided {@link Document} which must not be {@code null}.
     * Never {@code null}.
     */
    private Function<Document, List<Node>> xpathAsNodeList(String xpath)
    {
        return document -> {
            try
            {
                NodeList nodelist = (NodeList)compileXpath(xpath).evaluate(document, XPathConstants.NODESET);
                List<Node> result = new ArrayList<>();
                for (int i = 0; i < nodelist.getLength(); i++)
                {
                    result.add(nodelist.item(i));
                }
                return result;
            }
            catch (XPathExpressionException e)
            {
                throw new AssertionError("Cannot evaluate XPATH as list of nodes: " + xpath, e);
            }
        };
    }

    /**
     * Compiles specified XPath expression.
     *
     * @param xpath Expression to compile. Must not be {@code null}.
     * @return Expression that is ready to be evaluated. Never {@code null}.
     * @throws XPathExpressionException If specified expression cannot be compiled.
     */
    private XPathExpression compileXpath(String xpath) throws XPathExpressionException
    {
        return XPathFactory.newInstance().newXPath().compile(xpath);
    }

    /**
     * Modules with test dependencies.
     *
     * Dependencies include those required by
     * test itself as well as all dependencies required by implementation of {@link ICleanUpProjectSourcesManager}
     * to function properly (see sources of
     * {code com._1c.g5.v8.dt.internal.migration.cleanup.CleanUpProjectSourcesManager}).
     */
    public static class Dependencies
        extends AbstractServiceAwareModule
    {

        /**
         * Creates new instance.
         */
        public Dependencies()
        {
            super(CorePlugin.getDefault());
        }

        @Override
        protected void doConfigure()
        {
            bind(IWorkspaceOrchestrator.class).toService();
            bind(IBmModelManager.class).toService();
            bind(IDtProjectManager.class).toService();
            bind(IDerivedDataManagerProvider.class).toService();
        }

    }

}
