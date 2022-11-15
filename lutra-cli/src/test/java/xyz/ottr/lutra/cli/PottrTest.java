package xyz.ottr.lutra.cli;



/*-
 * #%L
 * lutra-cli
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

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.expansion.CheckingExpander;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

public class PottrTest {

    private static final Path ROOT = Paths.get("src", "test", "resources", "primer", "files");

    public static Stream<Arguments> data() {
        return Stream.of(
            arguments("01-basics/1/ins/Person1.stottr", "01-basics/1/tpl/", true),
            arguments(null, "01-basics/2/tpl/", true),
            arguments("01-basics/3/tpl/cycle.stottr", "01-basics/3/tpl/", false),
            arguments(null, "01-basics/4/tpl/", false),
            arguments("01-basics/5/ins/relative.stottr", "01-basics/5/tpl/", false),
            arguments("01-basics/6/ins/nullable.stottr", "01-basics/6/tpl/", true),
            arguments("01-basics/7/ins/Person.stottr", "01-basics/7/tpl/", true),
            arguments("01-basics/8/ins/organisation.stottr", "01-basics/8/tpl/", true),
            arguments("01-basics/9/ins/person.stottr", "01-basics/9/tpl/", true),
            arguments("01-basics/10/ins/member.stottr", "01-basics/10/tpl/", true),
            arguments("01-basics/11/ins/members.stottr", "01-basics/11/tpl/", true),
            arguments("01-basics/12/ins/friends.stottr", "01-basics/12/tpl/", true),
            arguments("01-basics/13/ins/orgmembers.stottr", "01-basics/13/tpl/", true),
            arguments("01-basics/14/ins/expmode.stottr", "01-basics/14/tpl/", true),
            arguments("01-basics/15/ins/namedpizza.stottr", null, true),

            arguments("02-modelling/1/ins/phoneinst.stottr", "02-modelling/1/tpl/", true)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void test(String instancePath, String templatePath, boolean expectedResults) {
        runExpand(instancePath, templatePath, expectedResults);
    }

    private String resolve(String pathFromRoot) {
        return ROOT.resolve(pathFromRoot).toString();
    }

    private void runExpand(String fileInstance, String pathTemplates, boolean expectedResults) {

        TemplateStore store = getStore();

        List<Message> messages = new ArrayList<>();

        if (StringUtils.isNotBlank(pathTemplates)) {
            messages.addAll(testTemplates(store, pathTemplates));
        }
        if (StringUtils.isNotBlank(fileInstance)) {
            messages.addAll(testInstances(store, fileInstance));
        }

        messages.removeIf(message -> message.getSeverity().isLessThan(Message.Severity.ERROR));

        // create matcher based on expectedResults: is or is-not if expected is true or false.
        Function<Object, Matcher> matcher = expectedResults
            ? o -> Is.is(o)
            : o -> Is.is(IsNot.not(o));

        MatcherAssert.assertThat("On " + pathTemplates + " and " + fileInstance, messages, matcher.apply(Collections.emptyList()));
    }

    private TemplateStore getStore() {
        TemplateManager tmwf = new TemplateManager();
        for (StandardFormat format : StandardFormat.values()) {
            tmwf.registerFormat(format.format);
        }
        return tmwf.getTemplateStore();
    }

    private List<Message> testTemplates(TemplateStore store, String path) {

        List<Message> messages = new ArrayList<>();

        TemplateReader reader = new TemplateReader(new SFileReader(), new STemplateParser());
        messages.addAll(reader.loadTemplatesFromFolder(store, resolve(path), new String[]{}, new String[]{}).getMessages());

        store.fetchMissingDependencies();
        messages.addAll(store.checkTemplates().getMessages());

        return messages;
    }

    private List<Message> testInstances(TemplateStore store, String file) {
        InstanceReader insReader = new InstanceReader(new SFileReader(), new SInstanceParser());
        Expander expander = new CheckingExpander(store);
        ResultStream<Instance> expandedInInstances = insReader
            .apply(resolve(file))
            .innerFlatMap(expander::expandInstanceFetch);

        // Write expanded instances to model
        String filePath = "src/test/resources/temp_pottrTests";
        SInstanceWriter insWriter = new SInstanceWriter(OTTR.getDefaultPrefixes());
        insWriter.init(filePath, null);
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        MessageHandler msgs = expansionErrors.getMessageHandler();
        msgs.combine(insWriter.flush());
        msgs.combine(insWriter.close());
        new File(filePath).delete();
        return msgs.getMessages();
    }
}
