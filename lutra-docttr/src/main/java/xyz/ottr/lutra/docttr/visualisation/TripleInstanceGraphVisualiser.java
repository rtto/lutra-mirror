package xyz.ottr.lutra.docttr.visualisation;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import j2html.tags.DomContent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.docttr.TermAction;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;

public class TripleInstanceGraphVisualiser extends GraphVisualiser implements TermAction<MutableNode>, Consumer<Instance> {

    private final List<Instance> instances;
    private final List<Term> argumentsTerms;

    public TripleInstanceGraphVisualiser(PrefixMapping prefixMapping) {
        super(prefixMapping);
        this.instances = new ArrayList<>();
        this.argumentsTerms = new ArrayList<>();
    }

    private final List<String> excludePredicates = List.of(
        RDF.first.toString(),
        RDF.rest.toString(),
        RDF.type.toString());

    protected void accept(Collection<Instance> instances) {
        instances.forEach(this);
    }

    @Override
    public void accept(Instance instance) {
        this.instances.add(instance);
    }

    private void registerArgumentTerms(Term argumentTerm) {
        this.argumentsTerms.add(argumentTerm);
        if (argumentTerm instanceof ListTerm) {
            ((ListTerm) argumentTerm).asList().forEach(this::registerArgumentTerms);
        }
    }

    public DomContent draw() {
        return renderAllEngines(getGraph());
    }

    public DomContent draw(List<Argument> arguments) {

        arguments.stream()
            .map(Argument::getTerm)
            .forEach(this::registerArgumentTerms);

        return draw();
    }

    // get an instance's list of argument terms
    private final Function<Instance, List<Term>> toTripleFunction = instance ->
        instance.getArguments().stream()
            .map(Argument::getTerm)
            .collect(Collectors.toList());

    public MutableGraph getGraph() {

        var graph = super.getGraph()
            .graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

        this.instances.stream()
            .map(this.toTripleFunction)
            .filter(triple -> !this.excludePredicates.contains(((IRITerm)triple.get(1)).getIri()))
            .forEach(triple -> {

                var predicateIRI = (IRITerm)triple.get(1);

                var subject = getNode(triple.get(0));
                var object = getNode(triple.get(2));
                var predicate = Label.html(getLabel(predicateIRI));

                var link = Factory.to(object).with(predicate);

                if (predicateIRI.getIri().equals(RDFS.subClassOf.getURI())) {
                    link = link.with(Arrow.EMPTY);
                }

                graph.add(subject.addLink(link));
            });
        return graph;
    }

    private MutableNode getNode(Term term) {
        var node = perform(term);
        if (this.argumentsTerms.contains(term)) {
            node.add("color", "red");
            node.add("penwidth", "2");
        }
        return node;
    }

    private String getLabel(Term term) {
        if (term instanceof BlankNodeTerm) {
            return "[blank]";
        }
        
        IRITerm termIRI = (IRITerm) term; 
        var iri = shortenURI(termIRI.getIri());
        if (this.argumentsTerms.contains(termIRI)) {
            iri = "<b><font color=\"red\">" + iri + "</font></b>";
        }
        return iri;
    }

    protected Label getTypedLabel(String types, String label) {
        if (!types.isEmpty()) {
            types = "<i>" + types + "</i>";
        }
        if (!types.isEmpty() && !label.isEmpty()) {
            types += "<br/>";
        }
        return Label.html(types + label);
    }

    protected String getTypesForLabel(Term term) {     
        return this.instances.stream()
                .map(this.toTripleFunction)
                .filter(triple -> triple.get(0).equals(term))
                .filter(triple -> ((IRITerm)triple.get(1)).getIri().equals(RDF.type.getURI()))
                .map(triple -> triple.get(2))
                .distinct()
                .map(this::getLabel)
                .collect(Collectors.joining(", "));
    }

    @Override
    public MutableNode perform(BlankNodeTerm term) {
        return Factory.mutNode(term.toString())
            .add("label", getTypedLabel(getTypesForLabel(term), ""))
            .add(Style.FILLED)
            .add("fillcolor", "gray90")
            .add("height", "0.3")
            .add("width", "0.3");
    }

    @Override
    public MutableNode perform(IRITerm term) {
        return Factory.mutNode(term.toString())
            .add("label", getTypedLabel(getTypesForLabel(term), getLabel(term)))
            .add("URL", term.getIri())
            .add(Style.FILLED)
            .add("fillcolor", "lightskyblue");
    }

    @Override
    public MutableNode perform(ListTerm term) {
        var root = Factory.mutNode(term.toString())
            .add("label", Label.html("<i>rdf:List</i>"))
            .add(Style.FILLED)
            .add("fillcolor", "gray90");

        var listItems = term.asList();
        for (int i = 0; i < listItems.size(); i += 1) {
            root.addLink(
                Factory.to(getNode(listItems.get(i)))
                    .with("label", i + 1)
                    .with("arrowhead", "odiamond")
            );
        }
        return root;
    }

    @Override
    public MutableNode perform(LiteralTerm term) {
        return Factory.mutNode(term.toString())
            // TODO label.html support for language
            .add("label", shortenURI(term.getDatatype()) + Space.LINEBR + term.getValue())
            .add(Style.FILLED, Style.ROUNDED)
            .add("fillcolor", "gray90")
            .add(Font.name("Times"));
    }

    @Override
    public MutableNode perform(NoneTerm term) {
        return perform(new IRITerm(OTTR.none))
            .add("fillcolor", "pink");
    }


}
