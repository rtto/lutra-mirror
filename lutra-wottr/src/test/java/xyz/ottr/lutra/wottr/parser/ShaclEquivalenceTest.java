package xyz.ottr.lutra.wottr.parser;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.graph.StandardTemplateStore;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFIO;


@RunWith(Parameterized.class)
public class ShaclEquivalenceTest {

    private static final String correct = FilenameUtils.separatorsToSystem("src/test/resources/spec/tests/correct/");
    private static final String incorrect = FilenameUtils.separatorsToSystem("src/test/resources/spec/tests/incorrect/");

    private static final Set<String> unsupportedTests = Stream.of(
            incorrect + "instance10.ttl", // Instance checking on types
            incorrect + "signature06.ttl" // TODO: Fix somehow
        ).collect(Collectors.toSet());
    
    private static final Set<String> instanceTests = Stream.of(
            correct + "argument02.ttl", 
            correct + "argument03.ttl", 
            correct + "instance01.ttl", 
            correct + "instance02.ttl", 
            correct + "instance08.ttl", 
            incorrect + "argument01.ttl",
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
    
    private final TemplateReader tempReader;
    private final InstanceReader insReader;
    
    private final String filename;
    private final boolean isCorrect;
    
    public ShaclEquivalenceTest(String filename, boolean isCorrect) {
        this.filename = filename;
        this.isCorrect = isCorrect;
        
        this.tempReader = new TemplateReader(RDFIO.fileReader(), new WTemplateParser());
        this.insReader = new InstanceReader(RDFIO.fileReader(), new WInstanceParser());
    }
    
    @Parameters(name = "{index}: {0} is {1}")
    public static List<Object[]> data() {

        List<Object[]> input = Files.loadFromFolder(correct, new String[] { "ttl" }, new String[0])
            .getStream()
            .map(Result::get)
            .sorted()
            .map(r -> new Object[]{ r, true })
            .collect(Collectors.toList());

        input.addAll(Files.loadFromFolder(incorrect, new String[] { "ttl" }, new String[0])
            .getStream()
            //.filter(x -> false) // for debugging
            .map(Result::get)
            .sorted()
            .map(s -> new Object[]{ s, false })
            .collect(Collectors.toList()));

        return input;   
    }
    
    
    @Test
    public void checkFile() {
        assumeTrue(!unsupportedTests.contains(this.filename));
        
        if (instanceTests.contains(this.filename)) {
            checkInstance(this.filename, this.isCorrect);
        } else {
            checkTemplate(this.filename, this.isCorrect);
        }
    }

    private void checkTemplate(String file, boolean correct) {

        ResultStream<Signature> templates = this.tempReader.apply(file);

        TemplateStore store = new StandardTemplateStore(null);
        ResultConsumer<Signature> tplErrorMessages = new ResultConsumer<>(store);
        templates.forEach(tpl -> {
            if (correct) {
                assertTrue("Should parse: " + file + ", but failed with errors:\n"
                    + tpl.getAllMessages(), tpl.isPresent());
            }
            tplErrorMessages.accept(tpl);
        });

        List<Message> errors = store.checkTemplatesForErrorsOnly()
            .combine(tplErrorMessages.getMessageHandler())
            .getMessages();

        errors.removeIf(message -> message.getSeverity().isLessThan(Message.Severity.ERROR));

        if (!correct) {
            assertFalse("Should produce error messages: " + file, errors.isEmpty());
        } else {
            assertTrue("File " + file + " should not produce any error messages, but gave:\n"
                + errors, errors.isEmpty());
            assertTrue("File " + file + " should produce a template, but no templates produced.",
                store.getAllSignatures().getStream().count() > 0);
        }
    }

    private void checkInstance(String file, boolean correct) {
        ResultStream<Instance> instances = this.insReader.apply(file);

        ResultConsumer<Instance> insErrorMessages = new ResultConsumer<>();
        instances.forEach(ins -> {
            if (correct) {
                assertTrue("Should parse: " + file + ", but failed with errors:\n"
                    + ins.getAllMessages(), ins.isPresent());
            }
            insErrorMessages.accept(ins);
        });

        if (correct) {
            Assertions.noErrors(insErrorMessages);
        } else {
            Assertions.atLeast(insErrorMessages, Message.Severity.ERROR);
        }
    }

}
