package xyz.ottr.lutra.store;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import static xyz.ottr.lutra.model.terms.ObjectTerm.cons;
import static xyz.ottr.lutra.model.terms.ObjectTerm.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class StandardTemplateStoreTest {

    @Test
    public void testAddBaseTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);

        BaseTemplate base = BaseTemplate.builder()
                .iri("base")
                .parameters(createParametersList(new String[] {"x", "y"}))
                .build();
        manager.addBaseTemplate(base);

        Template template = buildDummyTemplate("iri-0", new String[] {"x", "y"});
        manager.addTemplate(template);

        Assert.assertTrue("BaseTemplate should be in store", manager.containsBase("base"));
        Assert.assertTrue("Non-BaseTemplate should be in store but not as BaseTemplate",
                (!manager.containsBase("iri-0")) && manager.contains("iri-0"));
    }

    @Test
    public void testAddTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);

        // success - no signature
        Template template0 = buildDummyTemplate("iri-0", new String[] {"x", "y"});
        manager.addTemplate(template0);
        Assert.assertTrue("Adding Template without matching Signature present should succeed", manager.containsTemplate("iri-0"));

        // success - existing signature
        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        Assert.assertTrue("Adding Template with matching Signature present should succeed", manager.containsTemplate("iri-1"));

        // existing Sig is Template
        Template template2 = buildDummyTemplate("iri-2", new String[] {"a", "b"});
        manager.addTemplate(template2);
        Assert.assertFalse("Adding Template when there is already one in the store with dependencies set should "
                + "not succeed", manager.addTemplate(template2).isPresent());

        // differing parameters
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);
        Template template3 = buildDummyTemplate("iri-2", new String[] {"x", "y", "z"});
        Assert.assertFalse("Adding Template with different parameter number than Signature should not succeed",
                manager.addTemplate(template3).isPresent());
        // TODO add "real" differing parameters when implemented not just list length
    }

    @Test
    public void testAddSignature() {
        TemplateStore manager = new StandardTemplateStore(null);

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});

        manager.addSignature(signature1);
        Assert.assertTrue("Adding Signature to empty store should succeed", manager.containsSignature("iri-1"));
        manager.addSignature(signature2);
        Assert.assertTrue("Adding non-existing Signature to store should succeed", manager.containsSignature("iri-2"));
        Assert.assertFalse("Adding existing Signature to store should return false", manager.addSignature(signature1).isPresent());
        // TODO add Signature that is a Template with/without matching params once this is implemented
    }



    @Test
    public void testContainsWithTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);

        Assert.assertTrue("Checking for Template that is in the store should return true", manager.contains("iri-1"));
        Assert.assertFalse("Checking for Template that is not in the store should return false", manager.contains("iri-99"));
    }

    @Test
    public void testContainsWithSignature() {
        TemplateStore manager = new StandardTemplateStore(null);

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);

        Assert.assertTrue("Checking for Signature that is in the store should return true", manager.contains("iri-1"));
        Assert.assertFalse("Checking for Signature that is not in the store should return false", manager.contains("iri-99"));
    }

    @Test
    public void testContainsBase() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();

        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);

        Assert.assertFalse("Checking for IRI that is in the store but is not a BaseTemplate should return false",
                manager.containsBase("iri-1"));
        Assert.assertTrue("Checking for a BaseTemplate should return true", manager.containsBase(OTTR.BaseURI.Triple));
    }

    @Test
    public void testContainsDefinitionOf() {
        TemplateStore manager = new StandardTemplateStore(null);

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        Assert.assertFalse("Checking for IRI that is not in the store should return false", manager.containsDefinitionOf("iri-3"));
        Assert.assertFalse("Checking for IRI that is in the store but is not a Template should return false",
                manager.containsDefinitionOf("iri-2"));
        Assert.assertTrue("Checking for IRI that is in the store and is a Template should return true",
                manager.containsDefinitionOf("iri-1"));
    }

    @Test
    public void testGetSignature() {
        TemplateStore manager = new StandardTemplateStore(null);

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);

        Assert.assertTrue("Requesting existing Signature from store should produce value",
                manager.getSignature("iri-1").isPresent());
        Assert.assertFalse("Requesting non-existing Signature from store should not produce value",
                manager.getSignature("iri-2").isPresent());
    }

    @Test
    public void testGetTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);

        Assert.assertTrue("Requesting existing Template from store should produce value", manager.getTemplate("iri-1").isPresent());
        Assert.assertFalse("Requesting non-existing Template from store should not produce value",
                manager.getTemplate("iri-2").isPresent());

        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        // store containing Signature but asked for getTemplate()
        Assert.assertFalse("Requesting Template that is a Signature in the store should not produce value",
                manager.getTemplate("iri-2").isPresent());
    }

    @Test
    public void testGetAllTemplates() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        Assert.assertEquals("Number of Templates should be as expected", 2, manager.getAllBaseTemplates().getStream().count());
    }

    @Test
    public void testGetAllSignatures() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        Assert.assertEquals("Number of Signatures should be as expected", 4, manager.getAllSignatures().getStream().count());
    }

    @Test
    public void testGetAllBaseTemplates() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();

        Signature signature1 = buildDummySignature("iri-1", new String[] {"x", "y"});
        manager.addSignature(signature1);
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        Assert.assertEquals("Number of BaseTemplates should be as expected", 2, manager.getAllBaseTemplates().getStream().count());
    }

    @Test
    public void testGetDependsOn() {
        TemplateStore manager = new StandardTemplateStore(null);

        BaseTemplate base = BaseTemplate.builder()
                .iri("base")
                .parameters(createParametersList(new String[] {"x", "y"}))
                .build();
        manager.accept(base);

        // dependent on base
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        // dependent on base, but Signatures are ignored
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        // dependent on iri-1
        Template template3 = Template.builder()
                .iri("iri-3")
                .parameters(createParametersList(new String[] {"x", "y"}))
                .instance(Instance.builder()
                        .iri("iri-1")
                        .argument(Argument.builder().term(var("a")).build())
                        .argument(Argument.builder().term(cons(1)).build())
                        .build())
                .build();
        manager.addTemplate(template3);

        // NOT dependent on iri-1, but on base
        Template template4 = buildDummyTemplate("iri-4", new String[] {"x", "y"});
        manager.addTemplate(template4);

        Result<Set<String>> dependentOnBase = manager.getDependsOn("base");
        Assert.assertTrue(dependentOnBase.isPresent());
        Assert.assertEquals(Set.of("iri-1", "iri-4"), dependentOnBase.get());
        Assert.assertNotEquals(Set.of("iri-2"), dependentOnBase.get());

        Result<Set<String>> dependentOnIri1 = manager.getDependsOn("iri-1");
        Assert.assertTrue(dependentOnIri1.isPresent());
        Assert.assertEquals(Set.of("iri-3"), dependentOnIri1.get());
        Assert.assertNotEquals(Set.of("iri-4"), dependentOnIri1.get());
    }

    @Test
    public void testMissingDependencies() {
        TemplateStore manager = new StandardTemplateStore(null);

        BaseTemplate base = BaseTemplate.builder()
                .iri("base")
                .parameters(createParametersList(new String[] {"x", "y"}))
                .build();
        manager.accept(base);

        // dependent on base
        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.addTemplate(template1);
        // dependent on base, but Signatures are ignored
        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.addSignature(signature2);

        // dependent on something unknown
        Template template3 = Template.builder()
                .iri("iri-3")
                .parameters(createParametersList(new String[] {"x", "y"}))
                .instance(Instance.builder()
                        .iri("iri-0")
                        .argument(Argument.builder().term(var("a")).build())
                        .argument(Argument.builder().term(cons(1)).build())
                        .build())
                .build();
        manager.addTemplate(template3);

        Assert.assertEquals("Only missing dependencies should be returned", Set.of("iri-2", "iri-0"), manager.getMissingDependencies());
    }

    @Test
    public void testAccept() {
        TemplateStore manager = new StandardTemplateStore(null);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"x", "y"});
        manager.accept(template1);

        Assert.assertTrue("Added Signature should be found in TemplateStore", manager.getSignature("iri-1").isPresent());
        Assert.assertTrue("Added Template should be found in TemplateStore", manager.getTemplate("iri-1").isPresent());

        Signature signature2 = buildDummySignature("iri-2", new String[] {"x", "y"});
        manager.accept(signature2);

        Assert.assertTrue("Added Signature should be found in TemplateStore", manager.getSignature("iri-2").isPresent());
        Assert.assertFalse("Added Signature should not be a Template in the in TemplateStore", manager.getTemplate("iri-2").isPresent());
    }

    @Test
    public void testAcceptExistingTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);
        Template template0 = buildDummyTemplate("iri-0", new String[] {"x", "y"});

        manager.accept(template0);
        manager.accept(template0);
        Assertions.atLeast(manager.getMessageHandler(), Message.Severity.WARNING);
    }

    @Test
    public void testParametersOfSigAndTempDiffer() {
        StandardTemplateStore manager = new StandardTemplateStore(null);
        Signature signature1 = buildDummySignature("iri-1", new String[] {"a", "b"});
        Template template3 = buildDummyTemplate("iri-1", new String[] {"a"});

        manager.accept(signature1);
        manager.accept(template3);
        Assertions.atLeast(manager.getMessageHandler(), Message.Severity.WARNING);
    }

    @Test
    public void testAcceptExistingSignature() {
        TemplateStore manager = new StandardTemplateStore(null);
        Signature signature = buildDummySignature("iri-0", new String[] {"a", "b"});

        manager.accept(signature);
        manager.accept(signature);
        Assertions.atLeast(manager.getMessageHandler(), Message.Severity.WARNING);
    }

    @Test
    public void testAcceptExistingBaseTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);

        BaseTemplate base1 = BaseTemplate.builder()
                .iri("base")
                .parameters(createParametersList(new String[] {"x", "y"}))
                .build();

        BaseTemplate base2 = BaseTemplate.builder()
                .iri("base")
                .parameters(createParametersList(new String[] {"x", "y", "z"}))
                .build();

        manager.accept(base1);
        manager.accept(base2);
        Assertions.atLeast(manager.getMessageHandler(), Message.Severity.WARNING);
    }

    private Signature buildDummySignature(String iri, String[] parameters) {
        List<Parameter> parameterList = createParametersList(parameters);

        return Signature.superbuilder()
                .iri(iri)
                .parameters(parameterList)
                .build();
    }

    private Template buildDummyTemplate(String iri, String[] parameters) {
        List<Parameter> parameterList = createParametersList(parameters);

        return Template.builder()
                .iri(iri)
                .parameters(parameterList)
                .instance(Instance.builder()
                        .iri("base")
                        .argument(Argument.builder().term(var("a")).build())
                        .argument(Argument.builder().term(cons(1)).build())
                        .build())
                .instance(Instance.builder()
                        .iri("base")
                        .argument(Argument.builder().term(cons(2)).build())
                        .argument(Argument.builder().term(var("b")).build())
                        .build())
                .build();
    }

    private List<Parameter> createParametersList(String[] parameters) {
        List<Parameter> parameterList = new ArrayList<>(parameters.length);
        for (String s : parameters) {
            parameterList.add(Parameter.builder().term(var(s)).build());
        }
        return parameterList;
    }

}
