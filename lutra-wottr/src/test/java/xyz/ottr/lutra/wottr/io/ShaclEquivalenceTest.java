package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;

import xyz.ottr.lutra.wottr.io.WFileReader;
import xyz.ottr.lutra.wottr.io.WInstanceParser;
import xyz.ottr.lutra.wottr.io.WInstanceWriter;
import xyz.ottr.lutra.wottr.io.WTemplateParser;

public class ShaclEquivalenceTest {

    private static final String correct = "src/test/resources/spec/tests/correct/";
    private static final String incorrect = "src/test/resources/spec/tests/incorrect/";

    private static Set<String> unsupportedTests;
    private static Set<String> instanceTests;
    private static TemplateReader tempReader;
    private static InstanceReader insReader;

    @BeforeClass
    public static void populateUnsupported() {

        tempReader = new TemplateReader(new WFileReader(), new WTemplateParser());
        insReader = new InstanceReader(new WFileReader(), new WInstanceParser());

        instanceTests = Stream.of(
            correct + "argument02.ttl", 
            correct + "argument03.ttl", 
            correct + "instance01.ttl", 
            correct + "instance02.ttl", 
            correct + "instance08.ttl", 
            incorrect + "argument01.ttl", 
            incorrect + "basic01a.ttl", 
            incorrect + "basic01b.ttl", 
            incorrect + "basic01c.ttl", 
            incorrect + "basic01d.ttl", 
            incorrect + "basic01e.ttl", 
            incorrect + "basic02.ttl", 
            incorrect + "basic03.ttl", 
            incorrect + "basic04.ttl", 
            incorrect + "instance03.ttl", 
            incorrect + "instance04.ttl", 
            incorrect + "instance05.ttl", 
            incorrect + "instance06.ttl", 
            incorrect + "instance07.ttl", 
            incorrect + "instance09.ttl", 
            incorrect + "instance10.ttl", 
            incorrect + "instance11.ttl", 
            incorrect + "instance12.ttl" 
        ).collect(Collectors.toSet());

        unsupportedTests = Stream.of(
            correct + "basetemplate03.ttl", // Annotations
            correct + "signature10.ttl", // Annotations
            correct + "signature11.ttl", // Annotations
            incorrect + "basic01a.ttl", // Prefixes 
            incorrect + "basic01b.ttl", // Prefixes 
            incorrect + "basic01c.ttl", // Prefixes 
            incorrect + "basic01d.ttl", // Prefixes 
            incorrect + "basic01e.ttl", // Prefixes 
            incorrect + "basic02.ttl",  // Prefixes
            incorrect + "basic03.ttl",  // Prefixes
            incorrect + "basic04.ttl",  // Prefixes
            incorrect + "instance10.ttl", // Instance checking on types
            incorrect + "signature06.ttl", // TODO: Fix somehow
            incorrect + "signature12.ttl" // Annotation
        ).collect(Collectors.toSet());
    }

    private static void checkTemplate(String file, boolean correct) {

        ResultStream<TemplateSignature> templates = tempReader.apply(file);

        TemplateStore store = new DependencyGraph();
        ResultConsumer<TemplateSignature> tplErrorMessages = new ResultConsumer<>(store);
        templates.forEach(tpl -> {
            if (correct) {
                assertTrue("Should parse: " + file + ", but failed with errors:\n"
                    + tpl.getAllMessages().toString(), tpl.isPresent());
            }
            tplErrorMessages.accept(tpl);
        });

        List<Message> errors = store.checkTemplatesForErrorsOnly();
        errors.addAll(tplErrorMessages.getMessageHandler().getMessages());
        
        if (!correct) {
            assertFalse("Should produce error messages: " + file, errors.isEmpty());
        } else {
            assertTrue("File " + file + " should not produce any error messages, but gave:\n"
                + errors.toString(), errors.isEmpty());
            assertTrue("File " + file + " should produce a template, but no templates produced.",
                store.getAllTemplateObjects().getStream().count() > 0);
        }
    }

    private static void checkInstance(String file, boolean correct) {
        ResultStream<Instance> instances = insReader.apply(file);

        ResultConsumer<Instance> insErrorMessages = new ResultConsumer<>();
        instances.forEach(ins -> {
            if (correct) {
                assertTrue("Should parse: " + file + ", but failed with errors:\n"
                    + ins.getAllMessages().toString(), ins.isPresent());
            }
            insErrorMessages.accept(ins);
        });

        int msgLvl = insErrorMessages.getMessageHandler().printMessages();
        if (!correct) {
            assertTrue("Should have error messages: " + file, Message.moreSevere(msgLvl, Message.ERROR));
        }
        if (correct) {
            assertFalse("File " + file + " should not produce any error messages, but gave:\n"
                + insErrorMessages.getMessageHandler().getMessages().toString(),
                Message.moreSevere(msgLvl, Message.ERROR));
        }
    }

    private void check(String folder, boolean correct) {
        Files.loadFromFolder(folder, new String[] { "ttl" }, new String[0])
            .innerFilter(filename -> !instanceTests.contains(filename) && !unsupportedTests.contains(filename))
            .innerForEach(file -> checkTemplate(file, correct));
        Files.loadFromFolder(folder, new String[] { "ttl" }, new String[0])
            .innerFilter(filename -> instanceTests.contains(filename) && !unsupportedTests.contains(filename))
            .innerForEach(file -> checkInstance(file, correct));
    }

    @Ignore
    @Test
    public void correct() {
        check(correct, true);
    }

    @Ignore
    @Test
    public void incorrect() {
        check(incorrect, false);
    }
}
