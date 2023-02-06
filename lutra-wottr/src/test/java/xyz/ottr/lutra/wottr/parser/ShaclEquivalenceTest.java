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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.io.RDFIO;

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
    
    private TemplateReader tempReader;
    private InstanceReader insReader;

    @BeforeEach
    public void setupReaders() {
        this.tempReader = new TemplateReader(RDFIO.fileReader(), new WTemplateParser());
        this.insReader = new InstanceReader(RDFIO.fileReader(), new WInstanceParser());
    }

    public static Stream<Arguments> data() {

        Stream.Builder<Arguments> streamBuilder = Stream.builder();

        Files.loadFromFolder(correct, new String[] { "ttl" }, new String[0])
                .innerForEach(s -> streamBuilder.accept(arguments(s, true)));

        Files.loadFromFolder(incorrect, new String[] { "ttl" }, new String[0])
                .innerForEach(s -> streamBuilder.accept(arguments(s, false)));

        return streamBuilder.build();
    }
    
    @ParameterizedTest
    @MethodSource("data")
    public void checkFile(String filename, boolean isCorrect) {
        assumeTrue(!unsupportedTests.contains(filename));
        
        if (instanceTests.contains(filename)) {
            checkInstance(filename, isCorrect);
        } else {
            checkTemplate(filename, isCorrect);
        }
    }

    private void checkTemplate(String file, boolean correct) {

        ResultStream<Signature> templates = this.tempReader.apply(file);

        TemplateStore store = new StandardTemplateStore(null);
        ResultConsumer<Signature> tplErrorMessages = new ResultConsumer<>(store);
        templates.forEach(tpl -> {
            if (correct) {
                assertTrue(tpl.isPresent(), "Should parse: " + file + ", but failed with errors:\n" + tpl.getAllMessages());
            }
            tplErrorMessages.accept(tpl);
        });

        List<Message> errors = store.checkTemplatesForErrorsOnly()
            .combine(tplErrorMessages.getMessageHandler())
            .getMessages();

        errors.removeIf(message -> message.getSeverity().isLessThan(Message.Severity.ERROR));

        if (!correct) {
            assertFalse(errors.isEmpty(), "Should produce error messages: " + file);
        } else {
            assertTrue(errors.isEmpty(), "File " + file + " should not produce any error messages, but gave:\n" + errors);
            assertTrue(store.getAllSignatures().getStream().count() > 0,
                    "File " + file + " should produce a template, but no templates produced.");
        }
    }

    private void checkInstance(String file, boolean correct) {
        ResultStream<Instance> instances = this.insReader.apply(file);

        ResultConsumer<Instance> insErrorMessages = new ResultConsumer<>();
        instances.forEach(ins -> {
            if (correct) {
                assertTrue(ins.isPresent(), "Should parse: " + file + ", but failed with errors:\n" + ins.getAllMessages());
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
