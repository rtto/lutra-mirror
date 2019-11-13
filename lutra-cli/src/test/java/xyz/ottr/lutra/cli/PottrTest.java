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

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

@RunWith(Parameterized.class)
public class PottrTest {

    private static final Path ROOT = Paths.get("src", "test", "resources", ".temp-deploy", "pOTTR", "0.1", "files");

    private String instancePath;
    private String templatePath;
    private boolean expextedResults;

    public PottrTest(String instance, String template, boolean expextedResults) {
        this.instancePath = instance;
        this.templatePath = template;
        this.expextedResults = expextedResults;
    }

    @Parameterized.Parameters(name = "{index}: instance: {0}, template: {1}")
    public static List<Object[]> data() {
        return Arrays.asList(
            new Object[] { "01-basics/1/ins/Person1.stottr", "01-basics/1/tpl/", true },
            new Object[] { null, "01-basics/2/tpl/", true },
            new Object[] { "01-basics/3/tpl/cycle.stottr", "01-basics/3/tpl/", false },
            new Object[] { null, "01-basics/4/tpl/", false },
            new Object[] { "01-basics/5/ins/relative.stottr", "01-basics/5/tpl/", false },
            new Object[] { "01-basics/6/ins/nullable.stottr", "01-basics/6/tpl/", true },
            new Object[] { "01-basics/7/ins/Person.stottr", "01-basics/7/tpl/", true },
            new Object[] { "01-basics/8/ins/organisation.stottr", "01-basics/8/tpl/", true },
            new Object[] { "01-basics/9/ins/person.stottr", "01-basics/9/tpl/", true },
            new Object[] { "01-basics/10/ins/member.stottr", "01-basics/10/tpl/", true },
            new Object[] { "01-basics/11/ins/members.stottr", "01-basics/11/tpl/", true },
            new Object[] { "01-basics/12/ins/friends.stottr", "01-basics/12/tpl/", true },
            new Object[] { "01-basics/13/ins/orgmembers.stottr", "01-basics/13/tpl/", true },
            new Object[] { "01-basics/14/ins/expmode.stottr", "01-basics/14/tpl/", true },
            new Object[] { "01-basics/15/ins/namedpizza.stottr", null, true },

            new Object[] { "02-modelling/1/ins/phoneinst.stottr", "02-modelling/1/tpl/", true }
        );
    }


    @Test public void test() {
        runExpand(this.instancePath, this.templatePath, this.expextedResults);
    }

    private String resolve(String pathFromRoot) {
        return ROOT.resolve(pathFromRoot).toString();
    }

    private void runExpand(String fileInstance, String pathTemplates, boolean expectedResults) {

        boolean testResults = true;
        TemplateStore store = getStore();

        if (StringUtils.isNotBlank(pathTemplates)) {
            testResults &= testTemplates(store, pathTemplates);
        }
        if (StringUtils.isNotBlank(fileInstance)) {
            testResults &= testInstances(store, fileInstance);
        }
        assertEquals(expectedResults, testResults);
    }

    private TemplateStore getStore() {
        TemplateStore store = new DependencyGraph(ReaderRegistryImpl.getReaderRegistry());
        store.addOTTRBaseTemplates();
        return store;
    }

    private boolean testTemplates(TemplateStore store, String path) {
        TemplateReader reader = new TemplateReader(new SFileReader(), new STemplateParser());
        reader.loadTemplatesFromFolder(store, resolve(path), new String[]{}, new String[]{});
        store.fetchMissingDependencies();

        List<Message> tplMsg = store.checkTemplates();

        int maxError = tplMsg.stream()
            .mapToInt(Message::getLevel)
            .min()
            .orElse(Message.INFO);

        return !Message.moreSevere(maxError, Message.ERROR);
    }

    private boolean testInstances(TemplateStore store, String file) {
        InstanceReader insReader = new InstanceReader(new SFileReader(), new SInstanceParser());
        ResultStream<Instance> expandedInInstances = insReader
            .apply(resolve(file))
            .innerFlatMap(store::expandInstanceFetch);

        // Write expanded instances to model
        SInstanceWriter insWriter = new SInstanceWriter(new HashMap<>());
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<>(insWriter);
        expandedInInstances.forEach(expansionErrors);

        return !Message.moreSevere(expansionErrors.getMessageHandler().printMessages(), Message.ERROR);
    }
}
