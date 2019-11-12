package xyz.ottr.lutra.store.query;

/*-
 * #%L
 * lutra-core
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

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.ObjectTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.store.DependencyGraph;

public class CheckFactoryTest {

    private static final Logger log = LoggerFactory.getLogger(CheckFactoryTest.class);

    private DependencyGraph initStore() {
        
        DependencyGraph store = new DependencyGraph(null);
        store.addTemplateSignature(
            new TemplateSignature("base2",
                new ParameterList(new ObjectTerm("x", true), new ObjectTerm("y", true))));
        store.addTemplateSignature(
            new TemplateSignature("base3",
                new ParameterList(new ObjectTerm("x", true), new ObjectTerm("y", true), new ObjectTerm("z", true))));
        return store;
    }

    private void check(QueryEngine<DependencyGraph> engine, int numErrors, int severity) {

        List<Message> msgs = CheckFactory.allChecks
            .stream()
            .flatMap(c -> c.check(engine))
            .filter(msg -> Message.moreSevere(msg.getLevel(), severity))
            .collect(Collectors.toList());

        String assStr = "Should give " + numErrors + " messages of higher severity than "
            + severity + " but gave " + msgs.size();

        if (msgs.size() != numErrors) {
            msgs.forEach(m -> m.log(this.log));
        }

        assertTrue(assStr, msgs.size() == numErrors); 
    }

    @Test
    public void variableNotUsedWarning() {

        DependencyGraph store = initStore();
        store.addTemplate(
            new Template("test",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Collections.singleton(new Instance("base2",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))))));
        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.WARNING);
    }

    @Test
    public void variableUsedInsideList() {

        DependencyGraph store = initStore();
        store.addTemplate(
            new Template("test",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Collections.singleton(new Instance("base2",
                        new ArgumentList(new TermList(new ObjectTerm("a", true)),
                                         new TermList(new ObjectTerm(1),
                                                      new TermList(new ObjectTerm("b", true))))))));
        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 0, Message.WARNING);
    }

    @Test
    public void variableDefinedTwiceError() {

        DependencyGraph store = initStore();
        store.addTemplate(
            new Template("test",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("a", true)),
                Collections.singleton(new Instance("base2",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))))));
        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void incorrectNumberOfArgumentsError() {

        DependencyGraph store = initStore();
        store.addTemplate(
            new Template("test",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Collections.singleton(new Instance("base3",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm("b", true))))));
        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void nonNonBlankUsedAsNonBlankError() {

        DependencyGraph store = new DependencyGraph(null);
        store.addTemplateSignature(
            new TemplateSignature("base",
                new ParameterList(
                    new TermList(new ObjectTerm("x", true), new ObjectTerm("y", true)),
                    Collections.singleton(new ObjectTerm("x", true)), null, null)));
        store.addTemplate(
            new Template("test",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Collections.singleton(new Instance("base",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm("b", true))))));
        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void cyclicDependencyError() {

        DependencyGraph store = initStore();

        store.addTemplate(
            new Template("test1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(
                    new Instance("base2",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm("b", true))),
                    new Instance("test3",
                        new ArgumentList(new ObjectTerm("b", true), new ObjectTerm("a", true))))
                .collect(Collectors.toSet())));

        store.addTemplate(
            new Template("test2",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Collections.singleton(new Instance("test1",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm("b", true))))));

        store.addTemplate(
            new Template("test3",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Collections.singleton(new Instance("test2",
                        new ArgumentList(new ObjectTerm("a", true), new ObjectTerm("b", true))))));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 3, Message.ERROR); // One cycle for each testN
    }

    @Test
    public void correctConsistentTypeUsageTest() {
        DependencyGraph store = new DependencyGraph(null);

        Term varBase1 = new IRITerm("ex.com/var1");
        varBase1.setType(TypeFactory.getType(OTTR.TypeURI.IRI));
        Term varBase2 = new IRITerm("ex.com/var2");
        varBase2.setType(TypeFactory.getType(OWL.ObjectProperty));
        Term varBase3 = new LiteralTerm("7", TypeFactory.getType(XSD.integer).getIRI());

        store.addTemplateSignature(
            new TemplateSignature("hasInt",
                new ParameterList(varBase1, varBase2, varBase3)));

        Term varC1 = new IRITerm("ex.com/iri");
        varC1.setType(TypeFactory.getType(OTTR.TypeURI.IRI));
        Term varC2 = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        Term varC1b = new IRITerm("ex.com/iri");
        Term varC2b = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        Term constC1 = new IRITerm("ex.com/nicepropiri");
        Term constC2 = new IRITerm("ex.com/niceonlyprop");

        store.addTemplate(
            new Template("testCorrect",
                new ParameterList(varC1, varC2),
                Stream.of(
                    new Instance("hasInt",
                        new ArgumentList(varC1b, constC1, varC2b)),
                    new Instance("hasInt",
                        new ArgumentList(constC1, constC2, varC2b)))
                .collect(Collectors.toSet())));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 0, Message.WARNING);
    }

    @Test
    public void inconsistentTypeUsageTest() {

        // Using a constant as both Class and ObjectProperty
        DependencyGraph store = new DependencyGraph(null);

        Term classVar = new IRITerm("ex.com/classVar");
        classVar.setType(TypeFactory.getType(OWL.Class));
        Term objpropVar = new IRITerm("ex.com/objpropVar");
        objpropVar.setType(TypeFactory.getType(OWL.ObjectProperty));
        Term intVar = new LiteralTerm("7", TypeFactory.getType(XSD.integer).getIRI());

        store.addTemplateSignature(
            new TemplateSignature("hasInt",
                new ParameterList(classVar, objpropVar, intVar)));

        Term classVar2 = new IRITerm("ex.com/class");
        classVar2.setType(TypeFactory.getType(OWL.Class));
        Term intVar2 = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        Term classVar2b = new IRITerm("ex.com/class");
        Term intVar21b = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());
        Term intVar22b = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        Term propClass1 = new IRITerm("ex.com/nicepropclass");
        Term propClass2 = new IRITerm("ex.com/nicepropclass");
        Term prop = new IRITerm("ex.com/niceonlyprop");

        store.addTemplate(
            new Template("testIncorrect",
                new ParameterList(classVar2, intVar2),
                Stream.of(
                    new Instance("hasInt",
                        new ArgumentList(classVar2b, propClass1, intVar21b)),
                    new Instance("hasInt",
                        new ArgumentList(propClass2, prop, intVar22b)))
                .collect(Collectors.toSet())));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void incorrectTypeUsage() {

        // Using a variable with type IRI to a parameter with type Class
        DependencyGraph store = new DependencyGraph(null);

        Term varBase1 = new IRITerm("ex.com/var1");
        varBase1.setType(TypeFactory.getType(OWL.Class));
        Term varBase2 = new IRITerm("ex.com/var2");
        varBase2.setType(TypeFactory.getType(OWL.ObjectProperty));
        Term varBase3 = new LiteralTerm("7", TypeFactory.getType(XSD.integer).getIRI());

        store.addTemplateSignature(
            new TemplateSignature("hasInt",
                new ParameterList(varBase1, varBase2, varBase3)));

        Term var1 = new IRITerm("ex.com/iri");
        var1.setType(TypeFactory.getType(OTTR.TypeURI.IRI));
        Term var2 = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        Term var1b = new IRITerm("ex.com/iri");
        Term var2b = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        Term cons1 = new IRITerm("ex.com/prop1");

        store.addTemplate(
            new Template("testIncorrect",
                new ParameterList(var1, var2),
                Stream.of(
                    new Instance("hasInt",
                        new ArgumentList(var1b, cons1, var2b)))
                .collect(Collectors.toSet())));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void correctListTypeUsage() {

        // Using a variable with type IRI to a parameter with type Class
        DependencyGraph store = new DependencyGraph(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(TypeFactory.getType(OWL.Class)));

        store.addTemplateSignature(
            new TemplateSignature("areClasses",
                new ParameterList(varBase)));

        Term var = new BlankNodeTerm("_:class");
        var.setType(TypeFactory.getType(OWL.Class));

        Term cons = new BlankNodeTerm("_:b");

        store.addTemplate(
            new Template("testCorrect1",
                new ParameterList(var),
                Stream.of(
                    new Instance("areClasses",
                        new ArgumentList(new TermList(cons, var))))
                .collect(Collectors.toSet())));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 0, Message.ERROR);
    }

    @Test
    public void incorrectListTypeUsage() {

        // Using a list of a variable of type Class and a an integer as argument
        // to a parameter of type NEList<Class>
        DependencyGraph store = new DependencyGraph(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(TypeFactory.getType(OWL.Class)));

        store.addTemplateSignature(
            new TemplateSignature("areClasses",
                new ParameterList(varBase)));

        Term varClass = new BlankNodeTerm("_:class");
        varClass.setType(TypeFactory.getType(OWL.Class));

        Term one = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        store.addTemplate(
            new Template("testCorrect1",
                new ParameterList(varClass),
                Stream.of(
                    new Instance("areClasses",
                        new ArgumentList(new TermList(one, varClass))))
                .collect(Collectors.toSet())));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void incorrectDeepListTypeUsage() {

        DependencyGraph store = new DependencyGraph(null);

        Term varBase = new BlankNodeTerm("_:classeses");
        varBase.setType(new NEListType(new NEListType(TypeFactory.getType(OWL.Class))));

        store.addTemplateSignature(
            new TemplateSignature("deepLists",
                new ParameterList(varBase)));

        Term varClass = new BlankNodeTerm("_:class");
        varClass.setType(TypeFactory.getType(OWL.Class));

        Term one = new LiteralTerm("1", TypeFactory.getType(XSD.integer).getIRI());

        store.addTemplate(
            new Template("testCorrect1",
                new ParameterList(varClass),
                Stream.of(
                    new Instance("areClasses",
                        new ArgumentList(new TermList(
                                new TermList(new BlankNodeTerm(), varClass), // (_:a ?var) OK
                                new TermList(one, varClass)))))              // (1 ?var)   ERR
                .collect(Collectors.toSet())));

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }
}
