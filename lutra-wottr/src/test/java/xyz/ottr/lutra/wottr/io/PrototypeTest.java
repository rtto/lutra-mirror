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

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
*/
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.query.Query;
import xyz.ottr.lutra.store.query.Tuple;
import xyz.ottr.lutra.wottr.WTemplateFactory;



public class PrototypeTest {

    private static final String inFolder = "src/test/resources/correct/";
    //private static final String inFolder = "src/test/resources/correct/definitions/cluster/pizza-alt/";
    //private static final String inFolder = "/home/leifhka/gits/aibel-templates_altered/tpl/etl/book/";
    private static DependencyGraph graph;
    private static TemplateReader templateReader;
    private static TemplateReader legacyReader;

    private static final String newTemplate = "http://data.aibel.com/ottr/tpl/etl/book/ASME_B16_9_DIM/NewTemplate";

    @BeforeClass    
    public static void load() throws IOException {
        legacyReader = new TemplateReader(new WFileReader(), new xyz.ottr.lutra.wottr.legacy.io.WTemplateParser());
        templateReader = new TemplateReader(new WFileReader(), new WTemplateParser());
        graph = new DependencyGraph();
    }

    @Test
    public void shouldParse() throws IOException {
        graph.addTemplateSignature(WTemplateFactory.createTripleTemplateHead());
        //templateReader.loadTemplatesFromFolder(graph, inFolder, new String []{"ttl"}, new String []{});
        legacyReader.loadTemplatesFromFolder(graph, inFolder, new String []{"ttl"}, new String []{});
        //int f = 0;
        //for (Template t : graph.getAllTemplates().getStream().map(r -> r.get()).collect(Collectors.toSet())) {
        //    if (t.getIRI().equals(newTemplate)) {
        //        continue;
        //    }
        //    boolean fixed = graph.refactor(newTemplate, t.getIRI());
        //    if (fixed) {
        //        f++;
        //    }
        //}
        //System.out.println(f);

        //Instance ins = new Instance("http://draft.ottr.xyz/i17/partlength",
        //                            new ArgumentList(new IRITerm("example.org/Part"),
        //                                             new IRITerm("example.org/Whole"),
        //                                             new LiteralTerm("7", "http://www.w3.org/2001/XMLSchema#integer")));
        //ResultStream<Instance> instances = graph.expandInstance(ins, templateReader);
        //Set<Instance> expanded = new HashSet<>();
        //ResultConsumer<Instance> fetchErrs = new ResultConsumer<Instance>(exp -> expanded.add(exp));
        //instances.forEach(fetchErrs);
        //fetchErrs.printMessages();
        //System.out.println("Instances:");
        //expanded.forEach(exp -> System.out.println(exp.toString()));

        //Result<DependencyGraph> resExpanded = graph.expandAll();
        //DependencyGraph expGraph = resExpanded.get();

        // Test of instance expansion
        //Set<Instance> instances = new HashSet<>();
        //String ns = "http://example.org/";
        //int nrIns = 10;
        //for (int i = 0; i < nrIns; i++) {
        //    Instance ins = new Instance("http://draft.ottr.xyz/pizza/NamedPizza",
        //        new ArgumentList(new IRITerm(ns + "pizza" + i),
        //            new TermList(new IRITerm(ns + "topping1" + i),
        //                new IRITerm(ns + "topping2" + i),
        //                new IRITerm(ns + "topping2" + i))));
        //    instances.add(ins);
        //}

        //System.out.println("Expanding instances...");
        //ResultStream<Instance> expanded = ResultStream.innerOf(instances).parallel()
        //    .innerFlatMap(ins -> graph.expandInstance(ins));
        //long c = expanded.getStream().count();
        //System.out.println("Done expanding, got " + c + " instances.");

        //InstanceReader instanceReader = new InstanceReader(new WFileReader(),
        //        new WInstanceParser());


        //WInstanceWriter templateInstanceWriter = new WInstanceWriter();
        //ResultConsumer<Instance> insResultConsumer = new ResultConsumer(templateInstanceWriter);
        //instanceReader.apply("src/test/resources/correct/OptionalInstances.ttl")
        //    .map(ins -> {
        //        System.out.println(ins.toString());
        //        return ins;
        //    })
        //    .innerFlatMap(ins -> expGraph.expandInstance(ins))
        //    .forEach(insResultConsumer); 
        //System.out.println("MODEL:\n" + templateInstanceWriter.write());
        //insResultConsumer.printMessages();

        WTemplateWriter templateWriter = new WTemplateWriter();
        ResultConsumer<Template> resultConsumer = new ResultConsumer(templateWriter);
        //expGraph.getAllTemplates().forEach(resultConsumer); 
        graph.getAllTemplates().forEach(resultConsumer); 
        resultConsumer.getMessageHandler().printMessages();
        //System.out.println("Templates:");
        //templateWriter.printDefinitions();
    }

    @AfterClass
    public static void clear() {
        graph = null;
        templateReader = null;
    }
}
